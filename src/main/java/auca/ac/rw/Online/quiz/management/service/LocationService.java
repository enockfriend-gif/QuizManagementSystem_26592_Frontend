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

    // Custom queries (to be implemented in LocationRepository)
    // Examples:
    // public List<Location> findByProvinceId(Long provinceId) { ... }
    // public List<Location> findByProvinceName(String provinceName) { ... }
    // public List<User> findUsersByProvinceName(String provinceName) { ... }
}
