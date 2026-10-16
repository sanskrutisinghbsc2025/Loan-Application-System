package com.sit.loanapp.util;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for enforcing validation rules across all stages:
 * - Stage 1: Mandatory fields, email regex, 10-digit mobile, password policy
 * - Stage 4: PAN card validation, letters-only name checks, loan type validation
 */
public class ValidationUtil {

    // Regex for standard email format
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Regex for exactly 10 digits mobile number
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");

    // Regex for PAN card: 5 uppercase letters, 4 digits, 1 uppercase letter
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    // Regex for letters only (First Name, Last Name)
    private static final Pattern LETTERS_ONLY_PATTERN = Pattern.compile("^[a-zA-Z]+$");

    // Regex patterns for password complexity components
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]");

    // Valid Loan Types
    private static final Set<String> VALID_LOAN_TYPES = Set.of(
            "HOME LOAN",
            "PERSONAL LOAN",
            "EDUCATION LOAN",
            "VEHICLE LOAN",
            "CAR LOAN"
    );

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

    public static boolean isValidPan(String pan) {
        if (isBlank(pan)) {
            return false;
        }
        return PAN_PATTERN.matcher(pan.trim().toUpperCase()).matches();
    }

    public static boolean isValidNameLettersOnly(String name) {
        if (isBlank(name)) {
            return false;
        }
        return LETTERS_ONLY_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidLoanType(String loanType) {
        if (isBlank(loanType)) {
            return false;
        }
        return VALID_LOAN_TYPES.contains(loanType.trim().toUpperCase());
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        return UPPERCASE_PATTERN.matcher(password).find()
                && LOWERCASE_PATTERN.matcher(password).find()
                && DIGIT_PATTERN.matcher(password).find()
                && SPECIAL_CHAR_PATTERN.matcher(password).find();
    }

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
        return trimmed.length() >= 3 && trimmed.matches("^[a-zA-Z0-9_.-]+$");
    }
}
