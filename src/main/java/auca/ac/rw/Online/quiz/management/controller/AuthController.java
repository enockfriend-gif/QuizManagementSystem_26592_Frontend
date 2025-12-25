package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    // Request/Response records - defined as nested classes for Spring compatibility
    record LoginRequest(String usernameOrEmail, String password) {
    }

    record OtpVerifyRequest(String email, String code) {
    }

    record ResetRequest(String email) {
    }

    record ResetConfirmRequest(String email, String code, String newPassword) {
    }

    record TokenResponse(String token) {
    }

    record EmailResponse(String email) {
    }

    record ErrorResponse(String message) {
    }

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            log.info("Login attempt for: {} - Initiating 2FA OTP flow", request.usernameOrEmail());
            // Enforce two-factor authentication: authenticate credentials and send OTP
            String userEmail = authService.initiateLoginOtp(
                request.usernameOrEmail(), 
                request.password()
            );
            log.info("OTP sent successfully to email: {} for user: {}", userEmail, request.usernameOrEmail());
            // Return email address so frontend can redirect to OTP verification page
            return ResponseEntity.ok(new EmailResponse(userEmail));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed for {}: {}", request.usernameOrEmail(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error during login for {}: {}", request.usernameOrEmail(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verify(@RequestBody OtpVerifyRequest request) {
        try {
            log.info("OTP verification attempt for: {}", request.email());
            String token = authService.verifyLoginOtpAndIssueToken(request.email(), request.code());
            log.info("OTP verified successfully for: {}", request.email());
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (BadCredentialsException ex) {
            log.warn("OTP verification failed for {}: {}", request.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error during OTP verification for {}: {}", request.email(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("OTP verification failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/reset/request")
    public ResponseEntity<?> resetRequest(@RequestBody ResetRequest request) {
        try {
            log.info("Password reset request for email: {}", request.email());
            authService.sendPasswordResetOtp(request.email());
            log.info("Password reset OTP sent successfully to: {}", request.email());
            return ResponseEntity.accepted().build();
        } catch (BadCredentialsException ex) {
            log.warn("Password reset request failed for {}: {}", request.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid email format for password reset: {}", request.email());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid email format"));
        } catch (Exception ex) {
            log.error("Unexpected error during password reset request for {}: {}", request.email(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to send password reset email. Please try again later."));
        }
    }

    @PostMapping("/reset/confirm")
    public ResponseEntity<?> resetConfirm(@RequestBody ResetConfirmRequest request) {
        try {
            authService.resetPassword(request.email(), request.code(), request.newPassword());
            // Return 200 OK with success message instead of 204 for better frontend handling
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {
                {
                    put("message", "Password reset successfully");
                    put("status", "success");
                }
            });
        } catch (BadCredentialsException ex) {
            log.warn("Password reset failed for {}: {}", request.email(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error during password reset for {}: {}", request.email(), ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Password reset failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/check-user/{usernameOrEmail}")
    public ResponseEntity<?> checkUser(@PathVariable String usernameOrEmail) {
        try {
            boolean exists = authService.userExists(usernameOrEmail);
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {
                {
                    put("exists", exists);
                    put("usernameOrEmail", usernameOrEmail);
                }
            });
        } catch (Exception ex) {
            log.error("Error checking user {}: {}", usernameOrEmail, ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error checking user: " + ex.getMessage()));
        }
    }
}
