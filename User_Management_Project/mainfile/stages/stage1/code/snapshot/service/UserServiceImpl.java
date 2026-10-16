package com.sit.loanapp.service;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.User;
import com.sit.loanapp.util.ValidationUtil;

import java.sql.Timestamp;
import java.util.Optional;

/**
 * Business logic implementation for user operations, credential validation,
 * failed attempt tracking, and automated account lockout rules.
 */
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private static final int MAX_FAILED_ATTEMPTS = 3;

    public UserServiceImpl() {
        this(new UserDaoImpl());
    }

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public RegistrationResult register(User user) {
        if (user == null) {
            return new RegistrationResult(false, "User data cannot be null.");
        }

        // 1. Mandatory Field Validations
        if (ValidationUtil.isBlank(user.getFname())) {
            return new RegistrationResult(false, "First name is mandatory.");
        }
        if (ValidationUtil.isBlank(user.getLname())) {
            return new RegistrationResult(false, "Last name is mandatory.");
        }
        if (ValidationUtil.isBlank(user.getMobile())) {
            return new RegistrationResult(false, "Mobile number is mandatory.");
        }
        if (ValidationUtil.isBlank(user.getEmail())) {
            return new RegistrationResult(false, "Email address is mandatory.");
        }
        if (ValidationUtil.isBlank(user.getUsername())) {
            return new RegistrationResult(false, "Username is mandatory.");
        }
        if (ValidationUtil.isBlank(user.getPassword())) {
            return new RegistrationResult(false, "Password is mandatory.");
        }

        // 2. Format Validations
        if (!ValidationUtil.isValidMobile(user.getMobile())) {
            return new RegistrationResult(false, "Invalid mobile number. Must be exactly 10 digits.");
        }
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            return new RegistrationResult(false, "Invalid email format. Please provide a valid email (e.g. user@domain.com).");
        }
        if (!ValidationUtil.isValidPassword(user.getPassword())) {
            return new RegistrationResult(false, "Password does not meet security requirements:\n" + 
                    ValidationUtil.getPasswordPolicyDescription());
        }

        // 3. Uniqueness Checks
        if (userDao.existsByUsername(user.getUsername())) {
            return new RegistrationResult(false, "Username '" + user.getUsername() + "' is already taken. Please choose another.");
        }
        if (userDao.existsByEmail(user.getEmail())) {
            return new RegistrationResult(false, "Email '" + user.getEmail() + "' is already registered. Please log in or use a different email.");
        }

        // 4. Set Defaults
        user.setStatus("Active");
        user.setFailedAttempts(0);
        user.setUserRole("USER");
        user.setLastModifiedDate(new Timestamp(System.currentTimeMillis()));

        // 5. Persist
        boolean saved = userDao.registerUser(user);
        if (saved) {
            return new RegistrationResult(true, "Registration successful! You can now log in.");
        } else {
            return new RegistrationResult(false, "Database error occurred while creating user. Please try again later.");
        }
    }

    @Override
    public LoginResult login(String identifier, String password) {
        if (ValidationUtil.isBlank(identifier) || ValidationUtil.isBlank(password)) {
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null, "Username/Email and Password cannot be empty.", 0);
        }

        Optional<User> userOpt = userDao.findByUsernameOrEmail(identifier);
        if (userOpt.isEmpty()) {
            return new LoginResult(LoginStatus.USER_NOT_FOUND, null, "User not found. Please register or check your credentials.", 0);
        }

        User user = userOpt.get();

        // Check if account is already locked/inactive
        if (!user.isActive() || user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            return new LoginResult(
                    LoginStatus.ACCOUNT_ALREADY_LOCKED,
                    user,
                    "Your account is locked and cannot be accessed. Please contact support to unlock your account.",
                    0
            );
        }

        // Validate Password
        if (password.equals(user.getPassword())) {
            // Success: Reset failed attempts to 0
            if (user.getFailedAttempts() > 0) {
                userDao.resetFailedAttempts(user.getId());
                user.setFailedAttempts(0);
            }
            String welcomeMsg = "Welcome " + user.getFname() + " " + user.getLname();
            return new LoginResult(LoginStatus.SUCCESS, user, welcomeMsg, MAX_FAILED_ATTEMPTS);
        } else {
            // Password mismatch: Increment failed attempts
            int currentAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(currentAttempts);

            if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
                // Lock the account immediately
                userDao.lockAccount(user.getId());
                user.setStatus("Inactive");
                return new LoginResult(
                        LoginStatus.ACCOUNT_LOCKED,
                        user,
                        "Your account is locked due to 3 consecutive failed login attempts. Please contact support.",
                        0
                );
            } else {
                userDao.incrementFailedAttempts(user.getId());
                int remaining = MAX_FAILED_ATTEMPTS - currentAttempts;
                return new LoginResult(
                        LoginStatus.INVALID_CREDENTIALS,
                        user,
                        "Invalid password! You have " + remaining + " attempt(s) remaining before your account is locked.",
                        remaining
                );
            }
        }
    }

    @Override
    public PasswordResetResult resetPassword(String identifier, String newPassword) {
        if (ValidationUtil.isBlank(identifier)) {
            return new PasswordResetResult(false, "Username or Email is required for password reset.");
        }

        Optional<User> userOpt = userDao.findByUsernameOrEmail(identifier);
        if (userOpt.isEmpty()) {
            return new PasswordResetResult(false, "No registered user found with the provided identifier: " + identifier);
        }

        User user = userOpt.get();

        // Locked account cannot reset password on their own; must contact support/admin
        if (!user.isActive()) {
            return new PasswordResetResult(false, "Account is locked or inactive. Password reset is not permitted. Please contact support.");
        }

        if (ValidationUtil.isBlank(newPassword)) {
            return new PasswordResetResult(false, "New password cannot be empty.");
        }

        if (!ValidationUtil.isValidPassword(newPassword)) {
            return new PasswordResetResult(false, "New password does not meet security requirements:\n" +
                    ValidationUtil.getPasswordPolicyDescription());
        }

        boolean updated = userDao.updatePassword(user.getId(), newPassword);
        if (updated) {
            return new PasswordResetResult(true, "Password successfully reset for " + user.getUsername() + ". You can now log in.");
        } else {
            return new PasswordResetResult(false, "Failed to update password in database. Please try again.");
        }
    }

    @Override
    public Optional<User> getUserByIdentifier(String identifier) {
        if (ValidationUtil.isBlank(identifier)) {
            return Optional.empty();
        }
        return userDao.findByUsernameOrEmail(identifier);
    }
}
