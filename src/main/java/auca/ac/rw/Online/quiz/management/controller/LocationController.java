package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.service.LocationService;
import auca.ac.rw.Online.quiz.management.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.net.URI;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    @Autowired
    private LocationService locationService;
    @Autowired
    private LocationRepository locationRepository;

    @GetMapping
    public List<Location> getAll() { return locationService.getAllLocations(); }

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
}
