package com.sit.loanapp.dao;

import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity persistence operations and audit logging.
 */
public interface UserDao {

    /**
     * Checks if a given username already exists in the database.
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a given email already exists in the database.
     */
    boolean existsByEmail(String email);

    /**
     * Persists a new user record into the database.
     */
    boolean registerUser(User user);

    /**
     * Finds a user by their username OR email address.
     */
    Optional<User> findByUsernameOrEmail(String identifier);

    /**
     * Finds a user by their primary key ID.
     */
    Optional<User> findById(int id);

    /**
     * Increments the user's failed_attempts counter by 1.
     */
    boolean incrementFailedAttempts(int userId);

    /**
     * Resets the user's failed_attempts counter to 0 upon successful login.
     */
    boolean resetFailedAttempts(int userId);

    /**
     * Locks the account after 3 failed attempts:
     * sets status='Inactive', failed_attempts=3, and lastmodifieddate=NOW().
     */
    boolean lockAccount(int userId);

    /**
     * Unlocks an account: sets status='Active', failed_attempts=0, and lastmodifieddate=NOW().
     */
    boolean unlockAccount(int userId);

    /**
     * Updates the user's password and sets lastmodifieddate=NOW().
     */
    boolean updatePassword(int userId, String newPassword);

    // -------------------------------------------------------------------------
    // Stage 2: Admin Operations
    // -------------------------------------------------------------------------

    /**
     * Searches users matching the specified search criteria.
     * Criteria can be: fname, lname, email, mobile, username, status, or all.
     */
    List<User> searchUsers(String criteria, String term);

    /**
     * Retrieves all registered users from the system.
     */
    List<User> findAllUsers();

    /**
     * Updates editable user information: fname, lname, mobile, email, location.
     */
    boolean updateUser(User user);

    /**
     * Sets the status of a user ('Active' or 'Inactive') and updates lastmodifieddate.
     */
    boolean setUserStatus(int userId, String status);

    /**
     * Deletes a user record permanently from the database.
     */
    boolean deleteUser(int userId);

    /**
     * Persists an audit log record into the database.
     */
    boolean logAudit(AuditLog log);

    /**
     * Retrieves recent audit logs.
     */
    List<AuditLog> getRecentAuditLogs(int limit);
}
