package com.sit.loanapp.dao;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.model.LoanApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of LoanDao using PreparedStatements and safe connection handling.
 */
public class LoanDaoImpl implements LoanDao {

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    private final ConnectionSupplier connectionSupplier;

    public LoanDaoImpl() {
        this(DatabaseConfig::getConnection);
    }

    public LoanDaoImpl(ConnectionSupplier connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    private Connection getConnection() throws SQLException {
        return connectionSupplier.get();
    }

    @Override
    public boolean applyForLoan(LoanApplication app) {
        String sql = "INSERT INTO loan_applications (user_id, loan_amount, loan_type, loan_duration, " +
                     "interest_rate, annual_income, purpose, status, rejection_reason, application_date, last_modified_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setInt(1, app.getUserId());
            ps.setDouble(2, app.getLoanAmount());
            ps.setString(3, app.getLoanType());
            ps.setInt(4, app.getLoanDuration());
            ps.setDouble(5, app.getInterestRate());
            ps.setDouble(6, app.getAnnualIncome());
            ps.setString(7, app.getPurpose());
            ps.setString(8, app.getStatus() != null ? app.getStatus() : LoanApplication.STATUS_PENDING);
            ps.setString(9, app.getRejectionReason());
            ps.setTimestamp(10, now);
            ps.setTimestamp(11, now);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        app.setId(rs.getInt(1));
                    }
                }
                app.setApplicationDate(now);
                app.setLastModifiedDate(now);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] applyForLoan failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public Optional<LoanApplication> findById(int id) {
        String sql = "SELECT l.*, u.fname, u.lname, u.email, u.mobile " +
                     "FROM loan_applications l " +
                     "JOIN users u ON l.user_id = u.id " +
                     "WHERE l.id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findById failed: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<LoanApplication> findByUserId(int userId) {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT l.*, u.fname, u.lname, u.email, u.mobile " +
                     "FROM loan_applications l " +
                     "JOIN users u ON l.user_id = u.id " +
                     "WHERE l.user_id = ? " +
                     "ORDER BY l.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findByUserId failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<LoanApplication> findAll() {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT l.*, u.fname, u.lname, u.email, u.mobile " +
                     "FROM loan_applications l " +
                     "JOIN users u ON l.user_id = u.id " +
                     "ORDER BY l.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToLoan(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findAll failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<LoanApplication> findByStatus(String status) {
        List<LoanApplication> list = new ArrayList<>();
        String sql = "SELECT l.*, u.fname, u.lname, u.email, u.mobile " +
                     "FROM loan_applications l " +
                     "JOIN users u ON l.user_id = u.id " +
                     "WHERE LOWER(l.status) = LOWER(?) " +
                     "ORDER BY l.id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToLoan(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DAO Error] findByStatus failed: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean updateLoanStatus(int loanId, String status, String rejectionReason) {
        String sql = "UPDATE loan_applications SET status = ?, rejection_reason = ?, last_modified_date = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, rejectionReason);
            ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ps.setInt(4, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] updateLoanStatus failed: " + e.getMessage());
        }
        return false;
    }

    @Override
    public boolean updateLoanApproval(int loanId, double emiAmount, double totalInterest, double totalPayable) {
        String sql = "UPDATE loan_applications SET status = ?, rejection_reason = NULL, " +
                     "emi_amount = ?, total_interest = ?, total_payable = ?, last_modified_date = ? " +
                     "WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LoanApplication.STATUS_APPROVED);
            ps.setDouble(2, emiAmount);
            ps.setDouble(3, totalInterest);
            ps.setDouble(4, totalPayable);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            ps.setInt(6, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DAO Error] updateLoanApproval failed: " + e.getMessage());
        }
        return false;
    }

    private LoanApplication mapResultSetToLoan(ResultSet rs) throws SQLException {
        LoanApplication app = new LoanApplication();
        app.setId(rs.getInt("id"));
        app.setUserId(rs.getInt("user_id"));
        app.setLoanAmount(rs.getDouble("loan_amount"));
        app.setLoanType(rs.getString("loan_type"));
        app.setLoanDuration(rs.getInt("loan_duration"));
        app.setInterestRate(rs.getDouble("interest_rate"));
        app.setAnnualIncome(rs.getDouble("annual_income"));
        app.setPurpose(rs.getString("purpose"));
        app.setStatus(rs.getString("status"));
        app.setRejectionReason(rs.getString("rejection_reason"));
        app.setApplicationDate(rs.getTimestamp("application_date"));
        app.setLastModifiedDate(rs.getTimestamp("last_modified_date"));

        // Stage 5: Read EMI metrics if present in result set
        try {
            app.setEmiAmount(rs.getDouble("emi_amount"));
            app.setTotalInterest(rs.getDouble("total_interest"));
            app.setTotalPayable(rs.getDouble("total_payable"));
        } catch (SQLException ignored) {}

        // User join details
        try {
            String fname = rs.getString("fname");
            String lname = rs.getString("lname");
            if (fname != null) {
                app.setApplicantName(fname + " " + (lname != null ? lname : ""));
            }
            app.setApplicantEmail(rs.getString("email"));
            app.setApplicantMobile(rs.getString("mobile"));
        } catch (SQLException ignored) {}

        return app;
    }
}
