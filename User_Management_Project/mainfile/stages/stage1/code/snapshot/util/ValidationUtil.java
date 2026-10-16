package com.sit.loanapp.util;

import java.util.regex.Pattern;

/**
 * Utility class for enforcing Stage 1 validation rules:
 * - Mandatory field presence
 * - Email format validation (RFC-compliant regex)
 * - Mobile number validation (strictly 10 digits)
 * - Password complexity policy (>=8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character)
 */
public class ValidationUtil {

    // Regex for standard email format
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex for exactly 10 digits mobile number
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Regex patterns for password complexity components
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]");

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidMobile(String mobile) {
        if (isBlank(mobile)) {
            return false;
        }
        return MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    /**
     * Validates password against the Stage 1 specification:
     * - Minimum 8 characters long
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return UPPERCASE_PATTERN.matcher(password).find()
                && LOWERCASE_PATTERN.matcher(password).find()
                && DIGIT_PATTERN.matcher(password).find()
                && SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

    /**
     * Returns detailed feedback on why a password might fail validation.
     */
    public static String getPasswordPolicyDescription() {
        return "Password must be at least 8 characters long and contain:\n" +
               " - At least one uppercase letter (A-Z)\n" +
               " - At least one lowercase letter (a-z)\n" +
               " - At least one digit (0-9)\n" +
               " - At least one special character (!@#$%^&*...)";
    }

    public static boolean isValidUsername(String username) {
        if (isBlank(username)) {
            return false;
        }
        String trimmed = username.trim();
        // Alphanumeric with optional underscore/hyphen, at least 3 chars
        return trimmed.length() >= 3 && trimmed.matches("^[a-zA-Z0-9_.-]+$");
    }
}
