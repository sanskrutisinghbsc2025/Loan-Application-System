package com.sit.loanapp;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User DAO Integration Tests (In-Memory Database)")
public class UserDaoTest {

    private static final String H2_URL = "jdbc:h2:mem:loan_test_dao;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
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
        userDao = new UserDaoImpl(UserDaoTest::getTestConnection);
    }

    @Test
    @DisplayName("Register User and Retrieve by Username")
    void testRegisterAndRetrieveByUsername() {
        User user = new User("Amruta", "Patil", "9876543210", "amruta@example.com", "Pune", "amrutap", "Password@123");
        boolean registered = userDao.registerUser(user);
        assertTrue(registered);
        assertTrue(user.getId() > 0);

        Optional<User> found = userDao.findByUsernameOrEmail("amrutap");
        assertTrue(found.isPresent());
        assertEquals("Amruta", found.get().getFname());
        assertEquals("Patil", found.get().getLname());
        assertEquals("amruta@example.com", found.get().getEmail());
        assertEquals("Active", found.get().getStatus());
        assertEquals(0, found.get().getFailedAttempts());
    }

    @Test
    @DisplayName("Retrieve User by Email")
    void testRetrieveByEmail() {
        User user = new User("John", "Doe", "9123456780", "john.doe@bank.com", "Mumbai", "johnd", "Secure@123");
        userDao.registerUser(user);

        Optional<User> found = userDao.findByUsernameOrEmail("john.doe@bank.com");
        assertTrue(found.isPresent());
        assertEquals("johnd", found.get().getUsername());
    }

    @Test
    @DisplayName("Check Uniqueness: Username and Email")
    void testUniquenessChecks() {
        User user = new User("Alice", "Brown", "9000000001", "alice@example.com", "Delhi", "aliceb", "Password@1");
        userDao.registerUser(user);

        assertTrue(userDao.existsByUsername("aliceb"));
        assertTrue(userDao.existsByUsername("ALICEB")); // Case insensitive check
        assertFalse(userDao.existsByUsername("nonexistent"));

        assertTrue(userDao.existsByEmail("alice@example.com"));
        assertFalse(userDao.existsByEmail("other@example.com"));
    }

    @Test
    @DisplayName("Increment and Reset Failed Attempts")
    void testFailedAttemptsLifecycle() {
        User user = new User("Bob", "Ross", "9000000002", "bob@example.com", "Goa", "bobr", "Secret@99");
        userDao.registerUser(user);

        userDao.incrementFailedAttempts(user.getId());
        User step1 = userDao.findById(user.getId()).orElseThrow();
        assertEquals(1, step1.getFailedAttempts());

        userDao.incrementFailedAttempts(user.getId());
        User step2 = userDao.findById(user.getId()).orElseThrow();
        assertEquals(2, step2.getFailedAttempts());

        // Reset to 0
        userDao.resetFailedAttempts(user.getId());
        User step3 = userDao.findById(user.getId()).orElseThrow();
        assertEquals(0, step3.getFailedAttempts());
    }

    @Test
    @DisplayName("Lock Account after 3 Failures")
    void testLockAccount() {
        User user = new User("Charlie", "Puth", "9000000003", "charlie@example.com", "Pune", "charliep", "Tune#1234");
        userDao.registerUser(user);

        userDao.lockAccount(user.getId());
        User locked = userDao.findById(user.getId()).orElseThrow();
        assertEquals("Inactive", locked.getStatus());
        assertEquals(3, locked.getFailedAttempts());
        assertFalse(locked.isActive());
    }

    @Test
    @DisplayName("Update User Password")
    void testUpdatePassword() {
        User user = new User("David", "Miller", "9000000004", "david@example.com", "Chennai", "davidm", "OldPass@1");
        userDao.registerUser(user);

        boolean updated = userDao.updatePassword(user.getId(), "NewSecure@999");
        assertTrue(updated);

        User updatedUser = userDao.findById(user.getId()).orElseThrow();
        assertEquals("NewSecure@999", updatedUser.getPassword());
    }
}
