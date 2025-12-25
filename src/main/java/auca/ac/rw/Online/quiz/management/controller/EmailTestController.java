package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class EmailTestController {
    private static final Logger log = LoggerFactory.getLogger(EmailTestController.class);
    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/email")
    public ResponseEntity<?> testEmail(@RequestBody TestEmailRequest request) {
        try {
            log.info("=== Testing email sending ===");
            log.info("To: {}", request.email());
            
            String subject = "Test Email from Online Quiz System";
            String body = "This is a test email. If you receive this, your email configuration is working correctly!";
            
            emailService.sendOtp(request.email(), subject, body);
            
            return ResponseEntity.ok(new java.util.HashMap<String, String>() {
                {
                    put("status", "success");
                    put("message", "Test email sent successfully to " + request.email());
                }
            });
        } catch (Exception ex) {
            log.error("Test email failed: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(new java.util.HashMap<String, String>() {
                {
                    put("status", "error");
                    put("message", "Failed to send test email: " + ex.getMessage());
                }
            });
        }
    }

    record TestEmailRequest(String email) {}
}

