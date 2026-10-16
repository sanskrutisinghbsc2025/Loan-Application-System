package com.sit.loanapp.service;

import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for administrative management of users (Stage 2).
 */
public interface AdminService {

    class OperationResult {
        private final boolean success;
        private final String message;

        public OperationResult(boolean success, String message) {
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
     * Searches users by field criteria (fname, lname, email, mobile, username, status, or all).
     */
    List<User> searchUsers(String criteria, String term);

    /**
     * Retrieves all users.
     */
    List<User> getAllUsers();

    /**
     * Retrieves single user details by ID.
     */
    Optional<User> getUserById(int userId);

    /**
     * Activates a user account, unlocks them if previously locked, sends email notification, and records audit log.
     */
    OperationResult activateUser(User admin, int targetUserId);

    /**
     * Deactivates a user account, sends email notification, and records audit log.
     */
    OperationResult deactivateUser(User admin, int targetUserId);

    /**
     * Edits user details, validates inputs, sends email notification, and records audit log.
     */
    OperationResult editUser(User admin, User updatedUser);

    /**
     * Deletes user account permanently, sends final email notification, and records audit log.
     */
    OperationResult deleteUser(User admin, int targetUserId);

    /**
     * Retrieves recent audit logs.
     */
    List<AuditLog> getRecentAuditLogs(int limit);
}
