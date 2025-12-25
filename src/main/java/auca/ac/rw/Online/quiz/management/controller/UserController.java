package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.EUserRole;
import auca.ac.rw.Online.quiz.management.model.Location;
import auca.ac.rw.Online.quiz.management.model.LocationType;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import auca.ac.rw.Online.quiz.management.service.LocationService;
import auca.ac.rw.Online.quiz.management.util.EmailValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, LocationService locationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.locationService = locationService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> list() {
        try {
            // Use JOIN FETCH to eagerly load location and avoid LazyInitializationException
            List<User> users = userRepository.findAllWithLocation();
            // Set password to null for all users before returning
            users.forEach(user -> user.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            System.err.println("[UserController] Error fetching users: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    @GetMapping("/page")
    @Transactional(readOnly = true)
    public ResponseEntity<?> page(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String q) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage;
            
            // Fetch users (location will be loaded lazily, but we'll handle it)
            if (q == null || q.isBlank()) {
                userPage = userRepository.findAll(pageable);
            } else {
                userPage = userRepository.findByUsernameIgnoreCaseContaining(q, pageable);
            }
            
            // Eagerly load locations for all users in the page to avoid LazyInitializationException
            List<User> users = userPage.getContent();
            if (users != null && !users.isEmpty()) {
                try {
                    // Get all user IDs from the page
                    List<Long> userIds = new java.util.ArrayList<>();
                    for (User user : users) {
                        if (user != null && user.getId() != null) {
                            userIds.add(user.getId());
                        }
                    }
                    
                    // Only fetch if we have IDs
                    if (!userIds.isEmpty()) {
                        // Fetch users with locations using JOIN FETCH
                        List<User> usersWithLocation = userRepository.findAllWithLocationByIds(userIds);
                        
                        // Create a map for quick lookup
                        Map<Long, User> userMap = new java.util.HashMap<>();
                        if (usersWithLocation != null) {
                            for (User u : usersWithLocation) {
                                if (u != null && u.getId() != null) {
                                    userMap.put(u.getId(), u);
                                }
                            }
                        }
                        
                        // Create a new list with users that have locations loaded
                        List<User> updatedUsers = new java.util.ArrayList<>();
                        for (User user : users) {
                            if (user != null) {
                                User userWithLocation = userMap.get(user.getId());
                                if (userWithLocation != null) {
                                    updatedUsers.add(userWithLocation);
                                } else {
                                    updatedUsers.add(user);
                                }
                            }
                        }
                        
                        // Create a new Page with updated users
                        userPage = new PageImpl<>(
                            updatedUsers,
                            userPage.getPageable(),
                            userPage.getTotalElements()
                        );
                    }
                } catch (Exception e) {
                    System.err.println("[UserController] Error loading locations: " + e.getMessage());
                    e.printStackTrace();
                    // Continue with original users if location loading fails
                }
            }
            
            // Set password to null and handle location serialization for all users before returning
            userPage.getContent().forEach(user -> {
                user.setPassword(null);
                // If location is a proxy and can't be serialized, set to null
                if (user.getLocation() != null) {
                    try {
                        // Try to access location to see if it's loaded
                        user.getLocation().getId();
                    } catch (Exception e) {
                        // If location can't be accessed, set to null to avoid serialization error
                        user.setLocation(null);
                    }
                }
            });
            
            return ResponseEntity.ok(userPage);
        } catch (Exception e) {
            System.err.println("[UserController] Error fetching users page: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> get(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/createUser")
    public ResponseEntity<?> create(@RequestBody User user) {
        System.out.println("[UserController] Create user request for: " + user.getUsername());
        // Validate required fields
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("Username is required");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }

        // Validate email address
        try {
            EmailValidator.validateEmail(user.getEmail());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

        // Normalize email to lowercase
        user.setEmail(EmailValidator.normalizeEmail(user.getEmail()));

        // Check for duplicates
        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists: " + user.getEmail());
        }
        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists: " + user.getUsername());
        }

        // Set default role if not provided
        if (user.getRole() == null) {
            user.setRole(EUserRole.STUDENT);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Validate that location is provided (required)
        if (user.getLocation() == null) {
            return ResponseEntity.badRequest().body("Location is required for all users");
        }
        
        Location location = user.getLocation();
        
        // Validate that at least province is provided (required fields)
        if (location.getProvinceId() == null || location.getProvinceName() == null || location.getProvinceName().isBlank()) {
            return ResponseEntity.badRequest().body("Province ID and Province Name are required for location");
        }

        try {
            // Determine location type based on what fields are filled
            if (location.getVillageId() != null && location.getVillageName() != null && !location.getVillageName().isBlank()) {
                location.setLocationType(LocationType.VILLAGE);
            } else if (location.getCellId() != null && location.getCellName() != null && !location.getCellName().isBlank()) {
                location.setLocationType(LocationType.CELL);
            } else if (location.getSectorId() != null && location.getSectorName() != null && !location.getSectorName().isBlank()) {
                location.setLocationType(LocationType.SECTOR);
            } else if (location.getDistrictId() != null && location.getDistrictName() != null && !location.getDistrictName().isBlank()) {
                location.setLocationType(LocationType.DISTRICT);
            } else {
                location.setLocationType(LocationType.PROVINCE);
            }
            
            // Save location first (before user to ensure location has an ID)
            Location savedLocation = locationService.saveLocation(location);
            user.setLocation(savedLocation);
            
            // Save user
            User saved = userRepository.save(user);
            
            // Location-User relationship is now @ManyToOne, so no need to manually set user on location
            // Avoid returning password hash
            saved.setPassword(null);
            return ResponseEntity.created(URI.create("/api/users/" + saved.getId())).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating user: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User user) {
        System.out.println("[UserController] ========== UPDATE USER REQUEST ==========");
        System.out.println("[UserController] User ID: " + id);
        System.out.println("[UserController] Request body location: " + (user.getLocation() != null ? "present" : "null"));
        if (user.getLocation() != null) {
            System.out.println("[UserController] Location province: " + user.getLocation().getProvinceName());
        }
        
        try {
            return userRepository.findById(id)
                    .map(existing -> {
                        try {
                            System.out.println("[UserController] Found existing user: " + existing.getUsername());
                            // Force load location if it exists (since it's LAZY)
                            if (existing.getLocation() != null) {
                                Long locationId = existing.getLocation().getId(); // Force load
                                System.out.println("[UserController] Existing location ID: " + locationId);
                            } else {
                                System.out.println("[UserController] User has no existing location");
                            }
                            existing.setUsername(user.getUsername());
                            // Only update password when a new one is provided to avoid encoding null/empty
                            // values
                            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                                existing.setPassword(passwordEncoder.encode(user.getPassword()));
                            }

                            // Validate email address if email is being updated
                            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                                try {
                                    EmailValidator.validateEmail(user.getEmail());
                                    String normalizedEmail = EmailValidator.normalizeEmail(user.getEmail());
                                    // Check if email is already taken by another user
                                    if (!normalizedEmail.equals(existing.getEmail()) 
                                            && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
                                    }
                                    existing.setEmail(normalizedEmail);
                                } catch (IllegalArgumentException ex) {
                                    return ResponseEntity.badRequest().body(ex.getMessage());
                                }
                            }

                            if (user.getRole() != null) {
                                existing.setRole(user.getRole());
                            }
                            
                            // Location is required - validate it's provided
                            if (user.getLocation() == null) {
                                return ResponseEntity.badRequest().body("Location is required for all users");
                            }
                            
                            // Update location
                            {
                                Location location = user.getLocation();
                                
                                // Validate that at least province is provided (required fields)
                                if (location.getProvinceId() == null || location.getProvinceName() == null || location.getProvinceName().isBlank()) {
                                    return ResponseEntity.badRequest().body("Province ID and Province Name are required for location");
                                }
                                
                                Location locationToSave;
                                
                                // If user already has a location, update it; otherwise create new
                                if (existing.getLocation() != null && existing.getLocation().getId() != null) {
                                    // Update existing location - get it fresh from the database
                                    Long existingLocationId = existing.getLocation().getId();
                                    locationToSave = locationService.getLocationById(existingLocationId)
                                        .orElseThrow(() -> new RuntimeException("Existing location not found: " + existingLocationId));
                                    System.out.println("[UserController] Updating existing location ID: " + locationToSave.getId());
                                } else {
                                    // Create new location
                                    locationToSave = new Location();
                                    System.out.println("[UserController] Creating new location");
                                }
                                
                                // Update location fields from userData
                                locationToSave.setProvinceId(location.getProvinceId());
                                locationToSave.setProvinceName(location.getProvinceName());
                                locationToSave.setDistrictId(location.getDistrictId());
                                locationToSave.setDistrictName(location.getDistrictName());
                                locationToSave.setSectorId(location.getSectorId());
                                locationToSave.setSectorName(location.getSectorName());
                                locationToSave.setCellId(location.getCellId());
                                locationToSave.setCellName(location.getCellName());
                                locationToSave.setVillageId(location.getVillageId());
                                locationToSave.setVillageName(location.getVillageName());
                                
                                // Determine location type based on what fields are filled
                                if (locationToSave.getVillageId() != null && locationToSave.getVillageName() != null && !locationToSave.getVillageName().isBlank()) {
                                    locationToSave.setLocationType(LocationType.VILLAGE);
                                } else if (locationToSave.getCellId() != null && locationToSave.getCellName() != null && !locationToSave.getCellName().isBlank()) {
                                    locationToSave.setLocationType(LocationType.CELL);
                                } else if (locationToSave.getSectorId() != null && locationToSave.getSectorName() != null && !locationToSave.getSectorName().isBlank()) {
                                    locationToSave.setLocationType(LocationType.SECTOR);
                                } else if (locationToSave.getDistrictId() != null && locationToSave.getDistrictName() != null && !locationToSave.getDistrictName().isBlank()) {
                                    locationToSave.setLocationType(LocationType.DISTRICT);
                                } else {
                                    locationToSave.setLocationType(LocationType.PROVINCE);
                                }
                                
                                // Save location first
                                System.out.println("[UserController] Saving location with province: " + locationToSave.getProvinceName());
                                Location savedLocation = locationService.saveLocation(locationToSave);
                                System.out.println("[UserController] Location saved with ID: " + savedLocation.getId());
                                
                                // Set the location on the user (this will update the foreign key)
                                existing.setLocation(savedLocation);
                            }
                            
                            // Save user (this will persist the location_id foreign key)
                            System.out.println("[UserController] Saving user with ID: " + existing.getId());
                            User updated = userRepository.save(existing);
                            System.out.println("[UserController] User saved successfully");
                            
                            // Location-User relationship is now @ManyToOne, so no need to manually set user on location
                            
                            // Avoid returning password hash
                            updated.setPassword(null);
                            return ResponseEntity.ok(updated);
                        } catch (org.springframework.dao.DataIntegrityViolationException e) {
                            System.err.println("[UserController] DataIntegrityViolationException: " + e.getMessage());
                            if (e.getCause() != null) {
                                System.err.println("[UserController] Root cause: " + e.getCause().getMessage());
                            }
                            e.printStackTrace();
                            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                            if (message != null && (message.contains("unique") || message.contains("UNIQUE"))) {
                                return ResponseEntity.status(HttpStatus.CONFLICT)
                                        .body("A record with this value already exists. This might be due to a location already being assigned to another user.");
                            }
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Database constraint violation: " + message);
                        } catch (jakarta.persistence.PersistenceException e) {
                            System.err.println("[UserController] PersistenceException: " + e.getMessage());
                            if (e.getCause() != null) {
                                System.err.println("[UserController] Root cause: " + e.getCause().getMessage());
                            }
                            e.printStackTrace();
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Database error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                        } catch (Exception e) {
                            System.err.println("[UserController] Error updating user: " + e.getClass().getName());
                            System.err.println("[UserController] Error message: " + e.getMessage());
                            if (e.getCause() != null) {
                                System.err.println("[UserController] Cause: " + e.getCause().getMessage());
                            }
                            e.printStackTrace();
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body("Error updating user: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
                        }
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            System.err.println("[UserController] OUTER DataIntegrityViolationException: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[UserController] OUTER Root cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("status", HttpStatus.CONFLICT.value());
            error.put("error", "Data Integrity Violation");
            error.put("message", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        } catch (jakarta.persistence.PersistenceException e) {
            System.err.println("[UserController] OUTER PersistenceException: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[UserController] OUTER Root cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("error", "Database Error");
            error.put("message", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            System.err.println("[UserController] ========== OUTER EXCEPTION ==========");
            System.err.println("[UserController] Exception type: " + e.getClass().getName());
            System.err.println("[UserController] Exception message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[UserController] Cause: " + e.getCause().getMessage());
                System.err.println("[UserController] Cause type: " + e.getCause().getClass().getName());
            }
            e.printStackTrace();
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("error", "Internal Server Error");
            error.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            if (e.getCause() != null) {
                error.put("cause", e.getCause().getMessage());
            }
            error.put("exceptionType", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> settings) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userRepository.findByUsernameIgnoreCase(username)
                .map(user -> {
                    try {
                        String settingsJson = new com.fasterxml.jackson.databind.ObjectMapper()
                                .writeValueAsString(settings);
                        user.setSettings(settingsJson);
                        userRepository.save(user);
                        return ResponseEntity.ok("Settings updated successfully");
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving settings");
                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User userData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userRepository.findByUsernameIgnoreCase(username)
                .map(existing -> {
                    // Check if user is STUDENT or INSTRUCTOR - they cannot update profile information
                    if (existing.getRole() == auca.ac.rw.Online.quiz.management.model.EUserRole.STUDENT 
                            || existing.getRole() == auca.ac.rw.Online.quiz.management.model.EUserRole.INSTRUCTOR) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body("Students and Instructors are not allowed to change their profile information. Only password changes are allowed.");
                    }

                    // Only ADMIN can update profile information
                    // Update username if provided and different
                    if (userData.getUsername() != null && !userData.getUsername().isBlank() 
                            && !userData.getUsername().equals(existing.getUsername())) {
                        // Check if new username is already taken
                        if (userRepository.existsByUsernameIgnoreCase(userData.getUsername())) {
                            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
                        }
                        existing.setUsername(userData.getUsername());
                    }

                    // Validate and update email if provided
                    if (userData.getEmail() != null && !userData.getEmail().isBlank()) {
                        try {
                            EmailValidator.validateEmail(userData.getEmail());
                            String normalizedEmail = EmailValidator.normalizeEmail(userData.getEmail());
                            // Check if email is already taken by another user
                            if (!normalizedEmail.equals(existing.getEmail()) 
                                    && userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
                            }
                            existing.setEmail(normalizedEmail);
                        } catch (IllegalArgumentException ex) {
                            return ResponseEntity.badRequest().body(ex.getMessage());
                        }
                    }

                    // Update location if provided
                    if (userData.getLocation() != null) {
                        Location location = userData.getLocation();
                        
                        // If user already has a location, update it; otherwise create new
                        Location locationToSave = existing.getLocation();
                        if (locationToSave == null) {
                            locationToSave = new Location();
                        }
                        
                        // Update location fields from userData
                        locationToSave.setProvinceId(location.getProvinceId());
                        locationToSave.setProvinceName(location.getProvinceName());
                        locationToSave.setDistrictId(location.getDistrictId());
                        locationToSave.setDistrictName(location.getDistrictName());
                        locationToSave.setSectorId(location.getSectorId());
                        locationToSave.setSectorName(location.getSectorName());
                        locationToSave.setCellId(location.getCellId());
                        locationToSave.setCellName(location.getCellName());
                        locationToSave.setVillageId(location.getVillageId());
                        locationToSave.setVillageName(location.getVillageName());
                        
                        // Determine location type based on what fields are filled
                        if (locationToSave.getVillageId() != null && locationToSave.getVillageName() != null && !locationToSave.getVillageName().isBlank()) {
                            locationToSave.setLocationType(LocationType.VILLAGE);
                        } else if (locationToSave.getCellId() != null && locationToSave.getCellName() != null && !locationToSave.getCellName().isBlank()) {
                            locationToSave.setLocationType(LocationType.CELL);
                        } else if (locationToSave.getSectorId() != null && locationToSave.getSectorName() != null && !locationToSave.getSectorName().isBlank()) {
                            locationToSave.setLocationType(LocationType.SECTOR);
                        } else if (locationToSave.getDistrictId() != null && locationToSave.getDistrictName() != null && !locationToSave.getDistrictName().isBlank()) {
                            locationToSave.setLocationType(LocationType.DISTRICT);
                        } else {
                            locationToSave.setLocationType(LocationType.PROVINCE);
                        }
                        
                        // Save or update location (this will create a new record or update existing)
                        Location savedLocation = locationService.saveLocation(locationToSave);
                        existing.setLocation(savedLocation);
                    }

                    // Save user
                    User updated = userRepository.save(existing);
                    
                    // Location-User relationship is now @ManyToOne, so no need to manually set user on location
                    
                    // Avoid returning password hash
                    updated.setPassword(null);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        return userRepository.findByUsernameIgnoreCase(username)
                .map(user -> {
                    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect current password");
                    }
                    user.setPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok("Password changed successfully");
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
