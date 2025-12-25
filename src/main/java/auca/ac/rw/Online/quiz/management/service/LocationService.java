package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getAllLocations() { return locationRepository.findAll(); }

    public Optional<Location> getLocationById(Long id) { return locationRepository.findById(id); }

    public Location saveLocation(Location location) { return locationRepository.save(location); }

    public void deleteLocation(Long id) { locationRepository.deleteById(id); }
    
    public org.springframework.data.domain.Page<Location> search(String q, org.springframework.data.domain.Pageable pageable) {
        if (q == null || q.isBlank()) {
            return locationRepository.findAll(pageable);
        }
        return locationRepository.findByProvinceNameContainingIgnoreCaseOrDistrictNameContainingIgnoreCaseOrSectorNameContainingIgnoreCase(
            q, q, q, pageable);
    }
    
    public List<Location> getUserLocations() {
        return locationRepository.findAllWithUsers();
    }
    
    public org.springframework.data.domain.Page<Location> getUserLocationsPage(String q, org.springframework.data.domain.Pageable pageable) {
        if (q == null || q.isBlank()) {
            return locationRepository.findAllWithUsers(pageable);
        }
        return locationRepository.findUserLocationsWithSearch(q, pageable);
    }
    
    public void clearAllLocations() {
        locationRepository.deleteAll();
    }
    
    public long getLocationCount() {
        return locationRepository.count();
    }
    
    public int getProvinceCount() {
        List<Object[]> provinces = locationRepository.findDistinctProvinces();
        return provinces != null ? provinces.size() : 0;
    }
}
