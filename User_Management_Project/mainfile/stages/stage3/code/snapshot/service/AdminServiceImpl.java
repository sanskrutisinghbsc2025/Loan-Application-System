package com.sit.loanapp.service;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;
import com.sit.loanapp.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of AdminService for user management, role-based access checks,
 * email notifications, and compliance audit logging.
 */
public class AdminServiceImpl implements AdminService {

    private final UserDao userDao;
    private final NotificationService notificationService;

    public AdminServiceImpl() {
        this(new UserDaoImpl(), new ConsoleNotificationService());
    }

    public AdminServiceImpl(UserDao userDao, NotificationService notificationService) {
        this.userDao = userDao;
        this.notificationService = notificationService;
    }

    private boolean verifyAdminRole(User admin) {
        return admin != null && admin.isAdmin();
    }

    @Override
    public List<User> searchUsers(String criteria, String term) {
        return userDao.searchUsers(criteria, term);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }

    @Override
    public Optional<User> getUserById(int userId) {
        return userDao.findById(userId);
    }

    @Override
    public OperationResult activateUser(User admin, int targetUserId) {
        if (!verifyAdminRole(admin)) {
            return new OperationResult(false, "Access Denied: Only administrators can activate users.");
        }

        Optional<User> targetOpt = userDao.findById(targetUserId);
        if (targetOpt.isEmpty()) {
            return new OperationResult(false, "Target user not found (ID: " + targetUserId + ").");
        }

        User target = targetOpt.get();
        boolean updated = userDao.setUserStatus(targetUserId, "Active");
        if (updated) {
            target.setStatus("Active");
            target.setFailedAttempts(0);

            // Send notification email
            notificationService.sendActivationNotification(target);

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "ACTIVATE_USER",
                    target.getId(),
                    target.getUsername(),
                    "User account activated and failed login attempts reset to 0."
            ));

            return new OperationResult(true, "User '" + target.getUsername() + "' has been successfully activated.");
        }
        return new OperationResult(false, "Database update failed while activating user.");
    }

    @Override
    public OperationResult deactivateUser(User admin, int targetUserId) {
        if (!verifyAdminRole(admin)) {
            return new OperationResult(false, "Access Denied: Only administrators can deactivate users.");
        }

        if (admin.getId() == targetUserId) {
            return new OperationResult(false, "Safety Guard: You cannot deactivate your own administrative account.");
        }

        Optional<User> targetOpt = userDao.findById(targetUserId);
        if (targetOpt.isEmpty()) {
            return new OperationResult(false, "Target user not found (ID: " + targetUserId + ").");
        }

        User target = targetOpt.get();
        boolean updated = userDao.setUserStatus(targetUserId, "Inactive");
        if (updated) {
            target.setStatus("Inactive");

            // Send notification email
            notificationService.sendDeactivationNotification(target);

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "DEACTIVATE_USER",
                    target.getId(),
                    target.getUsername(),
                    "User account deactivated by administrator."
            ));

            return new OperationResult(true, "User '" + target.getUsername() + "' has been successfully deactivated.");
        }
        return new OperationResult(false, "Database update failed while deactivating user.");
    }

    @Override
    public OperationResult editUser(User admin, User updatedUser) {
        if (!verifyAdminRole(admin)) {
            return new OperationResult(false, "Access Denied: Only administrators can edit user information.");
        }

        if (updatedUser == null) {
            return new OperationResult(false, "User data cannot be null.");
        }

        Optional<User> existingOpt = userDao.findById(updatedUser.getId());
        if (existingOpt.isEmpty()) {
            return new OperationResult(false, "User not found (ID: " + updatedUser.getId() + ").");
        }

        // Validate mandatory fields
        if (ValidationUtil.isBlank(updatedUser.getFname())) {
            return new OperationResult(false, "First name is mandatory.");
        }
        if (ValidationUtil.isBlank(updatedUser.getLname())) {
            return new OperationResult(false, "Last name is mandatory.");
        }
        if (ValidationUtil.isBlank(updatedUser.getMobile())) {
            return new OperationResult(false, "Mobile number is mandatory.");
        }
        if (ValidationUtil.isBlank(updatedUser.getEmail())) {
            return new OperationResult(false, "Email address is mandatory.");
        }

        // Validate formats
        if (!ValidationUtil.isValidMobile(updatedUser.getMobile())) {
            return new OperationResult(false, "Mobile number must be exactly 10 digits.");
        }
        if (!ValidationUtil.isValidEmail(updatedUser.getEmail())) {
            return new OperationResult(false, "Invalid email address format.");
        }

        // Email uniqueness if changed
        User existing = existingOpt.get();
        if (!existing.getEmail().equalsIgnoreCase(updatedUser.getEmail()) && userDao.existsByEmail(updatedUser.getEmail())) {
            return new OperationResult(false, "Email '" + updatedUser.getEmail() + "' is already registered to another user.");
        }

        boolean updated = userDao.updateUser(updatedUser);
        if (updated) {
            // Send notification email
            notificationService.sendEditNotification(updatedUser);

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "EDIT_USER",
                    updatedUser.getId(),
                    existing.getUsername(),
                    String.format("Updated user details: Name=%s, Mobile=%s, Email=%s, Location=%s",
                            updatedUser.getFullName(), updatedUser.getMobile(), updatedUser.getEmail(), updatedUser.getLocation())
            ));

            return new OperationResult(true, "User details updated successfully for '" + existing.getUsername() + "'.");
        }
        return new OperationResult(false, "Database update failed while saving user edits.");
    }

    @Override
    public OperationResult deleteUser(User admin, int targetUserId) {
        if (!verifyAdminRole(admin)) {
            return new OperationResult(false, "Access Denied: Only administrators can delete users.");
        }

        if (admin.getId() == targetUserId) {
            return new OperationResult(false, "Safety Guard: You cannot delete your own administrative account.");
        }

        Optional<User> targetOpt = userDao.findById(targetUserId);
        if (targetOpt.isEmpty()) {
            return new OperationResult(false, "Target user not found (ID: " + targetUserId + ").");
        }

        User target = targetOpt.get();
        boolean deleted = userDao.deleteUser(targetUserId);
        if (deleted) {
            // Send notification email
            notificationService.sendDeletionNotification(target);

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "DELETE_USER",
                    target.getId(),
                    target.getUsername(),
                    "User account permanently deleted by administrator."
            ));

            return new OperationResult(true, "User '" + target.getUsername() + "' (ID: " + targetUserId + ") was permanently deleted.");
        }
        return new OperationResult(false, "Database error occurred while deleting user.");
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return userDao.getRecentAuditLogs(limit);
    }
}
