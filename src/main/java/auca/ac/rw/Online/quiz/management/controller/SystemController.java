package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.EUserRole;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * System-level utilities. Exposes a small, opt-in endpoint to ensure the default admin
 * user exists in the database. This endpoint is intentionally gated by the
 * environment variable `ALLOW_SETUP` and will return 403 if that variable is not set to `true`.
 *
 * Usage (local/dev):
 *  - set environment variable `ALLOW_SETUP=true`
 *  - POST /api/system/ensure-default-admin
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    private static final Logger log = LoggerFactory.getLogger(SystemController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/ensure-default-admin")
    public ResponseEntity<String> ensureDefaultAdmin() {
        String allow = System.getenv("ALLOW_SETUP");
        if (allow == null || !allow.equalsIgnoreCase("true")) {
            log.warn("Attempt to call ensure-default-admin but ALLOW_SETUP is not enabled");
            return ResponseEntity.status(403).body("Setup endpoint disabled. Set ALLOW_SETUP=true to enable.");
        }

        String email = "friendeno123@gmail.com";
        String username = "Friend Enock";
        String password = "Admin123@";

        Optional<User> existing = userRepository.findByEmailIgnoreCase(email)
                .or(() -> userRepository.findByUsernameIgnoreCase(username));

        User admin = existing.orElseGet(User::new);
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setRole(EUserRole.ADMIN);
        admin.setPassword(passwordEncoder.encode(password));

        userRepository.save(admin);
        log.info("Default admin ensured with email {}", email);
        return ResponseEntity.ok("Default admin created/updated (email=" + email + ")");
    }
}
