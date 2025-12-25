package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.OtpToken;
import auca.ac.rw.Online.quiz.management.model.OtpType;
import auca.ac.rw.Online.quiz.management.repository.OtpTokenRepository;
import auca.ac.rw.Online.quiz.management.util.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for OTP generation, validation, and management.
 * Implements bank-grade OTP security:
 * - 6-digit numeric OTP
 * - 5-minute expiration
 * - Single-use (invalidated after verification)
 * - Prevents reuse
 */
@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    public OtpService(OtpTokenRepository otpTokenRepository, EmailService emailService) {
        this.otpTokenRepository = otpTokenRepository;
        this.emailService = emailService;
    }

    /**
     * Generates a secure 6-digit numeric OTP
     * @return 6-digit OTP code as string
     */
    public String generateOtp() {
        int otp = 100000 + secureRandom.nextInt(900000); // Range: 100000-999999
        return String.format("%06d", otp);
    }

    /**
     * Sends OTP to the specified email address
     * @param email Recipient email address (any valid domain)
     * @param type Type of OTP (LOGIN_2FA, PASSWORD_RESET, etc.)
     * @return The generated OTP code
     */
    @Transactional
    public String sendOtp(String email, OtpType type) {
        return sendOtp(email, type, null);
    }

    /**
     * Sends the same OTP code to multiple email addresses
     * @param emails List of recipient email addresses
     * @param type Type of OTP (LOGIN_2FA, PASSWORD_RESET, etc.)
     * @param userEmail User's registered email (for finding user during verification)
     * @return The generated OTP code
     */
    @Transactional
    public String sendOtpToMultipleEmails(List<String> emails, OtpType type, String userEmail) {
        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException("Email list cannot be empty");
        }
        
        // Normalize all emails
        List<String> normalizedEmails = emails.stream()
                .map(email -> {
                    EmailValidator.validateEmail(email);
                    return EmailValidator.normalizeEmail(email);
                })
                .distinct() // Remove duplicates
                .toList();
        
        String normalizedUserEmail = (userEmail != null && !userEmail.isBlank()) 
            ? EmailValidator.normalizeEmail(userEmail) 
            : normalizedEmails.get(0);
        
        // Invalidate existing OTPs for all emails
        for (String email : normalizedEmails) {
            invalidateExistingOtps(email, type);
        }
        
        // Generate a single OTP code for all emails
        String otpCode = generateOtp();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        
        // Create OTP tokens for each email with the same code
        for (String email : normalizedEmails) {
            OtpToken token = new OtpToken();
            token.setEmail(email);
            token.setUserEmail(normalizedUserEmail);
            token.setCode(otpCode);
            token.setExpiresAt(expiresAt);
            token.setType(type);
            token.setUsed(false);
            otpTokenRepository.save(token);
            log.info("OTP token created for {} (user: {}, type: {}), expires at {}", 
                    email, normalizedUserEmail, type, expiresAt);
        }
        
        // Send OTP email to all addresses
        String subject = getOtpSubject(type);
        String body = getOtpBody(otpCode, OTP_EXPIRATION_MINUTES);
        
        int successCount = 0;
        int failCount = 0;
        for (String email : normalizedEmails) {
            try {
                emailService.sendOtp(email, subject, body);
                log.info("OTP email sent successfully to {}", email);
                successCount++;
            } catch (Exception ex) {
                log.error("Failed to send OTP email to {}: {}", email, ex.getMessage());
                failCount++;
                // Continue sending to other emails even if one fails
            }
        }
        
        if (successCount == 0) {
            log.warn("=== OTP CODE FOR ALL EMAILS: {} (Expires in {} minutes) ===", 
                    otpCode, OTP_EXPIRATION_MINUTES);
            throw new RuntimeException("Failed to send OTP to any email address");
        }
        
        if (failCount > 0) {
            log.warn("OTP sent to {} email(s) successfully, but failed to send to {} email(s)", 
                    successCount, failCount);
        }
        
        return otpCode;
    }

    /**
     * Sends OTP to the specified email address
     * @param email Recipient email address (any valid domain)
     * @param type Type of OTP (LOGIN_2FA, PASSWORD_RESET, etc.)
     * @param userEmail User's registered email (for finding user during verification, optional)
     * @return The generated OTP code
     */
    @Transactional
    public String sendOtp(String email, OtpType type, String userEmail) {
        // Validate email format
        EmailValidator.validateEmail(email);
        String normalizedEmail = EmailValidator.normalizeEmail(email);
        
        // Normalize userEmail if provided
        String normalizedUserEmail = (userEmail != null && !userEmail.isBlank()) 
            ? EmailValidator.normalizeEmail(userEmail) 
            : normalizedEmail; // Default to recipient email if not provided
        
        // Invalidate any existing unused OTPs for this email and type
        invalidateExistingOtps(normalizedEmail, type);
        
        // Generate new OTP
        String otpCode = generateOtp();
        
        // Create and save OTP token
        OtpToken token = new OtpToken();
        token.setEmail(normalizedEmail); // Email where OTP is sent
        token.setUserEmail(normalizedUserEmail); // User's registered email for finding user
        token.setCode(otpCode);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES));
        token.setType(type);
        token.setUsed(false);
        
        otpTokenRepository.save(token);
        log.info("OTP token created for {} (user: {}, type: {}), expires at {}", 
                normalizedEmail, normalizedUserEmail, type, token.getExpiresAt());
        
        // Send OTP via email
        String subject = getOtpSubject(type);
        String body = getOtpBody(otpCode, OTP_EXPIRATION_MINUTES);
        
        try {
            emailService.sendOtp(normalizedEmail, subject, body);
            log.info("OTP email sent successfully to {}", normalizedEmail);
        } catch (Exception ex) {
            log.error("Failed to send OTP email to {}: {}", normalizedEmail, ex.getMessage());
            // Still log OTP for development/debugging
            log.warn("=== OTP CODE FOR {}: {} (Expires in {} minutes) ===", 
                    normalizedEmail, otpCode, OTP_EXPIRATION_MINUTES);
            throw ex; // Re-throw to let caller handle
        }
        
        return otpCode;
    }

    /**
     * Validates OTP code for the given email and type
     * @param email Email address
     * @param code OTP code to validate
     * @param type Type of OTP
     * @return The validated OTP token
     * @throws BadCredentialsException if OTP is invalid, expired, or already used
     */
    @Transactional
    public OtpToken validateOtp(String email, String code, OtpType type) {
        String normalizedEmail = EmailValidator.normalizeEmail(email);
        String trimmedCode = code.trim();
        
        // Find the most recent unused, non-expired OTP for this email and type
        OtpToken token = otpTokenRepository
                .findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByExpiresAtDesc(
                        normalizedEmail, type, LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn("OTP validation failed: No valid OTP found for {} (type: {})", normalizedEmail, type);
                    return new BadCredentialsException("OTP not found or expired. Please request a new OTP.");
                });

        // Verify OTP code matches
        if (!token.getCode().equals(trimmedCode)) {
            log.warn("OTP validation failed: Invalid code for {} (type: {})", normalizedEmail, type);
            throw new BadCredentialsException("Invalid OTP code. Please check and try again.");
        }

        // Mark this OTP and all related OTPs (same code, same user, same type) as used
        // This prevents reuse if OTP was sent to multiple emails
        String userEmail = token.getUserEmail();
        List<OtpToken> relatedTokens = otpTokenRepository.findAll().stream()
                .filter(t -> t.getType() == type)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .filter(t -> t.getCode().equals(trimmedCode))
                .filter(t -> userEmail != null && userEmail.equals(t.getUserEmail()))
                .toList();
        
        for (OtpToken relatedToken : relatedTokens) {
            relatedToken.setUsed(true);
            otpTokenRepository.save(relatedToken);
        }
        
        log.info("OTP validated and marked as used for {} (type: {}). Marked {} related OTP(s) as used.", 
                normalizedEmail, type, relatedTokens.size());
        
        return token;
    }

    /**
     * Invalidates all existing unused OTPs for the given email and type
     * This prevents multiple valid OTPs from existing simultaneously
     */
    private void invalidateExistingOtps(String email, OtpType type) {
        List<OtpToken> existingTokens = otpTokenRepository.findAll().stream()
                .filter(t -> t.getEmail().equalsIgnoreCase(email))
                .filter(t -> t.getType() == type)
                .filter(t -> !t.isUsed())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .toList();
        
        if (!existingTokens.isEmpty()) {
            log.debug("Invalidating {} existing OTP(s) for {} (type: {})", existingTokens.size(), email, type);
            existingTokens.forEach(token -> {
                token.setUsed(true);
                otpTokenRepository.save(token);
            });
        }
    }

    /**
     * Gets the email subject based on OTP type
     */
    private String getOtpSubject(OtpType type) {
        return switch (type) {
            case LOGIN_2FA -> "Your Login Verification Code";
            case PASSWORD_RESET -> "Your Password Reset Code";
            default -> "Your Verification Code";
        };
    }

    /**
     * Gets the email body with OTP code
     */
    private String getOtpBody(String otpCode, int expirationMinutes) {
        return String.format(
            "Your verification code is: %s\n\n" +
            "This code will expire in %d minutes.\n\n" +
            "If you didn't request this code, please ignore this email.",
            otpCode, expirationMinutes
        );
    }
}

