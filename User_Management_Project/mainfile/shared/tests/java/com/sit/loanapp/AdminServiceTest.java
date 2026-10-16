package com.sit.loanapp;

import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.AdminService;
import com.sit.loanapp.service.AdminService.OperationResult;
import com.sit.loanapp.service.AdminServiceImpl;
import com.sit.loanapp.service.ConsoleNotificationService;
import com.sit.loanapp.service.UserService;
import com.sit.loanapp.service.UserService.LoginResult;
import com.sit.loanapp.service.UserService.LoginStatus;
import com.sit.loanapp.service.UserServiceImpl;
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

@DisplayName("Stage 2: Admin Management System Tests")
public class AdminServiceTest {

    private static final String H2_URL = "jdbc:h2:mem:loan_test_admin;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
    private UserDao userDao;
    private AdminService adminService;
    private UserService userService;
    private ConsoleNotificationService notificationService;
    private User adminUser;

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
        }
    }

    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = getTestConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM audit_logs");
            stmt.execute("DELETE FROM users");
        }

        userDao = new UserDaoImpl(AdminServiceTest::getTestConnection);
        notificationService = new ConsoleNotificationService();
        adminService = new AdminServiceImpl(userDao, notificationService);
        userService = new UserServiceImpl(userDao);

        // Seed Predefined Admin
        adminUser = new User("System", "Administrator", "9999999999", "admin@bank.com", "HQ", "admin", "Admin@1234", User.ROLE_ADMIN);
        userDao.registerUser(adminUser);
    }

    @Test
    @DisplayName("Admin Direct Login and Exclusion from Lockout")
    void testAdminDirectLoginAndNoLockout() {
        // Direct login success
        LoginResult success = userService.login("admin", "Admin@1234");
        assertTrue(success.isSuccess());
        assertTrue(success.getUser().isAdmin());
        assertTrue(success.getMessage().contains("Administrator"));

        // 5 consecutive wrong passwords for admin: must NOT lock account!
        for (int i = 0; i < 5; i++) {
            LoginResult fail = userService.login("admin", "WrongPass@99");
            assertFalse(fail.isSuccess());
            assertNotEquals(LoginStatus.ACCOUNT_LOCKED, fail.getStatus());
        }

        // Admin can still login with correct password immediately
        LoginResult loginAgain = userService.login("admin", "Admin@1234");
        assertTrue(loginAgain.isSuccess());
        assertTrue(loginAgain.getUser().isActive());
    }

    @Test
    @DisplayName("Search Users by Various Criteria")
    void testSearchUsers() {
        User u1 = new User("Amruta", "Patil", "9876543210", "amruta.patil@example.com", "Pune", "amrutap", "Pass@1234");
        User u2 = new User("Rahul", "Sharma", "9123456780", "rahul.sharma@example.com", "Mumbai", "rahuls", "Pass@1234");
        userDao.registerUser(u1);
        userDao.registerUser(u2);

        // Search by first name
        List<User> byFname = adminService.searchUsers("fname", "Amruta");
        assertEquals(1, byFname.size());
        assertEquals("amrutap", byFname.get(0).getUsername());

        // Search by mobile
        List<User> byMobile = adminService.searchUsers("mobile", "9123456780");
        assertEquals(1, byMobile.size());
        assertEquals("rahuls", byMobile.get(0).getUsername());

        // Search by email
        List<User> byEmail = adminService.searchUsers("email", "amruta.patil");
        assertEquals(1, byEmail.size());

        // General search across all fields
        List<User> general = adminService.searchUsers("all", "example.com");
        assertEquals(2, general.size());
    }

    @Test
    @DisplayName("Activate User, Reset Lockout, and Trigger Email Notification")
    void testActivateUser() {
        User lockedUser = new User("Sarah", "Smith", "9988776655", "sarah@example.com", "Bangalore", "sarahs", "Pass@1234");
        lockedUser.setStatus("Inactive");
        lockedUser.setFailedAttempts(3);
        userDao.registerUser(lockedUser);
        userDao.lockAccount(lockedUser.getId());

        notificationService.clearHistory();
        OperationResult result = adminService.activateUser(adminUser, lockedUser.getId());
        assertTrue(result.isSuccess());

        User refreshed = userDao.findById(lockedUser.getId()).orElseThrow();
        assertEquals("Active", refreshed.getStatus());
        assertEquals(0, refreshed.getFailedAttempts());
        assertTrue(refreshed.isActive());

        // Verify notification
        assertFalse(notificationService.getSentEmails().isEmpty());
        var email = notificationService.getSentEmails().get(0);
        assertEquals("sarah@example.com", email.toEmail());
        assertEquals("Account Update Notification", email.subject());
        assertTrue(email.body().contains("successfully activated by the admin"));

        // Verify audit log
        List<AuditLog> auditLogs = adminService.getRecentAuditLogs(5);
        assertFalse(auditLogs.isEmpty());
        assertEquals("ACTIVATE_USER", auditLogs.get(0).getAction());
        assertEquals("admin", auditLogs.get(0).getAdminUsername());
    }

    @Test
    @DisplayName("Deactivate User, Trigger Email Notification, and Prevent Self-Deactivation")
    void testDeactivateUser() {
        User activeUser = new User("John", "Doe", "9876543211", "john@example.com", "Delhi", "johnd", "Pass@1234");
        userDao.registerUser(activeUser);

        // Admin cannot deactivate themselves
        OperationResult selfDeact = adminService.deactivateUser(adminUser, adminUser.getId());
        assertFalse(selfDeact.isSuccess());
        assertTrue(selfDeact.getMessage().contains("cannot deactivate your own"));

        // Admin deactivates regular user
        notificationService.clearHistory();
        OperationResult result = adminService.deactivateUser(adminUser, activeUser.getId());
        assertTrue(result.isSuccess());

        User refreshed = userDao.findById(activeUser.getId()).orElseThrow();
        assertEquals("Inactive", refreshed.getStatus());

        // Verify email notification
        var email = notificationService.getSentEmails().get(0);
        assertEquals("john@example.com", email.toEmail());
        assertTrue(email.body().contains("your account has been deactivated"));
    }

    @Test
    @DisplayName("Edit User Information and Enforce Validation")
    void testEditUserInformation() {
        User user = new User("Pooja", "Verma", "9876543212", "pooja@example.com", "Pune", "poojav", "Pass@1234");
        userDao.registerUser(user);

        // Invalid mobile in edit
        user.setMobile("12345");
        OperationResult failEdit = adminService.editUser(adminUser, user);
        assertFalse(failEdit.isSuccess());

        // Valid update
        user.setMobile("9888888888");
        user.setLocation("Hyderabad");
        notificationService.clearHistory();
        OperationResult successEdit = adminService.editUser(adminUser, user);
        assertTrue(successEdit.isSuccess());

        User refreshed = userDao.findById(user.getId()).orElseThrow();
        assertEquals("9888888888", refreshed.getMobile());
        assertEquals("Hyderabad", refreshed.getLocation());

        // Verify email notification
        var email = notificationService.getSentEmails().get(0);
        assertTrue(email.body().contains("account details have been updated"));
    }

    @Test
    @DisplayName("Delete User, Enforce Confirmation, Trigger Notification, Prevent Self-Delete")
    void testDeleteUser() {
        User target = new User("Temp", "User", "9876543213", "temp@example.com", "Pune", "tempu", "Pass@1234");
        userDao.registerUser(target);

        // Admin cannot delete themselves
        OperationResult selfDel = adminService.deleteUser(adminUser, adminUser.getId());
        assertFalse(selfDel.isSuccess());

        // Delete target user
        notificationService.clearHistory();
        OperationResult result = adminService.deleteUser(adminUser, target.getId());
        assertTrue(result.isSuccess());

        // Verify user no longer exists in database
        Optional<User> check = userDao.findById(target.getId());
        assertTrue(check.isEmpty());

        // Verify notification
        var email = notificationService.getSentEmails().get(0);
        assertEquals("temp@example.com", email.toEmail());
        assertTrue(email.body().contains("account has been deleted from the system"));

        // Verify audit log
        List<AuditLog> auditLogs = adminService.getRecentAuditLogs(5);
        assertEquals("DELETE_USER", auditLogs.get(0).getAction());
    }

    @Test
    @DisplayName("Role-Based Access Control: Regular User Blocked from Admin Actions")
    void testRoleBasedAccessControl() {
        User regularUser = new User("Regular", "Person", "9876543214", "regular@example.com", "City", "regularp", "Pass@1234", User.ROLE_USER);
        userDao.registerUser(regularUser);

        OperationResult act = adminService.activateUser(regularUser, 999);
        assertFalse(act.isSuccess());
        assertTrue(act.getMessage().contains("Access Denied"));

        OperationResult deact = adminService.deactivateUser(regularUser, 999);
        assertFalse(deact.isSuccess());
        assertTrue(deact.getMessage().contains("Access Denied"));

        OperationResult del = adminService.deleteUser(regularUser, 999);
        assertFalse(del.isSuccess());
        assertTrue(del.getMessage().contains("Access Denied"));
    }
}
