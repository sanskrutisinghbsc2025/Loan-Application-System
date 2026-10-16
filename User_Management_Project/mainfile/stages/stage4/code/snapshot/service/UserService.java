package com.sit.loanapp.service;

import com.sit.loanapp.model.User;

import java.util.Optional;

/**
 * Service interface managing user business logic:
 * - User Registration with validations
 * - User Authentication and Account Lockout tracking (3 failed attempts limit)
 * - Password Reset workflow
 */
public interface UserService {

    enum LoginStatus {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        ACCOUNT_ALREADY_LOCKED,
        USER_NOT_FOUND,
        ERROR
    }

    class RegistrationResult {
        private final boolean success;
        private final String message;

        public RegistrationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    class LoginResult {
        private final LoginStatus status;
        private final User user;
        private final String message;
        private final int remainingAttempts;

        public LoginResult(LoginStatus status, User user, String message, int remainingAttempts) {
            this.status = status;
            this.user = user;
            this.message = message;
            this.remainingAttempts = remainingAttempts;
        }

        public LoginStatus getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }

        public boolean isSuccess() {
            return status == LoginStatus.SUCCESS;
        }
    }

    class PasswordResetResult {
        private final boolean success;
        private final String message;

        public PasswordResetResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Registers a new user after running all mandatory and pattern validations.
     */
    RegistrationResult register(User user);

    /**
     * Authenticates a user using email/username and password.
     * Manages failed attempt counter and automatically locks account after 3 failed attempts.
     */
    LoginResult login(String identifier, String password);

    /**
     * Resets a user's password following identity validation and policy enforcement.
     */
    PasswordResetResult resetPassword(String identifier, String newPassword);

    /**
     * Retrieves user profile by email or username.
     */
    Optional<User> getUserByIdentifier(String identifier);
}
