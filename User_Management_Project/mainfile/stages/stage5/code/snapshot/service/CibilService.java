package com.sit.loanapp.service;

import com.sit.loanapp.model.User;

/**
 * Service interface for PAN validation, CIBIL score generation, interest rate calculation,
 * and auto-generating credentials during CIBIL-backed onboarding (Stage 4).
 */
public interface CibilService {

    class CibilCalculatorResult {
        private final boolean success;
        private final String message;
        private final String pan;
        private final String loanType;
        private final int cibilScore;
        private final double interestRate;

        public CibilCalculatorResult(boolean success, String message, String pan, String loanType, int cibilScore, double interestRate) {
            this.success = success;
            this.message = message;
            this.pan = pan;
            this.loanType = loanType;
            this.cibilScore = cibilScore;
            this.interestRate = interestRate;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getPan() {
            return pan;
        }

        public String getLoanType() {
            return loanType;
        }

        public int getCibilScore() {
            return cibilScore;
        }

        public double getInterestRate() {
            return interestRate;
        }
    }

    class CibilOnboardingResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String generatedUsername;
        private final String generatedPassword;
        private final int cibilScore;

        public CibilOnboardingResult(boolean success, String message, User user,
                                     String generatedUsername, String generatedPassword, int cibilScore) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.generatedUsername = generatedUsername;
            this.generatedPassword = generatedPassword;
            this.cibilScore = cibilScore;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }

        public String getGeneratedUsername() {
            return generatedUsername;
        }

        public String getGeneratedPassword() {
            return generatedPassword;
        }

        public int getCibilScore() {
            return cibilScore;
        }
    }

    /**
     * Generates a random CIBIL score strictly between 600 and 900.
     */
    int generateCibilScore();

    /**
     * Calculates the interest rate based on loan type and CIBIL score.
     */
    double calculateInterestRate(String loanType, int cibilScore);

    /**
     * Validates PAN and calculates interest rate according to the Stage 4 specification.
     */
    CibilCalculatorResult calculate(String pan, String loanType);

    /**
     * Auto-generates username using the Stage 4 formula:
     * First 3 letters of First Name + First 3 letters of Last Name + Last 4 digits of Mobile.
     */
    String generateUsername(String fname, String lname, String mobile);

    /**
     * Auto-generates a secure 8-10 character password meeting complexity policy.
     */
    String generatePassword();

    /**
     * Validates applicant details, auto-generates credentials, dispatches credentials email,
     * generates CIBIL score, and persists user.
     */
    CibilOnboardingResult onboardWithCibil(String fname, String lname, String mobile,
                                          String email, String location, String pancard);
}
