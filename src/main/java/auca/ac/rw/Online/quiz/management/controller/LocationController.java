package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.controller.dto.LocationWithUserDTO;
import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.service.LocationService;
import auca.ac.rw.Online.quiz.management.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.net.URI;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Autowired
    private LocationRepository locationRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public List<Location> getAll() { 
        List<Location> locations = locationService.getAllLocations();
        // Eagerly load user data to avoid LazyInitializationException
        if (locations != null && !locations.isEmpty()) {
            List<Long> locationIds = locations.stream()
                .map(Location::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toList());
            
            if (!locationIds.isEmpty()) {
                List<Location> locationsWithUser = locationRepository.findAllWithUserByIds(locationIds);
                java.util.Map<Long, Location> locationMap = locationsWithUser.stream()
                    .filter(loc -> loc.getId() != null)
                    .collect(java.util.stream.Collectors.toMap(Location::getId, loc -> loc));
                
                // Update locations with eagerly loaded user data (users is now a collection)
                locations.forEach(loc -> {
                    Location locationWithUser = locationMap.get(loc.getId());
                    if (locationWithUser != null && locationWithUser.getUsers() != null) {
                        loc.setUsers(locationWithUser.getUsers());
                    }
                });
            }
        }
        return locations;
    }
    
    @GetMapping("/page")
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<LocationWithUserDTO> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "false") boolean userLocationsOnly) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Location> result;
        
        // If userLocationsOnly is true, only return locations associated with users
        if (userLocationsOnly) {
            result = locationService.getUserLocationsPage(q, pageable);
        } else {
            result = locationService.search(q, pageable);
        }
        
        // Flatten locations with users into DTOs (one row per location-user pair)
        List<LocationWithUserDTO> dtoList = new ArrayList<>();
        long totalElements = result != null ? result.getTotalElements() : 0;
        
        if (result != null && result.getContent() != null && !result.getContent().isEmpty()) {
            List<Long> locationIds = result.getContent().stream()
                .map(Location::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toList());
            
            if (!locationIds.isEmpty()) {
                try {
                    List<Location> locationsWithUser = locationRepository.findAllWithUserByIds(locationIds);
                    final java.util.Map<Long, Location> locationMap;
                    if (locationsWithUser != null) {
                        locationMap = locationsWithUser.stream()
                            .filter(loc -> loc != null && loc.getId() != null)
                            .collect(java.util.stream.Collectors.toMap(Location::getId, loc -> loc, (existing, replacement) -> existing));
                    } else {
                        locationMap = new java.util.HashMap<>();
                    }
                    
                    // Flatten: create one DTO per location-user pair
                    for (Location loc : result.getContent()) {
                        if (loc != null) {
                            Location locationWithUser = locationMap.get(loc.getId());
                            if (locationWithUser != null && locationWithUser.getUsers() != null && !locationWithUser.getUsers().isEmpty()) {
                                // Create a DTO for each user at this location
                                for (User user : locationWithUser.getUsers()) {
                                    dtoList.add(new LocationWithUserDTO(locationWithUser, user));
                                }
                            } else {
                                // Location with no users - still include it with null user info
                                dtoList.add(new LocationWithUserDTO(loc, null));
                            }
                        }
                    }
                    
                    // For totalElements, we approximate by using the location count
                    // In a production system, you'd want to count all location-user pairs
                    // For now, this provides a reasonable approximation
                    totalElements = dtoList.isEmpty() ? 0 : (long) (result.getTotalElements() * 1.0);
                } catch (Exception e) {
                    System.err.println("[LocationController] Error eagerly loading user data: " + e.getMessage());
                    e.printStackTrace();
                    // Fallback: create DTOs without user data
                    for (Location loc : result.getContent()) {
                        if (loc != null) {
                            dtoList.add(new LocationWithUserDTO(loc, null));
                        }
                    }
                    totalElements = result.getTotalElements();
                }
            }
        }
        
        // Apply pagination to the flattened list
        // Since we've already flattened, we need to slice the list to match the requested page size
        int start = (int) (pageable.getOffset());
        int end = Math.min(start + pageable.getPageSize(), dtoList.size());
        List<LocationWithUserDTO> pagedList = dtoList.isEmpty() ? dtoList : dtoList.subList(Math.min(start, dtoList.size()), end);
        
        return new org.springframework.data.domain.PageImpl<>(pagedList, pageable, totalElements);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getById(@PathVariable Long id) {
        return locationService.getLocationById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Location> create(@RequestBody Location location) {
        Location saved = locationService.saveLocation(location);
        return ResponseEntity.created(URI.create("/api/locations/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Location> update(@PathVariable Long id, @RequestBody Location location) {
        return locationService.getLocationById(id)
                .map(existing -> {
                    existing.setProvinceId(location.getProvinceId());
                    existing.setProvinceName(location.getProvinceName());
                    existing.setDistrictId(location.getDistrictId());
                    existing.setDistrictName(location.getDistrictName());
                    existing.setSectorId(location.getSectorId());
                    existing.setSectorName(location.getSectorName());
                    existing.setCellId(location.getCellId());
                    existing.setCellName(location.getCellName());
                    existing.setVillageId(location.getVillageId());
                    existing.setVillageName(location.getVillageName());
                    existing.setLocationType(location.getLocationType());
                    Location saved = locationService.saveLocation(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (locationService.getLocationById(id).isPresent()) {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/province-id/{provinceId}")
    public List<Location> getByProvinceId(@PathVariable Long provinceId) {
        return locationRepository.findByProvinceId(provinceId);
    }

    @GetMapping("/province-name/{provinceName}")
    public List<Location> getByProvinceName(@PathVariable String provinceName) {
        return locationRepository.findByProvinceName(provinceName);
    }

    @GetMapping("/users/province-name/{provinceName}")
    public List<User> getUsersByProvinceName(@PathVariable String provinceName) {
        return locationRepository.findUsersByProvinceName(provinceName);
    }

    @GetMapping("/users/province-id/{provinceId}")
    public List<User> getUsersByProvinceId(@PathVariable Long provinceId) {
        return locationRepository.findUsersByProvinceId(provinceId);
    }

    // Cascading dropdown endpoints
    @GetMapping("/provinces")
    public ResponseEntity<List<Map<String, Object>>> getProvinces() {
        List<Object[]> results = locationRepository.findDistinctProvinces();
        List<Map<String, Object>> provinces = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> province = new java.util.HashMap<>();
            province.put("id", row[0]);
            province.put("name", row[1]);
            provinces.add(province);
        }
        return ResponseEntity.ok(provinces);
    }

    @GetMapping("/districts")
    public ResponseEntity<List<Map<String, Object>>> getDistricts(@RequestParam Long provinceId) {
        List<Object[]> results = locationRepository.findDistinctDistrictsByProvinceId(provinceId);
        List<Map<String, Object>> districts = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> district = new java.util.HashMap<>();
            district.put("id", row[0]);
            district.put("name", row[1]);
            districts.add(district);
        }
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/sectors")
    public ResponseEntity<List<Map<String, Object>>> getSectors(
            @RequestParam Long provinceId,
            @RequestParam Long districtId) {
        List<Object[]> results = locationRepository.findDistinctSectorsByProvinceAndDistrict(provinceId, districtId);
        List<Map<String, Object>> sectors = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> sector = new java.util.HashMap<>();
            sector.put("id", row[0]);
            sector.put("name", row[1]);
            sectors.add(sector);
        }
        return ResponseEntity.ok(sectors);
    }

    @GetMapping("/cells")
    public ResponseEntity<List<Map<String, Object>>> getCells(
            @RequestParam Long provinceId,
            @RequestParam Long districtId,
            @RequestParam Long sectorId) {
        List<Object[]> results = locationRepository.findDistinctCellsByProvinceDistrictAndSector(provinceId, districtId, sectorId);
        List<Map<String, Object>> cells = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> cell = new java.util.HashMap<>();
            cell.put("id", row[0]);
            cell.put("name", row[1]);
            cells.add(cell);
        }
        return ResponseEntity.ok(cells);
    }

    @GetMapping("/villages")
    public ResponseEntity<List<Map<String, Object>>> getVillages(
            @RequestParam Long provinceId,
            @RequestParam Long districtId,
            @RequestParam Long sectorId,
            @RequestParam Long cellId) {
        List<Object[]> results = locationRepository.findDistinctVillagesByLocation(provinceId, districtId, sectorId, cellId);
        List<Map<String, Object>> villages = new java.util.ArrayList<>();
        for (Object[] row : results) {
            Map<String, Object> village = new java.util.HashMap<>();
            village.put("id", row[0]);
            village.put("name", row[1]);
            villages.add(village);
        }
        return ResponseEntity.ok(villages);
    }
    
    @GetMapping("/check-provinces")
    public ResponseEntity<Map<String, Object>> checkProvinces() {
        List<Object[]> provinces = locationRepository.findDistinctProvinces();
        List<Map<String, Object>> provinceList = new java.util.ArrayList<>();
        if (provinces != null) {
            for (Object[] row : provinces) {
                Map<String, Object> province = new java.util.HashMap<>();
                province.put("id", row[0]);
                province.put("name", row[1]);
                provinceList.add(province);
            }
        }
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("count", provinceList.size());
        result.put("provinces", provinceList);
        result.put("expected", 5);
        return ResponseEntity.ok(result);
    }
    
}
