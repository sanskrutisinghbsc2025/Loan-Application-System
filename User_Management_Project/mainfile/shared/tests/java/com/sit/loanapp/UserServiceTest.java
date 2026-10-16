package com.sit.loanapp;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.UserService;
import com.sit.loanapp.service.UserService.LoginResult;
import com.sit.loanapp.service.UserService.LoginStatus;
import com.sit.loanapp.service.UserService.PasswordResetResult;
import com.sit.loanapp.service.UserService.RegistrationResult;
import com.sit.loanapp.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Service Business Logic Tests")
public class UserServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:loan_test_service;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private UserService userService;
    private UserDao userDao;

    private static Connection getTestConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, "sa", "");
    }

    @BeforeAll
    static void initSchema() throws Exception {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
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
                "  user_role VARCHAR(20) NOT NULL DEFAULT 'USER'," +
                "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        }
    }

    @BeforeEach
    void setup() throws Exception {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM users");
        }
        userDao = new UserDaoImpl(UserServiceTest::getTestConnection);
        userService = new UserServiceImpl(userDao);
    }

    @Test
    @DisplayName("User Registration Validation Failures")
    void testRegistrationValidationFailures() {
        // Missing mandatory field
        User emptyFname = new User("", "Patil", "9876543210", "amruta@example.com", "Pune", "amrutap", "Pass@1234");
        RegistrationResult r1 = userService.register(emptyFname);
        assertFalse(r1.isSuccess());
        assertTrue(r1.getMessage().contains("First name is mandatory"));

        // Invalid mobile
        User badMobile = new User("Amruta", "Patil", "12345", "amruta@example.com", "Pune", "amrutap", "Pass@1234");
        RegistrationResult r2 = userService.register(badMobile);
        assertFalse(r2.isSuccess());
        assertTrue(r2.getMessage().contains("Invalid mobile number"));

        // Invalid email
        User badEmail = new User("Amruta", "Patil", "9876543210", "bad-email", "Pune", "amrutap", "Pass@1234");
        RegistrationResult r3 = userService.register(badEmail);
        assertFalse(r3.isSuccess());
        assertTrue(r3.getMessage().contains("Invalid email format"));

        // Weak password
        User weakPass = new User("Amruta", "Patil", "9876543210", "amruta@example.com", "Pune", "amrutap", "weak");
        RegistrationResult r4 = userService.register(weakPass);
        assertFalse(r4.isSuccess());
        assertTrue(r4.getMessage().contains("Password does not meet"));
    }

    @Test
    @DisplayName("Successful Registration and Duplicate Prevention")
    void testRegistrationSuccessAndDuplicates() {
        User validUser = new User("Amruta", "Patil", "9876543210", "amruta.patil@example.com", "Pune", "amrutap", "Password@123");
        RegistrationResult reg = userService.register(validUser);
        assertTrue(reg.isSuccess());

        // Attempt registering with duplicate username
        User dupUsername = new User("Another", "User", "9111111111", "another@example.com", "Mumbai", "amrutap", "Password@123");
        RegistrationResult dupUserResult = userService.register(dupUsername);
        assertFalse(dupUserResult.isSuccess());
        assertTrue(dupUserResult.getMessage().contains("already taken"));

        // Attempt registering with duplicate email
        User dupEmail = new User("Another", "User", "9111111111", "amruta.patil@example.com", "Mumbai", "anotheruser", "Password@123");
        RegistrationResult dupEmailResult = userService.register(dupEmail);
        assertFalse(dupEmailResult.isSuccess());
        assertTrue(dupEmailResult.getMessage().contains("already registered"));
    }

    @Test
    @DisplayName("User Login with Username and Email")
    void testLoginSuccess() {
        User user = new User("Amruta", "Patil", "9876543210", "amruta.patil@example.com", "Pune", "amrutap", "Password@123");
        userService.register(user);

        // Login with username
        LoginResult res1 = userService.login("amrutap", "Password@123");
        assertTrue(res1.isSuccess());
        assertEquals(LoginStatus.SUCCESS, res1.getStatus());
        assertEquals("Welcome Amruta Patil", res1.getMessage());

        // Login with email
        LoginResult res2 = userService.login("amruta.patil@example.com", "Password@123");
        assertTrue(res2.isSuccess());
        assertEquals(LoginStatus.SUCCESS, res2.getStatus());
        assertEquals("Welcome Amruta Patil", res2.getMessage());
    }

    @Test
    @DisplayName("Login with Non-existent User")
    void testLoginUserNotFound() {
        LoginResult result = userService.login("unknown_user", "AnyPassword@1");
        assertFalse(result.isSuccess());
        assertEquals(LoginStatus.USER_NOT_FOUND, result.getStatus());
    }

    @Test
    @DisplayName("Account Lockout Sequence: 3 Consecutive Failed Attempts")
    void testAccountLockoutAfterThreeFailures() {
        User user = new User("Test", "Subject", "9876500000", "subject@test.com", "City", "testsub", "Correct@Pass1");
        userService.register(user);

        // 1st failed attempt -> 2 remaining
        LoginResult fail1 = userService.login("testsub", "WrongPass@1");
        assertFalse(fail1.isSuccess());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, fail1.getStatus());
        assertEquals(2, fail1.getRemainingAttempts());
        assertTrue(fail1.getMessage().contains("2 attempt(s) remaining"));

        // 2nd failed attempt -> 1 remaining
        LoginResult fail2 = userService.login("testsub", "WrongPass@2");
        assertFalse(fail2.isSuccess());
        assertEquals(LoginStatus.INVALID_CREDENTIALS, fail2.getStatus());
        assertEquals(1, fail2.getRemainingAttempts());
        assertTrue(fail2.getMessage().contains("1 attempt(s) remaining"));

        // 3rd failed attempt -> Account Locked!
        LoginResult fail3 = userService.login("testsub", "WrongPass@3");
        assertFalse(fail3.isSuccess());
        assertEquals(LoginStatus.ACCOUNT_LOCKED, fail3.getStatus());
        assertEquals(0, fail3.getRemainingAttempts());
        assertTrue(fail3.getMessage().contains("account is locked"));
        assertTrue(fail3.getMessage().contains("contact support"));

        // Subsequent attempt on locked account
        LoginResult fail4 = userService.login("testsub", "Correct@Pass1");
        assertFalse(fail4.isSuccess());
        assertEquals(LoginStatus.ACCOUNT_ALREADY_LOCKED, fail4.getStatus());
        assertTrue(fail4.getMessage().contains("locked"));
    }

    @Test
    @DisplayName("Reset Failed Attempts on Successful Login")
    void testResetAttemptsOnSuccess() {
        User user = new User("Recover", "User", "9876500001", "recover@test.com", "City", "recoveru", "Valid#Password99");
        userService.register(user);

        // 2 failed attempts
        userService.login("recoveru", "WrongPass@1");
        userService.login("recoveru", "WrongPass@2");

        // 3rd attempt is successful
        LoginResult success = userService.login("recoveru", "Valid#Password99");
        assertTrue(success.isSuccess());

        // Verify DB failed_attempts is 0
        User inDb = userDao.findByUsernameOrEmail("recoveru").orElseThrow();
        assertEquals(0, inDb.getFailedAttempts());
    }

    @Test
    @DisplayName("Forget Password Reset Workflow")
    void testForgotPasswordWorkflow() {
        User user = new User("Forgetful", "User", "9876500002", "forgot@test.com", "City", "forgotu", "Old#Secret1");
        userService.register(user);

        // Reset with invalid new password
        PasswordResetResult badReset = userService.resetPassword("forgotu", "weak");
        assertFalse(badReset.isSuccess());

        // Reset with compliant new password
        PasswordResetResult goodReset = userService.resetPassword("forgotu", "New#Secret999");
        assertTrue(goodReset.isSuccess());

        // Login with new password
        LoginResult loginNew = userService.login("forgotu", "New#Secret999");
        assertTrue(loginNew.isSuccess());

        // Old password must fail
        LoginResult loginOld = userService.login("forgotu", "Old#Secret1");
        assertFalse(loginOld.isSuccess());
    }
}
