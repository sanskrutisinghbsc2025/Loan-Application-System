package com.sit.loanapp.dao;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of the UserDao interface supporting Stage 1 & Stage 2 operations.
 */
public class UserDaoImpl implements UserDao {

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    private final ConnectionSupplier connectionSupplier;

    public UserDaoImpl() {
        this(DatabaseConfig::getConnection);
    }

    public UserDaoImpl(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    private Connection getConnection() throws SQLException {
        return connectionSupplier.get();
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] existsByUsername failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] existsByEmail failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, " +
                     "status, username, password, failed_attempts, user_role, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setString(1, user.getFname());
            ps.setString(2, user.getLname());
            ps.setString(3, user.getMobile());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getLocation());
            ps.setTimestamp(6, now);
            ps.setString(7, user.getStatus() != null ? user.getStatus() : "Active");
            ps.setString(8, user.getUsername());
            ps.setString(9, user.getPassword());
            ps.setInt(10, user.getFailedAttempts());
            ps.setString(11, user.getUserRole() != null ? user.getUserRole() : User.ROLE_USER);
            ps.setTimestamp(12, now);

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                user.setLastModifiedDate(now);
                user.setCreatedAt(now);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] registerUser failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String identifier) {
        String sql = "SELECT * FROM users WHERE LOWER(username) = LOWER(?) OR LOWER(email) = LOWER(?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findByUsernameOrEmail failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean incrementFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_attempts = failed_attempts + 1, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] incrementFailedAttempts failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean resetFailedAttempts(int userId) {
        String sql = "UPDATE users SET failed_attempts = 0, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] resetFailedAttempts failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean lockAccount(int userId) {
        String sql = "UPDATE users SET status = 'Inactive', failed_attempts = 3, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] lockAccount failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean unlockAccount(int userId) {
        String sql = "UPDATE users SET status = 'Active', failed_attempts = 0, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] unlockAccount failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ?, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] updatePassword failed: " + e.getMessage());
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Stage 2: Admin Operations Implementation
    // -------------------------------------------------------------------------

    @Override
    public List<User> searchUsers(String criteria, String term) {
        List<User> list = new ArrayList<>();
        String trimmed = (term != null) ? term.trim() : "";
        String wildcard = "%" + trimmed.toLowerCase() + "%";

        String sql;
        switch (criteria.toLowerCase()) {
            case "fname" -> sql = "SELECT * FROM users WHERE LOWER(fname) LIKE ? ORDER BY id ASC";
            case "lname" -> sql = "SELECT * FROM users WHERE LOWER(lname) LIKE ? ORDER BY id ASC";
            case "email" -> sql = "SELECT * FROM users WHERE LOWER(email) LIKE ? ORDER BY id ASC";
            case "mobile" -> sql = "SELECT * FROM users WHERE mobile LIKE ? ORDER BY id ASC";
            case "username" -> sql = "SELECT * FROM users WHERE LOWER(username) LIKE ? ORDER BY id ASC";
            case "status" -> sql = "SELECT * FROM users WHERE LOWER(status) = LOWER(?) ORDER BY id ASC";
            default -> sql = "SELECT * FROM users WHERE LOWER(fname) LIKE ? OR LOWER(lname) LIKE ? " +
                             "OR LOWER(email) LIKE ? OR mobile LIKE ? OR LOWER(username) LIKE ? ORDER BY id ASC";
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if ("status".equalsIgnoreCase(criteria)) {
                ps.setString(1, trimmed);
            } else if (!"all".equalsIgnoreCase(criteria) && !criteria.equalsIgnoreCase("default")) {
                ps.setString(1, wildcard);
            } else {
                ps.setString(1, wildcard);
                ps.setString(2, wildcard);
                ps.setString(3, wildcard);
                ps.setString(4, wildcard);
                ps.setString(5, wildcard);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] searchUsers failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findAllUsers failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET fname = ?, lname = ?, mobile = ?, email = ?, location = ?, lastmodifieddate = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setString(1, user.getFname());
            ps.setString(2, user.getLname());
            ps.setString(3, user.getMobile());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getLocation());
            ps.setTimestamp(6, now);
            ps.setInt(7, user.getId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                user.setLastModifiedDate(now);
            }
            return updated;
        } catch (SQLException e) {
            System.err.println("[DAO Error] updateUser failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean setUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ?, lastmodifieddate = ?, " +
                     "failed_attempts = CASE WHEN ? = 'Active' THEN 0 ELSE failed_attempts END " +
                     "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, status);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] setUserStatus failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] deleteUser failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean logAudit(AuditLog log) {
        String sql = "INSERT INTO audit_logs (admin_username, action, target_user_id, target_username, details, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setString(1, log.getAdminUsername());
            ps.setString(2, log.getAction());
            if (log.getTargetUserId() != null) {
                ps.setInt(3, log.getTargetUserId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setString(4, log.getTargetUsername());
            ps.setString(5, log.getDetails());
            ps.setTimestamp(6, now);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setId(rs.getInt(1));
                    }
                }
                log.setCreatedAt(now);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] logAudit failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int limit) {
        List<AuditLog> list = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY id DESC LIMIT ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    log.setAdminUsername(rs.getString("admin_username"));
                    log.setAction(rs.getString("action"));
                    int targetId = rs.getInt("target_user_id");
                    if (!rs.wasNull()) {
                        log.setTargetUserId(targetId);
                    }
                    log.setTargetUsername(rs.getString("target_username"));
                    log.setDetails(rs.getString("details"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(log);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] getRecentAuditLogs failed: " + e.getMessage());
        }
        return list;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFname(rs.getString("fname"));
        user.setLname(rs.getString("lname"));
        user.setMobile(rs.getString("mobile"));
        user.setEmail(rs.getString("email"));
        user.setLocation(rs.getString("location"));
        user.setLastModifiedDate(rs.getTimestamp("lastmodifieddate"));
        user.setStatus(rs.getString("status"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setUserRole(rs.getString("user_role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
