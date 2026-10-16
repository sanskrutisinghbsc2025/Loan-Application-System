package com.sit.loanapp.dao;

import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User entity persistence operations, audit logging, and CIBIL data.
 */
public interface UserDao {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean registerUser(User user);

    Optional<User> findByUsernameOrEmail(String identifier);

    Optional<User> findById(int id);

    boolean incrementFailedAttempts(int userId);

    boolean resetFailedAttempts(int userId);

    boolean lockAccount(int userId);

    boolean unlockAccount(int userId);

    boolean updatePassword(int userId, String newPassword);

    // Stage 2 Admin Operations
    List<User> searchUsers(String criteria, String term);

    List<User> findAllUsers();

    boolean updateUser(User user);

    boolean setUserStatus(int userId, String status);

    boolean deleteUser(int userId);

    boolean logAudit(AuditLog log);

    List<AuditLog> getRecentAuditLogs(int limit);

    // Stage 4 CIBIL & PAN Extension
    boolean updateCibilAndPan(int userId, String pancard, int cibilScore);
}
