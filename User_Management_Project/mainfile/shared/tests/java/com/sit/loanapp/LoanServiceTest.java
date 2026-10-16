package com.sit.loanapp;

import com.sit.loanapp.dao.LoanDao;
import com.sit.loanapp.dao.LoanDaoImpl;
import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.LoanApplication;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.ConsoleNotificationService;
import com.sit.loanapp.service.LoanService;
import com.sit.loanapp.service.LoanService.LoanActionResult;
import com.sit.loanapp.service.LoanService.LoanSubmissionResult;
import com.sit.loanapp.service.LoanServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stage 3: Loan Application Management Tests")
public class LoanServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:loan_test_stage3;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private LoanDao loanDao;
    private UserDao userDao;
    private LoanService loanService;
    private ConsoleNotificationService notificationService;
    private User applicantUser;
    private User adminUser;

    private static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, "sa", "");
    }

    @BeforeAll
    static void initSchema() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS loan_applications");
            stmt.execute("DROP TABLE IF EXISTS audit_logs");
            stmt.execute("DROP TABLE IF EXISTS users");

            stmt.execute(
                "CREATE TABLE users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  fname VARCHAR(50) NOT NULL," +
                "  lname VARCHAR(50) NOT NULL," +
                "  mobile VARCHAR(15) NOT NULL," +
                "  email VARCHAR(100) NOT NULL UNIQUE," +
                "  location VARCHAR(100), pancard VARCHAR(10), cibil_score INT," +
                "  lastmodifieddate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'Active'," +
                "  username VARCHAR(50) NOT NULL UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  failed_attempts INT NOT NULL DEFAULT 0," +
                "  user_role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE audit_logs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  admin_username VARCHAR(50) NOT NULL," +
                "  action VARCHAR(50) NOT NULL," +
                "  target_user_id INT NULL," +
                "  target_username VARCHAR(50) NULL," +
                "  details TEXT NULL," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            stmt.execute(
                "CREATE TABLE loan_applications (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id INT NOT NULL," +
                "  loan_amount DOUBLE NOT NULL," +
                "  loan_type VARCHAR(50) NOT NULL," +
                "  loan_duration INT NOT NULL," +
                "  interest_rate DOUBLE NOT NULL," +
                "  annual_income DOUBLE NOT NULL," +
                "  purpose TEXT NOT NULL," +
                "  status VARCHAR(30) NOT NULL DEFAULT 'Pending'," +
                "  rejection_reason TEXT NULL," +
                "  emi_amount DOUBLE NULL DEFAULT 0.0," +
                "  total_interest DOUBLE NULL DEFAULT 0.0," +
                "  total_payable DOUBLE NULL DEFAULT 0.0," +
                "  application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  last_modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM loan_applications");
            stmt.execute("DELETE FROM audit_logs");
            stmt.execute("DELETE FROM users");
        }

        userDao = new UserDaoImpl(LoanServiceTest::getTestConnection);
        loanDao = new LoanDaoImpl(LoanServiceTest::getTestConnection);
        notificationService = new ConsoleNotificationService();
        loanService = new LoanServiceImpl(loanDao, userDao, notificationService);

        applicantUser = new User("Amruta", "Patil", "9876543210", "amruta.patil@example.com", "Pune", "amrutap", "Pass@1234", User.ROLE_USER);
        userDao.registerUser(applicantUser);

        adminUser = new User("System", "Admin", "9999999999", "admin@bank.com", "HQ", "admin", "Admin@1234", User.ROLE_ADMIN);
        userDao.registerUser(adminUser);
    }

    @Test
    @DisplayName("Auto-assigned Interest Rates per Loan Type")
    void testInterestRateAutoAssignment() {
        assertEquals(7.5, loanService.getDefaultInterestRate("Home Loan"));
        assertEquals(10.5, loanService.getDefaultInterestRate("Personal Loan"));
        assertEquals(5.0, loanService.getDefaultInterestRate("Education Loan"));
        assertEquals(8.0, loanService.getDefaultInterestRate("Vehicle Loan"));
        assertEquals(8.0, loanService.getDefaultInterestRate("Car Loan"));
    }

    @Test
    @DisplayName("Loan Application Form Validations")
    void testLoanFormValidations() {
        // Zero/negative loan amount
        LoanSubmissionResult r1 = loanService.applyForLoan(applicantUser, -5000, "Home Loan", 120, 600000, "House renovation");
        assertFalse(r1.isSuccess());
        assertTrue(r1.getMessage().contains("positive number"));

        // Zero/negative duration
        LoanSubmissionResult r2 = loanService.applyForLoan(applicantUser, 500000, "Home Loan", 0, 600000, "House renovation");
        assertFalse(r2.isSuccess());
        assertTrue(r2.getMessage().contains("greater than 0 months"));

        // Zero/negative annual income
        LoanSubmissionResult r3 = loanService.applyForLoan(applicantUser, 500000, "Home Loan", 120, -100, "House renovation");
        assertFalse(r3.isSuccess());
        assertTrue(r3.getMessage().contains("Annual income must be a positive number"));

        // Blank purpose
        LoanSubmissionResult r4 = loanService.applyForLoan(applicantUser, 500000, "Home Loan", 120, 600000, "   ");
        assertFalse(r4.isSuccess());
        assertTrue(r4.getMessage().contains("Purpose of loan is mandatory"));
    }

    @Test
    @DisplayName("Successful Loan Submission with Status 'Pending'")
    void testSuccessfulLoanSubmission() {
        LoanSubmissionResult result = loanService.applyForLoan(
                applicantUser, 300000.0, "Home Loan", 180, 850000.0, "Down payment on apartment"
        );
        assertTrue(result.isSuccess());
        assertNotNull(result.getApplication());
        assertTrue(result.getApplication().getId() > 0);
        assertEquals(LoanApplication.STATUS_PENDING, result.getApplication().getStatus());
        assertEquals(7.5, result.getApplication().getInterestRate());

        // Verify user can fetch their loans
        List<LoanApplication> userLoans = loanService.getUserLoans(applicantUser.getId());
        assertEquals(1, userLoans.size());
        assertEquals(300000.0, userLoans.get(0).getLoanAmount());
    }

    @Test
    @DisplayName("Admin Approves Loan and Dispatches Email Notification")
    void testAdminApproveLoan() {
        LoanSubmissionResult sub = loanService.applyForLoan(
                applicantUser, 150000.0, "Personal Loan", 36, 600000.0, "Medical emergency"
        );
        int loanId = sub.getApplication().getId();

        notificationService.clearHistory();
        LoanActionResult result = loanService.approveLoan(adminUser, loanId);
        assertTrue(result.isSuccess());

        LoanApplication refreshed = loanService.getLoanById(loanId).orElseThrow();
        assertEquals(LoanApplication.STATUS_APPROVED, refreshed.getStatus());
        assertTrue(refreshed.getEmiAmount() > 0, "EMI amount should be calculated and persisted");
        assertTrue(refreshed.getTotalInterest() > 0, "Total interest should be calculated and persisted");
        assertTrue(refreshed.getTotalPayable() > refreshed.getLoanAmount(), "Total payable should exceed principal");

        // Verify Stage 5 Email Notification with 7 parameters
        assertFalse(notificationService.getSentEmails().isEmpty());
        var email = notificationService.getSentEmails().get(0);
        assertEquals("amruta.patil@example.com", email.toEmail());
        assertEquals("Your Loan Application has been Approved!", email.subject());
        assertTrue(email.body().contains("Dear Amruta Patil"));
        assertTrue(email.body().contains("Sanctioned Loan Amount"));
        assertTrue(email.body().contains("Monthly EMI Amount"));

        // Verify audit log
        List<AuditLog> auditLogs = userDao.getRecentAuditLogs(5);
        assertFalse(auditLogs.isEmpty());
        assertEquals("APPROVE_LOAN", auditLogs.get(0).getAction());
        assertEquals("admin", auditLogs.get(0).getAdminUsername());
        assertTrue(auditLogs.get(0).getDetails().contains("EMI:"));
    }

    @Test
    @DisplayName("Stage 5: EMI Calculation Formula Mathematical Precision")
    void testEmiCalculationFormulaAccuracy() {
        // Profile 1: $100,000 at 12% p.a. for 12 months
        var calc1 = loanService.calculateEmi(100000.0, 12.0, 12);
        assertEquals(8884.88, calc1.getEmiAmount(), 0.05, "Monthly EMI should match standard amortization");
        assertEquals(106618.56, calc1.getTotalPayable(), 0.50, "Total payable should match EMI * 12");
        assertEquals(6618.56, calc1.getTotalInterest(), 0.50, "Total interest should match payable - principal");

        // Profile 2: $500,000 at 7.5% p.a. for 240 months (Home Loan 20 years)
        var calc2 = loanService.calculateEmi(500000.0, 7.5, 240);
        assertEquals(4027.97, calc2.getEmiAmount(), 0.05, "Monthly EMI for 20-year home loan");
        assertTrue(calc2.getTotalPayable() > 960000.0, "Total payable should exceed $960k");
        assertTrue(calc2.getTotalInterest() > 460000.0, "Total interest should exceed $460k");

        // Profile 3: Zero percent interest loan
        var calc3 = loanService.calculateEmi(120000.0, 0.0, 12);
        assertEquals(10000.0, calc3.getEmiAmount(), 0.01);
        assertEquals(120000.0, calc3.getTotalPayable(), 0.01);
        assertEquals(0.0, calc3.getTotalInterest(), 0.01);

        // Profile 4: Invalid/boundary inputs
        var calc4 = loanService.calculateEmi(-5000.0, 10.0, 12);
        assertEquals(0.0, calc4.getEmiAmount());
        var calc5 = loanService.calculateEmi(50000.0, 10.0, 0);
        assertEquals(0.0, calc5.getEmiAmount());
    }

    @Test
    @DisplayName("Stage 5: Approval Persists EMI in MySQL and Emails All 7 Mandatory Parameters")
    void testStage5ApprovalEmiPersistenceAndNotificationFields() {
        // Submit Home Loan
        LoanSubmissionResult sub = loanService.applyForLoan(
                applicantUser, 250000.0, "Home Loan", 120, 95000.0, "Home purchase"
        );
        assertTrue(sub.isSuccess());
        int loanId = sub.getApplication().getId();

        notificationService.clearHistory();
        LoanActionResult result = loanService.approveLoan(adminUser, loanId);
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("Monthly EMI:"));

        // Direct DB Verification
        Optional<LoanApplication> optFromDb = loanDao.findById(loanId);
        assertTrue(optFromDb.isPresent());
        LoanApplication dbLoan = optFromDb.get();
        assertEquals(LoanApplication.STATUS_APPROVED, dbLoan.getStatus());
        assertTrue(dbLoan.getEmiAmount() > 0, "EMI persisted in DB");
        assertTrue(dbLoan.getTotalInterest() > 0, "Total interest persisted in DB");
        assertTrue(dbLoan.getTotalPayable() > 250000.0, "Total payable persisted in DB");

        // Email Verification - All 7 Required Fields
        assertFalse(notificationService.getSentEmails().isEmpty());
        var email = notificationService.getSentEmails().get(0);
        String body = email.body();

        assertTrue(body.contains("1. Sanctioned Loan Amount : $250,000.00"), "Must contain Sanctioned Loan Amount");
        assertTrue(body.contains("2. Loan Type             : Home Loan"), "Must contain Loan Type");
        assertTrue(body.contains("3. Loan Tenure           : 120 months"), "Must contain Loan Tenure");
        assertTrue(body.contains("4. Applied Interest Rate : 7.50%"), "Must contain Applied Interest Rate");
        assertTrue(body.contains("5. Monthly EMI Amount    : $"), "Must contain Monthly EMI Amount");
        assertTrue(body.contains("6. Total Interest Amount : $"), "Must contain Total Interest Amount");
        assertTrue(body.contains("7. Total Payable Amount  : $"), "Must contain Total Payable Amount");
    }

    @Test
    @DisplayName("Admin Rejects Loan with Reason and Dispatches Email Notification")
    void testAdminRejectLoan() {
        LoanSubmissionResult sub = loanService.applyForLoan(
                applicantUser, 900000.0, "Vehicle Loan", 60, 200000.0, "Sports car"
        );
        int loanId = sub.getApplication().getId();

        // Reject without reason must fail
        LoanActionResult noReason = loanService.rejectLoan(adminUser, loanId, "   ");
        assertFalse(noReason.isSuccess());
        assertTrue(noReason.getMessage().contains("mandatory"));

        // Reject with valid reason
        notificationService.clearHistory();
        LoanActionResult result = loanService.rejectLoan(adminUser, loanId, "Annual income is insufficient for the requested amount.");
        assertTrue(result.isSuccess());

        LoanApplication refreshed = loanService.getLoanById(loanId).orElseThrow();
        assertEquals(LoanApplication.STATUS_REJECTED, refreshed.getStatus());
        assertEquals("Annual income is insufficient for the requested amount.", refreshed.getRejectionReason());

        // Verify Stage 3 Email Notification
        var email = notificationService.getSentEmails().get(0);
        assertEquals("amruta.patil@example.com", email.toEmail());
        assertEquals("Your Loan Application has been Rejected", email.subject());
        assertTrue(email.body().contains("Dear Amruta Patil"));
        assertTrue(email.body().contains("Reason: Annual income is insufficient"));

        // Verify audit log
        List<AuditLog> auditLogs = userDao.getRecentAuditLogs(5);
        assertEquals("REJECT_LOAN", auditLogs.get(0).getAction());
    }

    @Test
    @DisplayName("RBAC: Regular User Cannot Approve or Reject Loans")
    void testNonAdminCannotReviewLoans() {
        LoanSubmissionResult sub = loanService.applyForLoan(
                applicantUser, 50000.0, "Personal Loan", 12, 500000.0, "Travel expenses"
        );
        int loanId = sub.getApplication().getId();

        LoanActionResult actApprove = loanService.approveLoan(applicantUser, loanId);
        assertFalse(actApprove.isSuccess());
        assertTrue(actApprove.getMessage().contains("Access Denied"));

        LoanActionResult actReject = loanService.rejectLoan(applicantUser, loanId, "Self rejection test");
        assertFalse(actReject.isSuccess());
        assertTrue(actReject.getMessage().contains("Access Denied"));
    }
}
