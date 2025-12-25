package auca.ac.rw.Online.quiz.management.util;

import java.util.regex.Pattern;

public class EmailValidator {

    // RFC 5322 compliant email regex pattern
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);

    /**
     * Validates if an email address is valid (any domain)
     * 
     * @param email The email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return pattern.matcher(email.trim()).matches();
    }

    /**
     * Validates email format (accepts any valid email domain)
     * 
     * @param email The email address to validate
     * @throws IllegalArgumentException if email is not valid
     */
    public static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        String trimmedEmail = email.trim();

        if (!isValidEmail(trimmedEmail)) {
            throw new IllegalArgumentException(
                    "Invalid email format. Please provide a valid email address. Provided: " + email);
        }
    }

    /**
     * Normalizes email address (lowercase, trimmed)
     * 
     * @param email The email address to normalize
     * @return normalized email address
     */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        return email.trim().toLowerCase();
    }
}
