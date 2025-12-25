package auca.ac.rw.Online.quiz.management.system;

import auca.ac.rw.Online.quiz.management.model.EUserRole;
import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.LocationType;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.LocationRepository;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import auca.ac.rw.Online.quiz.management.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates a single default admin account if none exists.
 * 
 * Default Admin Credentials:
 * - Username: Friend Enock
 * - Email: friendeno123@gmail.com
 * - Password: Admin123@
 * - Role: ADMIN
 * 
 * ⚠️ WARNING: Change password immediately after first login in production!
 */
@Component
public class DefaultAdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DefaultAdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;
    private final LocationRepository locationRepository;

    public DefaultAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder, LocationService locationService, LocationRepository locationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationService = locationService;
        this.locationRepository = locationRepository;
    }
    
    /**
     * Gets or creates a default location (Kigali City) for default users
     */
    private Location getOrCreateDefaultLocation() {
        // Try to find an existing location in Kigali by province name (using repository directly to avoid loading users)
        var kigaliLocations = locationRepository.findByProvinceName("Kigali");
        if (kigaliLocations != null && !kigaliLocations.isEmpty()) {
            return kigaliLocations.get(0);
        }
        
        // If no Kigali location found, try to get any location by ID (limit to first few to avoid loading all)
        var anyLocation = locationRepository.findById(1L);
        if (anyLocation.isPresent()) {
            return anyLocation.get();
        }
        
        // Try to get location by province ID
        var locationsByProvince = locationRepository.findByProvinceId(1L);
        if (locationsByProvince != null && !locationsByProvince.isEmpty()) {
            return locationsByProvince.get(0);
        }
        
        // Create a default location for Kigali City
        Location defaultLocation = new Location();
        defaultLocation.setProvinceId(1L);
        defaultLocation.setProvinceName("Kigali");
        defaultLocation.setLocationType(LocationType.PROVINCE);
        return locationService.saveLocation(defaultLocation);
    }

    @Override
    public void run(ApplicationArguments args) {
        // Get or create default location for default users
        Location defaultLocation = getOrCreateDefaultLocation();
        
        String email = "friendeno123@gmail.com";
        String username = "Friend Enock";

        // Upsert admin to guarantee known credentials
        User admin = userRepository.findByEmailIgnoreCase(email)
                .or(() -> userRepository.findByUsernameIgnoreCase(username))
                .orElseGet(User::new);

        admin.setUsername(username);
        admin.setEmail(email);
        admin.setRole(EUserRole.ADMIN);
        admin.setPassword(passwordEncoder.encode("Admin123@"));
        
        // Set location if not already set
        if (admin.getLocation() == null) {
            admin.setLocation(defaultLocation);
        }

        userRepository.save(admin);
        log.info("Default admin ensured with email {}", email);

        // Create a default instructor account if absent
        String instrEmail = "instructor@example.com";
        String instrUsername = "Default Instructor";

        User instructor = userRepository.findByEmailIgnoreCase(instrEmail)
            .or(() -> userRepository.findByUsernameIgnoreCase(instrUsername))
            .orElseGet(User::new);

        instructor.setUsername(instrUsername);
        instructor.setEmail(instrEmail);
        instructor.setRole(EUserRole.INSTRUCTOR);
        instructor.setPassword(passwordEncoder.encode("Instructor123@"));
        
        // Set location if not already set
        if (instructor.getLocation() == null) {
            instructor.setLocation(defaultLocation);
        }

        userRepository.save(instructor);
        log.info("Default instructor ensured with email {}", instrEmail);

        // Create a default student account if absent
        String studEmail = "student@example.com";
        String studUsername = "Default Student";

        User student = userRepository.findByEmailIgnoreCase(studEmail)
            .or(() -> userRepository.findByUsernameIgnoreCase(studUsername))
            .orElseGet(User::new);

        student.setUsername(studUsername);
        student.setEmail(studEmail);
        student.setRole(EUserRole.STUDENT);
        student.setPassword(passwordEncoder.encode("Student123@"));
        
        // Set location if not already set
        if (student.getLocation() == null) {
            student.setLocation(defaultLocation);
        }

        userRepository.save(student);
        log.info("Default student ensured with email {}", studEmail);
    }
}


