package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.config.UserDetailsServiceImpl;
import auca.ac.rw.Online.quiz.management.model.OtpToken;
import auca.ac.rw.Online.quiz.management.model.OtpType;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import auca.ac.rw.Online.quiz.management.security.JwtService;
import auca.ac.rw.Online.quiz.management.util.EmailValidator;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
            UserDetailsServiceImpl userDetailsService,
            JwtService jwtService,
            OtpService otpService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String initiateLoginOtp(String usernameOrEmail, String password) {
        log.debug("Initiating login OTP for: {}", usernameOrEmail);

        // Check if user exists first
        boolean userExists = userRepository.findByUsernameIgnoreCase(usernameOrEmail).isPresent() ||
                userRepository.findByEmailIgnoreCase(usernameOrEmail).isPresent();

        if (!userExists) {
            log.warn("Login attempt failed: User not found - {}", usernameOrEmail);
            throw new BadCredentialsException("User not found. Please check your username/email.");
        }

        // Authenticate username/password first
        try {
            log.debug("Attempting authentication for: {}", usernameOrEmail);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));
            log.debug("Authentication successful for: {}", usernameOrEmail);
        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for {}: {}", usernameOrEmail, ex.getMessage());
            throw new BadCredentialsException("Invalid password. Please check your credentials.");
        } catch (Exception ex) {
            log.error("Authentication error for {}: {}", usernameOrEmail, ex.getMessage(), ex);
            throw new BadCredentialsException("Authentication failed: " + ex.getMessage());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        User user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail)
                        .orElseThrow(() -> {
                            log.error("User not found after successful authentication: {}", usernameOrEmail);
                            return new BadCredentialsException("User not found");
                        }));

        // Get user's registered email (required for finding user during verification)
        String userRegisteredEmail = user.getEmail();
        if (userRegisteredEmail == null || userRegisteredEmail.isBlank()) {
            log.error("User {} has no email address", user.getUsername());
            throw new BadCredentialsException("User email not found");
        }
        userRegisteredEmail = EmailValidator.normalizeEmail(userRegisteredEmail);

        // Send OTP to registered email
        log.info("Sending OTP to registered email: {} for user: {}", 
                userRegisteredEmail, user.getUsername());
        otpService.sendOtp(userRegisteredEmail, OtpType.LOGIN_2FA, userRegisteredEmail);
        return userRegisteredEmail; // Return the email address where OTP was sent
    }

    @Transactional
    public String authenticateAndIssueToken(String usernameOrEmail, String password) {
        log.info("Authenticating user: {} (Bypassing OTP)", usernameOrEmail);

        // Authenticate username/password
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(usernameOrEmail, password));

        UserDetails userDetails = userDetailsService.loadUserByUsername(usernameOrEmail);
        User user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .orElseGet(() -> userRepository.findByEmailIgnoreCase(usernameOrEmail)
                        .orElseThrow(() -> new BadCredentialsException("User not found")));

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String jwt = jwtService.generateToken(userDetails, claims);

        log.info("JWT Token issued directly for user: {}", user.getUsername());
        return jwt;
    }

    @Transactional
    public String verifyLoginOtpAndIssueToken(String email, String code) {
        // Validate and verify OTP (this marks it as used)
        // The email parameter is the email where OTP was sent (could be custom or registered)
        OtpToken token = otpService.validateOtp(email, code, OtpType.LOGIN_2FA);

        // Get user using the registered email from the token (not the OTP delivery email)
        String userEmail = token.getUserEmail();
        if (userEmail == null || userEmail.isBlank()) {
            // Fallback: try to find user by the OTP email (for backward compatibility)
            userEmail = EmailValidator.normalizeEmail(email);
            log.warn("OTP token missing userEmail, using OTP email as fallback: {}", userEmail);
        }
        
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());
        String jwt = jwtService.generateToken(userDetails, claims);

        log.info("OTP verified successfully for user: {} (OTP sent to: {}, registered email: {})", 
                user.getUsername(), email, userEmail);
        return jwt;
    }

    public void sendPasswordResetOtp(String email) {
        // Validate email format
        EmailValidator.validateEmail(email);
        String normalizedEmail = EmailValidator.normalizeEmail(email);

        if (!userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BadCredentialsException("Email not found");
        }

        otpService.sendOtp(normalizedEmail, OtpType.PASSWORD_RESET);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        // Validate and verify OTP (this marks it as used)
        otpService.validateOtp(email, code, OtpType.PASSWORD_RESET);

        // Reset password
        String normalizedEmail = EmailValidator.normalizeEmail(email);
        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Email not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password reset successfully for user: {}", normalizedEmail);
    }

    public boolean userExists(String usernameOrEmail) {
        return userRepository.findByUsernameIgnoreCase(usernameOrEmail).isPresent() ||
                userRepository.findByEmailIgnoreCase(usernameOrEmail).isPresent();
    }
}
