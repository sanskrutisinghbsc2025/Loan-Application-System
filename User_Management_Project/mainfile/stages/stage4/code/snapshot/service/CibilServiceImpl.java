package com.sit.loanapp.service;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.User;
import com.sit.loanapp.util.ValidationUtil;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Random;

/**
 * Implementation of CibilService managing PAN validation, CIBIL score generation,
 * interest rate adjustments, auto-generated credentials, and onboarding.
 */
public class CibilServiceImpl implements CibilService {

    private final UserDao userDao;
    private final NotificationService notificationService;
    private final Random random;

    public CibilServiceImpl() {
        this(new UserDaoImpl(), new ConsoleNotificationService(), new SecureRandom());
    }

    public CibilServiceImpl(UserDao userDao, NotificationService notificationService, Random random) {
        this.userDao = userDao;
        this.notificationService = notificationService;
        this.random = random;
    }

    @Override
    public int generateCibilScore() {
        // Generates random integer strictly between 600 and 900 inclusive
        return 600 + random.nextInt(301);
    }

    @Override
    public double calculateInterestRate(String loanType, int cibilScore) {
        int clampedCibil = Math.max(600, Math.min(900, cibilScore));
        double factor = (900.0 - clampedCibil) / 300.0; // 0.0 at 900, 1.0 at 600

        double minRate;
        double maxRate;

        String normalized = (loanType != null) ? loanType.trim().toUpperCase() : "";
        if (normalized.contains("HOME")) {
            minRate = 6.5;
            maxRate = 8.5;
        } else if (normalized.contains("PERSONAL")) {
            minRate = 9.0;
            maxRate = 12.0;
        } else if (normalized.contains("EDUCATION")) {
            minRate = 4.0;
            maxRate = 6.0;
        } else if (normalized.contains("VEHICLE") || normalized.contains("CAR")) {
            minRate = 7.0;
            maxRate = 9.0;
        } else {
            minRate = 8.0;
            maxRate = 10.0;
        }

        double calculated = minRate + factor * (maxRate - minRate);
        return Math.round(calculated * 10.0) / 10.0;
    }

    @Override
    public CibilCalculatorResult calculate(String pan, String loanType) {
        // 1. PAN Validation
        if (!ValidationUtil.isValidPan(pan)) {
            return new CibilCalculatorResult(false, "Invalid PAN format. Please enter a valid PAN.", pan, loanType, 0, 0.0);
        }

        // 2. Loan Type Validation
        if (!ValidationUtil.isValidLoanType(loanType)) {
            return new CibilCalculatorResult(false, "Invalid loan type selected.", pan, loanType, 0, 0.0);
        }

        String formattedPan = pan.trim().toUpperCase();
        String normalizedType = normalizeLoanType(loanType);

        int cibilScore = generateCibilScore();
        double rate = calculateInterestRate(normalizedType, cibilScore);

        String message = String.format("For PAN %s, with a CIBIL score of %d, the interest rate for %s is %.1f%%.",
                formattedPan, cibilScore, normalizedType, rate);

        return new CibilCalculatorResult(true, message, formattedPan, normalizedType, cibilScore, rate);
    }

    @Override
    public String generateUsername(String fname, String lname, String mobile) {
        String cleanFname = (fname != null) ? fname.trim() : "";
        String cleanLname = (lname != null) ? lname.trim() : "";
        String cleanMobile = (mobile != null) ? mobile.trim() : "";

        String fPart = cleanFname.length() >= 3 ? cleanFname.substring(0, 3) : cleanFname;
        String lPart = cleanLname.length() >= 3 ? cleanLname.substring(0, 3) : cleanLname;
        String mPart = cleanMobile.length() >= 4 ? cleanMobile.substring(cleanMobile.length() - 4) : cleanMobile;

        // Capitalize first letter of each part to match example: Shivani + Chaudhari + 9876543210 -> ShiCha3210
        String fCap = fPart.isEmpty() ? "Usr" : Character.toUpperCase(fPart.charAt(0)) + fPart.substring(1).toLowerCase();
        String lCap = lPart.isEmpty() ? "App" : Character.toUpperCase(lPart.charAt(0)) + lPart.substring(1).toLowerCase();

        return fCap + lCap + mPart;
    }

    @Override
    public String generatePassword() {
        // Generates an 8-10 char password containing Letters, Numbers, and Symbols
        // Example matching specification: "@Shiv!123"
        String[] prefixes = {"@Loan", "@Shiv", "@Secure", "@Bank", "@Smart"};
        String prefix = prefixes[random.nextInt(prefixes.length)];
        int digits = 100 + random.nextInt(900);
        return prefix + "!" + digits;
    }

    @Override
    public CibilOnboardingResult onboardWithCibil(String fname, String lname, String mobile,
                                                 String email, String location, String pancard) {
        // Validations
        if (ValidationUtil.isBlank(fname) || !ValidationUtil.isValidNameLettersOnly(fname)) {
            return new CibilOnboardingResult(false, "First Name is required and must contain only letters.", null, null, null, 0);
        }
        if (ValidationUtil.isBlank(lname) || !ValidationUtil.isValidNameLettersOnly(lname)) {
            return new CibilOnboardingResult(false, "Last Name is required and must contain only letters.", null, null, null, 0);
        }
        if (!ValidationUtil.isValidMobile(mobile)) {
            return new CibilOnboardingResult(false, "Mobile Number is required and must contain 10 digits.", null, null, null, 0);
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return new CibilOnboardingResult(false, "Email Address is required and must follow a valid email format.", null, null, null, 0);
        }
        if (ValidationUtil.isBlank(location)) {
            return new CibilOnboardingResult(false, "Location is required.", null, null, null, 0);
        }
        if (!ValidationUtil.isValidPan(pancard)) {
            return new CibilOnboardingResult(false, "Invalid PAN format. Please enter a valid PAN.", null, null, null, 0);
        }

        // Check email uniqueness
        if (userDao.existsByEmail(email)) {
            return new CibilOnboardingResult(false, "Email '" + email + "' is already registered.", null, null, null, 0);
        }

        // Generate credentials
        String username = generateUsername(fname, lname, mobile);
        // Ensure username uniqueness
        if (userDao.existsByUsername(username)) {
            username = username + (random.nextInt(90) + 10);
        }
        String password = generatePassword();
        int cibilScore = generateCibilScore();

        // Create user
        User user = new User(fname.trim(), lname.trim(), mobile.trim(), email.trim(), location.trim(), username, password);
        user.setPancard(pancard.trim().toUpperCase());
        user.setCibilScore(cibilScore);
        user.setStatus("Active");
        user.setUserRole(User.ROLE_USER);
        user.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

        boolean saved = userDao.registerUser(user);
        if (saved) {
            // Send actual email notification containing generated credentials
            notificationService.sendCredentialsNotification(email.trim(), fname.trim(), username, password);

            String message = String.format("Credentials generated successfully! Username: %s | CIBIL Score: %d", username, cibilScore);
            return new CibilOnboardingResult(true, message, user, username, password, cibilScore);
        }
        return new CibilOnboardingResult(false, "Database error during user onboarding.", null, null, null, 0);
    }

    private String normalizeLoanType(String loanType) {
        String upper = (loanType != null) ? loanType.trim().toUpperCase() : "";
        if (upper.contains("HOME")) return "Home Loan";
        if (upper.contains("PERSONAL")) return "Personal Loan";
        if (upper.contains("EDUCATION")) return "Education Loan";
        if (upper.contains("VEHICLE") || upper.contains("CAR")) return "Vehicle Loan";
        return loanType;
    }
}
