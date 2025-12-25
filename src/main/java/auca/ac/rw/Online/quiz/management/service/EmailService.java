package auca.ac.rw.Online.quiz.management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String mailUsername;
    
    @Value("${spring.mail.password:}")
    private String mailPassword;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @jakarta.annotation.PostConstruct
    public void initializeEmailConfiguration() {
        // Gmail app passwords sometimes have spaces - remove them FIRST
        if (mailPassword != null && mailPassword.contains(" ")) {
            log.info("Removing spaces from Gmail App Password (Gmail passwords should not have spaces)");
            mailPassword = mailPassword.replaceAll("\\s+", "");
            log.info("Password normalized (new length: {})", mailPassword.length());
        }
        
        // Log email configuration (after normalization)
        log.info("========================================");
        log.info("=== Email Configuration Check ===");
        log.info("========================================");
        log.info("Environment variables checked: MAIL_USERNAME, MAIL_PASSWORD");
        log.info("");
        
        boolean usernameValid = StringUtils.hasText(mailUsername) && 
                !mailUsername.equals("your_gmail_username") &&
                !mailUsername.equals("your-email@gmail.com");
        log.info("MAIL_USERNAME configured: {}", usernameValid ? "✓ YES" : "✗ NO");
        
        boolean passwordValid = StringUtils.hasText(mailPassword) && 
                !mailPassword.equals("your_gmail_app_password") &&
                !mailPassword.equals("your_gmail_app_password");
        log.info("MAIL_PASSWORD configured: {}", passwordValid ? "✓ YES" : "✗ NO");
        log.info("");
        
        if (usernameValid) {
            log.info("Email username: {} (masked)", maskEmail(mailUsername));
        } else {
            log.error("✗ Email username NOT configured!");
            log.error("Current value: {}", mailUsername != null ? maskEmail(mailUsername) : "NULL");
            log.error("");
            log.error("To fix: Set environment variable MAIL_USERNAME");
            log.error("Windows PowerShell: $env:MAIL_USERNAME='your-email@gmail.com'");
            log.error("Windows CMD: set MAIL_USERNAME=your-email@gmail.com");
        }
        
        if (passwordValid) {
            log.info("Email password: *** (configured, {} chars)", mailPassword.length());
            if (mailPassword.length() != 16 && !mailPassword.contains(" ")) {
                log.warn("⚠ Warning: Gmail App Password should be 16 characters (no spaces)");
                log.warn("If you copied it with spaces, remove them!");
            }
        } else {
            log.error("✗ Email password NOT configured!");
            log.error("Current value: {}", mailPassword != null && !mailPassword.isEmpty() ? 
                    "*** (length: " + mailPassword.length() + ")" : "NULL or EMPTY");
            log.error("");
            log.error("To fix: Set environment variable MAIL_PASSWORD");
            log.error("Windows PowerShell: $env:MAIL_PASSWORD='your-16-char-app-password'");
            log.error("Windows CMD: set MAIL_PASSWORD=your-16-char-app-password");
            log.error("");
            log.error("How to get Gmail App Password:");
            log.error("1. Enable 2-Step Verification: https://myaccount.google.com/security");
            log.error("2. Generate App Password: https://myaccount.google.com/apppasswords");
            log.error("3. Select 'Mail' and 'Windows Computer'");
            log.error("4. Copy the 16-character password (remove spaces if any)");
        }
        log.info("");
        log.info("Email configuration status: {}", (usernameValid && passwordValid) ? "✓ READY" : "✗ NOT READY");
        log.info("========================================");
    }
    
    private String maskEmail(String email) {
        if (email == null || email.length() <= 3) {
            return "***";
        }
        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, Math.min(2, atIndex)) + "***@" + 
                   (atIndex < email.length() - 1 ? email.substring(atIndex + 1) : "");
        }
        return email.substring(0, 2) + "***";
    }

    public void sendOtp(String to, String subject, String body) {
        // Normalize password again (in case it wasn't done in PostConstruct)
        if (mailPassword != null && mailPassword.contains(" ")) {
            log.info("Removing spaces from Gmail App Password at send time");
            mailPassword = mailPassword.replaceAll("\\s+", "");
        }
        
        // Validate email configuration
        boolean usernameValid = StringUtils.hasText(mailUsername) && 
                               !mailUsername.equals("your_gmail_username") &&
                               !mailUsername.equals("your-email@gmail.com");
        boolean passwordValid = StringUtils.hasText(mailPassword) && 
                               !mailPassword.equals("your_gmail_app_password") &&
                               !mailPassword.equals("your_gmail_app_password");
        
        if (!usernameValid || !passwordValid) {
            log.error("=== Email Configuration Error ===");
            log.error("EMAIL_USERNAME/MAIL_USERNAME status: {}", usernameValid ? "CONFIGURED" : "NOT CONFIGURED");
            log.error("EMAIL_PASSWORD/MAIL_PASSWORD status: {}", passwordValid ? "CONFIGURED" : "NOT CONFIGURED");
            log.error("Current username value: {}", mailUsername != null && !mailUsername.isEmpty() ? 
                     maskEmail(mailUsername) : "EMPTY or NULL");
            log.error("Current password value: {}", mailPassword != null && !mailPassword.isEmpty() ? 
                     "*** (length: " + mailPassword.length() + ")" : "EMPTY or NULL");
            log.error("=================================");
            log.error("Please set environment variables (either EMAIL_* or MAIL_*):");
            log.error("Windows System Properties:");
            log.error("  - EMAIL_USERNAME = your-email@gmail.com");
            log.error("  - EMAIL_PASSWORD = your-app-password");
            log.error("OR");
            log.error("  - MAIL_USERNAME = your-email@gmail.com");
            log.error("  - MAIL_PASSWORD = your-app-password");
            log.error("Windows PowerShell: $env:EMAIL_USERNAME='your-email@gmail.com'");
            log.error("Windows PowerShell: $env:EMAIL_PASSWORD='your-app-password'");
            log.error("Windows CMD: set EMAIL_USERNAME=your-email@gmail.com");
            log.error("Windows CMD: set EMAIL_PASSWORD=your-app-password");
            log.error("Then RESTART your Spring Boot application");
            log.error("For Gmail: Get App Password from https://myaccount.google.com/apppasswords");

            // Extract and log OTP code for development
            String otpCode = extractOtpCode(body);
            log.warn("=== EMAIL NOT CONFIGURED - OTP FOR {}: {} ===", to, otpCode);
            log.warn("=== Please configure email settings to receive OTP via email ===");

            // Do NOT throw in development mode; continue so authentication can proceed and OTP remains in DB
            return;
        }
        
        // Validate email format (any valid domain)
        if (to == null || to.isBlank()) {
            log.error("Invalid email address: email is null or blank");
            throw new IllegalArgumentException("Email address is required");
        }
        
        String normalizedEmail = to.trim().toLowerCase();
        if (!normalizedEmail.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            log.error("Invalid email format: {}", to);
            throw new IllegalArgumentException("Invalid email format");
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailUsername); // Set from address
            message.setTo(to.toLowerCase().trim()); // Normalize email
            message.setSubject(subject);
            message.setText(body);
            
            log.info("=== Attempting to send OTP email ===");
            log.info("From: {}", maskEmail(mailUsername));
            log.info("To: {}", to);
            log.info("Subject: {}", subject);
            log.info("SMTP Host: smtp.gmail.com");
            log.info("SMTP Port: 587");
            
            mailSender.send(message);
            log.info("✓✓✓ OTP email sent successfully to {} ✓✓✓", to);
        } catch (org.springframework.mail.MailAuthenticationException ex) {
            log.error("========================================");
            log.error("❌ EMAIL AUTHENTICATION FAILED ❌");
            log.error("========================================");
            log.error("Error: {}", ex.getMessage());
            log.error("Full exception:", ex);
            log.error("");
            log.error("Common causes:");
            log.error("1. Incorrect Gmail App Password");
            log.error("2. App Password not generated (use: https://myaccount.google.com/apppasswords)");
            log.error("3. 2-Step Verification not enabled on Gmail account");
            log.error("4. Wrong MAIL_USERNAME or MAIL_PASSWORD environment variable");
            log.error("");
            log.error("Current configuration:");
            log.error("  MAIL_USERNAME: {}", maskEmail(mailUsername));
            log.error("  MAIL_PASSWORD: {} (length: {})", 
                     mailPassword != null && !mailPassword.isEmpty() ? "***SET***" : "NOT SET", 
                     mailPassword != null ? mailPassword.length() : 0);
            log.error("");
            String otpCode = extractOtpCode(body);
            log.error("=== EMAIL AUTH FAILED - OTP FOR {}: {} ===", to, otpCode);
            log.error("========================================");
            // Re-throw to let caller know email failed
            throw new RuntimeException("Email authentication failed. Please check your Gmail App Password configuration. OTP code: " + otpCode, ex);
        } catch (org.springframework.mail.MailSendException ex) {
            log.error("========================================");
            log.error("❌ EMAIL SEND FAILED ❌");
            log.error("========================================");
            log.error("Error: {}", ex.getMessage());
            log.error("Full exception:", ex);
            log.error("");
            log.error("Common causes:");
            log.error("1. Network connectivity issues");
            log.error("2. Gmail blocking the connection");
            log.error("3. Firewall blocking SMTP port 587");
            log.error("4. Invalid recipient email address");
            log.error("");
            String otpCode = extractOtpCode(body);
            log.error("=== EMAIL SEND FAILED - OTP FOR {}: {} ===", to, otpCode);
            log.error("========================================");
            // Re-throw to let caller know email failed
            throw new RuntimeException("Failed to send email. OTP code: " + otpCode, ex);
        } catch (Exception ex) {
            log.error("========================================");
            log.error("❌ UNEXPECTED EMAIL ERROR ❌");
            log.error("========================================");
            log.error("Error: {}", ex.getMessage());
            log.error("Full exception:", ex);
            log.error("");
            String otpCode = extractOtpCode(body);
            log.error("=== EMAIL ERROR - OTP FOR {}: {} ===", to, otpCode);
            log.error("========================================");
            // Re-throw to let caller know email failed
            throw new RuntimeException("Unexpected error sending email. OTP code: " + otpCode, ex);
        }
    }
    
    private String extractOtpCode(String body) {
        if (body == null) {
            return "NOT_FOUND";
        }
        
        try {
            if (body.contains("code is:")) {
                int startIdx = body.indexOf("code is:") + 9;
                int endIdx = Math.min(startIdx + 6, body.length());
                return body.substring(startIdx, endIdx).trim();
            }
            // Fallback: try to find 6-digit number in body
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
            java.util.regex.Matcher matcher = pattern.matcher(body);
            if (matcher.find()) {
                return matcher.group();
            }
        } catch (Exception e) {
            log.debug("Could not extract OTP code from body", e);
        }
        return "NOT_FOUND";
    }
}

