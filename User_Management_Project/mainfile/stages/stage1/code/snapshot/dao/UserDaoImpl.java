package com.sit.loanapp.dao;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * JDBC implementation of the UserDao interface.
 * Implements prepared statements, safe resource cleanup, and parameterized queries.
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
            ps.setString(11, user.getUserRole() != null ? user.getUserRole() : "USER");
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
