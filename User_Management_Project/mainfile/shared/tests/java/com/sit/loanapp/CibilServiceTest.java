package com.sit.loanapp;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.CibilService;
import com.sit.loanapp.service.CibilService.CibilCalculatorResult;
import com.sit.loanapp.service.CibilService.CibilOnboardingResult;
import com.sit.loanapp.service.CibilServiceImpl;
import com.sit.loanapp.service.ConsoleNotificationService;
import com.sit.loanapp.util.ValidationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Stage 4: CIBIL Score & PAN Validation Tests")
public class CibilServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:loan_test_stage4;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private UserDao userDao;
    private CibilService cibilService;
    private ConsoleNotificationService notificationService;

    private static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, "sa", "");
    }

    @BeforeAll
    static void initSchema() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS audit_logs");
            stmt.execute("DROP TABLE IF EXISTS users");

            stmt.execute(
                "CREATE TABLE users (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  fname VARCHAR(50) NOT NULL," +
                "  lname VARCHAR(50) NOT NULL," +
                "  mobile VARCHAR(15) NOT NULL," +
                "  email VARCHAR(100) NOT NULL UNIQUE," +
                "  location VARCHAR(100)," +
                "  pancard VARCHAR(10) NULL," +
                "  cibil_score INT NULL," +
                "  lastmodifieddate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  status VARCHAR(20) NOT NULL DEFAULT 'Active'," +
                "  username VARCHAR(50) NOT NULL UNIQUE," +
                "  password VARCHAR(255) NOT NULL," +
                "  failed_attempts INT NOT NULL DEFAULT 0," +
                "  user_role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }

        userDao = new UserDaoImpl(CibilServiceTest::getTestConnection);
        notificationService = new ConsoleNotificationService();
        // Fixed seed random for deterministic calculations where needed
        cibilService = new CibilServiceImpl(userDao, notificationService, new Random(42));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABCDE1234F", "BKWPR5432M", "CKLPS9876T", "XYZAB5678Z"})
    @DisplayName("Valid PAN Numbers")
    void testValidPanNumbers(String pan) {
        assertTrue(ValidationUtil.isValidPan(pan));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345ABCDE", "ABCD12345E", "ABCDE12345", "ABCDE123F", "ABCDE12345F", "abcde1234", ""})
    @DisplayName("Invalid PAN Numbers")
    void testInvalidPanNumbers(String pan) {
        assertFalse(ValidationUtil.isValidPan(pan));
    }

    @Test
    @DisplayName("CIBIL Score Generation Range: 600 to 900")
    void testCibilScoreRange() {
        for (int i = 0; i < 50; i++) {
            int score = cibilService.generateCibilScore();
            assertTrue(score >= 600 && score <= 900, "CIBIL score " + score + " must be between 600 and 900.");
        }
    }

    @Test
    @DisplayName("Interest Rate Range Bounds per Loan Type")
    void testInterestRateRanges() {
        // Home Loan: 6.5% - 8.5%
        double homeLow = cibilService.calculateInterestRate("Home Loan", 900);
        double homeHigh = cibilService.calculateInterestRate("Home Loan", 600);
        assertEquals(6.5, homeLow);
        assertEquals(8.5, homeHigh);

        // Personal Loan: 9.0% - 12.0%
        double personalLow = cibilService.calculateInterestRate("Personal Loan", 900);
        double personalHigh = cibilService.calculateInterestRate("Personal Loan", 600);
        assertEquals(9.0, personalLow);
        assertEquals(12.0, personalHigh);

        // Education Loan: 4.0% - 6.0%
        double eduLow = cibilService.calculateInterestRate("Education Loan", 900);
        double eduHigh = cibilService.calculateInterestRate("Education Loan", 600);
        assertEquals(4.0, eduLow);
        assertEquals(6.0, eduHigh);

        // Vehicle Loan: 7.0% - 9.0%
        double vehLow = cibilService.calculateInterestRate("Vehicle Loan", 900);
        double vehHigh = cibilService.calculateInterestRate("Vehicle Loan", 600);
        assertEquals(7.0, vehLow);
        assertEquals(9.0, vehHigh);
    }

    @Test
    @DisplayName("Interest Rate Calculator with PAN Validation and Exact Output Format")
    void testCalculatorWorkflow() {
        // Invalid PAN format
        CibilCalculatorResult r1 = cibilService.calculate("BADPAN123", "Home Loan");
        assertFalse(r1.isSuccess());
        assertEquals("Invalid PAN format. Please enter a valid PAN.", r1.getMessage());

        // Invalid Loan Type
        CibilCalculatorResult r2 = cibilService.calculate("ABCDE1234F", "InvalidType");
        assertFalse(r2.isSuccess());
        assertEquals("Invalid loan type selected.", r2.getMessage());

        // Valid Calculation
        CibilCalculatorResult r3 = cibilService.calculate("ABCDE1234F", "Home Loan");
        assertTrue(r3.isSuccess());
        assertTrue(r3.getCibilScore() >= 600 && r3.getCibilScore() <= 900);
        assertTrue(r3.getInterestRate() >= 6.5 && r3.getInterestRate() <= 8.5);
        assertTrue(r3.getMessage().startsWith("For PAN ABCDE1234F, with a CIBIL score of "));
        assertTrue(r3.getMessage().contains("the interest rate for Home Loan is "));
    }

    @Test
    @DisplayName("Auto-Generate Username: First 3 fname + First 3 lname + Last 4 mobile")
    void testGenerateUsername() {
        // Document specification example: fname = Shivani, lname = Chaudhari, mobile = 9876543210 -> ShiCha3210
        String username = cibilService.generateUsername("Shivani", "Chaudhari", "9876543210");
        assertEquals("ShiCha3210", username);

        String username2 = cibilService.generateUsername("Amruta", "Patil", "9123456789");
        assertEquals("AmrPat6789", username2);
    }

    @Test
    @DisplayName("Auto-Generate Password: 8-10 chars with Letters, Numbers, and Symbols")
    void testGeneratePassword() {
        String pass = cibilService.generatePassword();
        assertTrue(pass.length() >= 8 && pass.length() <= 12);
        assertTrue(ValidationUtil.isValidPassword(pass));
    }

    @Test
    @DisplayName("CIBIL Check and Auto-Generated Credentials Onboarding Workflow")
    void testOnboardWithCibil() {
        notificationService.clearHistory();

        CibilOnboardingResult result = cibilService.onboardWithCibil(
                "Shivani", "Chaudhari", "9876543210", "shivani@example.com", "Pune", "ABCDE1234F"
        );

        assertTrue(result.isSuccess());
        assertEquals("ShiCha3210", result.getGeneratedUsername());
        assertNotNull(result.getGeneratedPassword());
        assertTrue(result.getCibilScore() >= 600 && result.getCibilScore() <= 900);

        // Verify credentials notification email dispatched
        assertFalse(notificationService.getSentEmails().isEmpty());
        var email = notificationService.getSentEmails().get(0);
        assertEquals("shivani@example.com", email.toEmail());
        assertEquals("Your Username and Password for Loan Application is", email.subject());
        assertTrue(email.body().contains("ShiCha3210"));

        // Verify user persisted with pancard and cibil_score
        User userInDb = userDao.findByUsernameOrEmail("ShiCha3210").orElseThrow();
        assertEquals("ABCDE1234F", userInDb.getPancard());
        assertNotNull(userInDb.getCibilScore());
        assertEquals(result.getCibilScore(), userInDb.getCibilScore());
    }
}
