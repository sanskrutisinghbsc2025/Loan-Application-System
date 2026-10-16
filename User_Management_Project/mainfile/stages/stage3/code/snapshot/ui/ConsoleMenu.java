package com.sit.loanapp.ui;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.LoanApplication;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.AdminService;
import com.sit.loanapp.service.AdminServiceImpl;
import com.sit.loanapp.service.LoanService;
import com.sit.loanapp.service.LoanServiceImpl;
import com.sit.loanapp.service.UserService;
import com.sit.loanapp.service.UserService.LoginResult;
import com.sit.loanapp.service.UserService.PasswordResetResult;
import com.sit.loanapp.service.UserService.RegistrationResult;
import com.sit.loanapp.service.UserServiceImpl;
import com.sit.loanapp.util.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console Interface for Stages 1, 2, and 3:
 * - Stage 1: User Registration, Authentication, Account Lockout
 * - Stage 2: Admin Dashboard, User Management, RBAC, Status Toggles, Deletion
 * - Stage 3: Loan Application Submission, Admin Loan Review, Approvals, Rejections, and Notifications
 */
public class ConsoleMenu {

    private final UserService userService;
    private final AdminService adminService;
    private final LoanService loanService;
    private final InputReader reader;

    public ConsoleMenu(Scanner scanner) {
        this(new UserServiceImpl(), new AdminServiceImpl(), new LoanServiceImpl(), scanner);
    }

    public ConsoleMenu(UserService userService, AdminService adminService, LoanService loanService, Scanner scanner) {
        this.userService = userService;
        this.adminService = adminService;
        this.loanService = loanService;
        this.reader = new InputReader(scanner);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenuHeader();
            System.out.println(" 1. Register New User");
            System.out.println(" 2. User / Admin Login");
            System.out.println(" 3. Forget Password");
            System.out.println(" 4. Database Connection Info");
            System.out.println(" 5. Exit");
            System.out.println("----------------------------------------------------------------");
            int choice = reader.readInt("Select an option (1-5): ", 1, 5);

            switch (choice) {
                case 1 -> handleRegistration();
                case 2 -> handleLogin();
                case 3 -> handleForgotPassword();
                case 4 -> handleDbInfo();
                case 5 -> {
                    System.out.println("\nThank you for using Loan Application System. Goodbye!");
                    running = false;
                }
            }
        }
    }

    private void printMainMenuHeader() {
        System.out.println("\n================================================================");
        System.out.println("               LOAN APPLICATION SYSTEM - STAGE 3                ");
        System.out.println("        User Management & Loan Application Platform             ");
        System.out.println("================================================================");
    }

    // -------------------------------------------------------------------------
    // 1. User Registration Flow (Stage 1)
    // -------------------------------------------------------------------------
    private void handleRegistration() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                     USER REGISTRATION                          ");
        System.out.println("----------------------------------------------------------------");

        String fname = reader.readMandatoryString("Enter First Name (fname)*: ", "First Name");
        String lname = reader.readMandatoryString("Enter Last Name (lname)*: ", "Last Name");

        String mobile;
        while (true) {
            mobile = reader.readMandatoryString("Enter Mobile Number (10 digits)*: ", "Mobile Number");
            if (ValidationUtil.isValidMobile(mobile)) {
                break;
            }
            System.out.println(">> Error: Mobile number must be exactly 10 digits.");
        }

        String email;
        while (true) {
            email = reader.readMandatoryString("Enter Email Address (email)*: ", "Email Address");
            if (ValidationUtil.isValidEmail(email)) {
                break;
            }
            System.out.println(">> Error: Invalid email format. (e.g. name@domain.com)");
        }

        String location = reader.readLine("Enter Location (City/State): ");
        String username = reader.readMandatoryString("Enter Desired Username (username)*: ", "Username");

        String password;
        while (true) {
            System.out.println("\nPassword Policy: Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char.");
            password = reader.readMandatoryString("Enter Password*: ", "Password");
            if (ValidationUtil.isValidPassword(password)) {
                break;
            }
            System.out.println(">> Error: Password does not meet policy requirements!");
        }

        User newUser = new User(fname, lname, mobile, email, location, username, password);
        RegistrationResult result = userService.register(newUser);

        System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
    }

    // -------------------------------------------------------------------------
    // 2. Authentication & Login Flow (Stages 1 & 2)
    // -------------------------------------------------------------------------
    private void handleLogin() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                     AUTHENTICATION / LOGIN                     ");
        System.out.println("----------------------------------------------------------------");
        String identifier = reader.readMandatoryString("Enter Username OR Email: ", "Identifier");
        String password = reader.readMandatoryString("Enter Password: ", "Password");

        LoginResult result = userService.login(identifier, password);

        if (result.isSuccess()) {
            User user = result.getUser();
            System.out.println("\n================================================================");
            System.out.println(" " + result.getMessage());
            System.out.println("================================================================");

            if (user.isAdmin()) {
                showAdminDashboard(user);
            } else {
                showPostLoginMenu(user);
            }
        } else {
            System.out.println("\n[LOGIN FAILED] " + result.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 3. Regular User Post-Login Menu (Stages 1 & 3)
    // -------------------------------------------------------------------------
    private void showPostLoginMenu(User user) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n----------------------------------------------------------------");
            System.out.println(" Welcome, " + user.getFname() + " " + user.getLname());
            System.out.println(" Account Status: " + user.getStatus() + " | Role: " + user.getUserRole());
            System.out.println("----------------------------------------------------------------");
            System.out.println(" 1. View Profile Details");
            System.out.println(" 2. Apply for Loan (Stage 3 Active)");
            System.out.println(" 3. View My Loan Applications");
            System.out.println(" 4. Logout");
            System.out.println("----------------------------------------------------------------");
            int choice = reader.readInt("Select an option (1-4): ", 1, 4);

            switch (choice) {
                case 1 -> printUserProfile(user);
                case 2 -> handleUserApplyForLoan(user);
                case 3 -> handleUserViewMyLoans(user);
                case 4 -> {
                    System.out.println("\n>> Successfully logged out. Returning to main menu...");
                    inSession = false;
                }
            }
        }
    }

    private void handleUserApplyForLoan(User user) {
        System.out.println("\n================================================================");
        System.out.println("                    APPLY FOR A LOAN                            ");
        System.out.println("================================================================");
        System.out.println("Select Loan Type:");
        System.out.println(" 1. Personal Loan (10.5% interest rate)");
        System.out.println(" 2. Home Loan (7.5% interest rate)");
        System.out.println(" 3. Education Loan (5.0% interest rate)");
        System.out.println(" 4. Car / Vehicle Loan (8.0% interest rate)");
        int typeChoice = reader.readInt("Select Loan Type (1-4): ", 1, 4);

        String loanType = switch (typeChoice) {
            case 1 -> "Personal Loan";
            case 2 -> "Home Loan";
            case 3 -> "Education Loan";
            default -> "Vehicle Loan";
        };

        double interestRate = loanService.getDefaultInterestRate(loanType);
        System.out.printf(">> Selected: %s (Applied Interest Rate: %.2f%%)%n", loanType, interestRate);

        double amount;
        while (true) {
            System.out.print("Enter Requested Loan Amount ($): ");
            try {
                amount = Double.parseDouble(reader.readLine(""));
                if (amount > 0) break;
                System.out.println(">> Error: Loan amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Please enter a valid numerical amount.");
            }
        }

        int durationMonths = reader.readInt("Enter Loan Duration (in months, e.g. 12, 24, 60): ", 1, 480);

        double annualIncome;
        while (true) {
            System.out.print("Enter Your Annual Income ($): ");
            try {
                annualIncome = Double.parseDouble(reader.readLine(""));
                if (annualIncome > 0) break;
                System.out.println(">> Error: Annual income must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println(">> Error: Please enter a valid numerical income.");
            }
        }

        String purpose = reader.readMandatoryString("Enter Purpose of Loan: ", "Purpose");

        System.out.println("\n--- Loan Application Summary ---");
        System.out.printf(" Applicant     : %s%n", user.getFullName());
        System.out.printf(" Loan Type     : %s%n", loanType);
        System.out.printf(" Loan Amount   : $%,.2f%n", amount);
        System.out.printf(" Duration      : %d months%n", durationMonths);
        System.out.printf(" Interest Rate : %.2f%%%n", interestRate);
        System.out.printf(" Annual Income : $%,.2f%n", annualIncome);
        System.out.printf(" Purpose       : %s%n", purpose);

        String confirm = reader.readLine("\nSubmit this application? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm) || "y".equalsIgnoreCase(confirm)) {
            var result = loanService.applyForLoan(user, amount, loanType, durationMonths, annualIncome, purpose);
            System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
        } else {
            System.out.println("\n[CANCELLED] Loan application submission was cancelled.");
        }
    }

    private void handleUserViewMyLoans(User user) {
        System.out.println("\n--- My Loan Applications ---");
        List<LoanApplication> loans = loanService.getUserLoans(user.getId());
        if (loans.isEmpty()) {
            System.out.println("You have not submitted any loan applications yet.");
        } else {
            printLoanTable(loans);
        }
    }

    // -------------------------------------------------------------------------
    // 4. Admin Dashboard (Stages 2 & 3)
    // -------------------------------------------------------------------------
    private void showAdminDashboard(User admin) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n================================================================");
            System.out.println("                     ADMIN DASHBOARD                            ");
            System.out.println(" Logged in as: " + admin.getFullName() + " (" + admin.getUsername() + ")");
            System.out.println(" Role: " + admin.getUserRole() + " | Control Center");
            System.out.println("================================================================");
            System.out.println(" 1. Search Users");
            System.out.println(" 2. View User Details");
            System.out.println(" 3. Edit User Information");
            System.out.println(" 4. Activate User");
            System.out.println(" 5. Deactivate User");
            System.out.println(" 6. Delete User");
            System.out.println(" 7. Review Loan Applications (Stage 3 Active)");
            System.out.println(" 8. View Audit Logs");
            System.out.println(" 9. Logout");
            System.out.println("----------------------------------------------------------------");
            int choice = reader.readInt("Select an option (1-9): ", 1, 9);

            switch (choice) {
                case 1 -> handleAdminSearchUsers();
                case 2 -> handleAdminViewUserDetails();
                case 3 -> handleAdminEditUser(admin);
                case 4 -> handleAdminActivateUser(admin);
                case 5 -> handleAdminDeactivateUser(admin);
                case 6 -> handleAdminDeleteUser(admin);
                case 7 -> handleAdminReviewLoans(admin);
                case 8 -> handleAdminViewAuditLogs();
                case 9 -> {
                    System.out.println("\n>> Administrator logged out. Returning to main menu...");
                    inSession = false;
                }
            }
        }
    }

    private void handleAdminReviewLoans(User admin) {
        System.out.println("\n================================================================");
        System.out.println("                 LOAN APPLICATION REVIEW                        ");
        System.out.println("================================================================");
        List<LoanApplication> loans = loanService.getAllLoans(admin);
        if (loans.isEmpty()) {
            System.out.println("No loan applications submitted in the system.");
            return;
        }

        printLoanTable(loans);

        System.out.println("\nActions:");
        System.out.println(" 1. View Full Loan Application Details");
        System.out.println(" 2. Approve a Loan Application");
        System.out.println(" 3. Reject a Loan Application");
        System.out.println(" 4. Back to Dashboard");
        int action = reader.readInt("Select an action (1-4): ", 1, 4);

        switch (action) {
            case 1 -> {
                int loanId = reader.readInt("Enter Loan ID to view: ", 1, Integer.MAX_VALUE);
                Optional<LoanApplication> loanOpt = loanService.getLoanById(loanId);
                if (loanOpt.isEmpty()) {
                    System.out.println("\n[ERROR] Loan Application not found (ID: " + loanId + ").");
                } else {
                    printLoanDetails(loanOpt.get());
                }
            }
            case 2 -> {
                int loanId = reader.readInt("Enter Loan ID to APPROVE: ", 1, Integer.MAX_VALUE);
                var result = loanService.approveLoan(admin, loanId);
                System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
            }
            case 3 -> {
                int loanId = reader.readInt("Enter Loan ID to REJECT: ", 1, Integer.MAX_VALUE);
                String reason = reader.readMandatoryString("Enter mandatory Rejection Reason: ", "Rejection Reason");
                var result = loanService.rejectLoan(admin, loanId, reason);
                System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
            }
            case 4 -> {}
        }
    }

    private void handleAdminSearchUsers() {
        System.out.println("\n--- Search Users by Criteria ---");
        System.out.println(" 1. First Name");
        System.out.println(" 2. Last Name");
        System.out.println(" 3. Email Address");
        System.out.println(" 4. Mobile Number");
        System.out.println(" 5. Username");
        System.out.println(" 6. Status (Active / Inactive)");
        System.out.println(" 7. General Search (All fields)");
        int criteriaChoice = reader.readInt("Select criteria (1-7): ", 1, 7);

        String criteria = switch (criteriaChoice) {
            case 1 -> "fname";
            case 2 -> "lname";
            case 3 -> "email";
            case 4 -> "mobile";
            case 5 -> "username";
            case 6 -> "status";
            default -> "all";
        };

        String query = reader.readLine("Enter search keyword (or press Enter for all): ");
        List<User> users = adminService.searchUsers(criteria, query);

        if (users.isEmpty()) {
            System.out.println("\n[INFO] No users found matching the query.");
        } else {
            System.out.println("\nFound " + users.size() + " user(s):");
            printUserTable(users);
        }
    }

    private void handleAdminViewUserDetails() {
        int userId = reader.readInt("Enter User ID to view: ", 1, Integer.MAX_VALUE);
        Optional<User> userOpt = adminService.getUserById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("\n[ERROR] User not found with ID: " + userId);
        } else {
            printUserProfile(userOpt.get());
        }
    }

    private void handleAdminEditUser(User admin) {
        int userId = reader.readInt("Enter User ID to edit: ", 1, Integer.MAX_VALUE);
        Optional<User> userOpt = adminService.getUserById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("\n[ERROR] User not found with ID: " + userId);
            return;
        }

        User user = userOpt.get();
        System.out.println("\nEditing User: " + user.getUsername() + " (" + user.getFullName() + ")");
        System.out.println("(Press Enter without typing to keep current value)");

        String fname = reader.readLine("First Name [" + user.getFname() + "]: ");
        if (!fname.isEmpty()) user.setFname(fname);

        String lname = reader.readLine("Last Name [" + user.getLname() + "]: ");
        if (!lname.isEmpty()) user.setLname(lname);

        String mobile = reader.readLine("Mobile (10 digits) [" + user.getMobile() + "]: ");
        if (!mobile.isEmpty()) user.setMobile(mobile);

        String email = reader.readLine("Email [" + user.getEmail() + "]: ");
        if (!email.isEmpty()) user.setEmail(email);

        String location = reader.readLine("Location [" + (user.getLocation() != null ? user.getLocation() : "") + "]: ");
        if (!location.isEmpty()) user.setLocation(location);

        var result = adminService.editUser(admin, user);
        System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
    }

    private void handleAdminActivateUser(User admin) {
        int userId = reader.readInt("Enter User ID to activate: ", 1, Integer.MAX_VALUE);
        var result = adminService.activateUser(admin, userId);
        System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
    }

    private void handleAdminDeactivateUser(User admin) {
        int userId = reader.readInt("Enter User ID to deactivate: ", 1, Integer.MAX_VALUE);
        var result = adminService.deactivateUser(admin, userId);
        System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
    }

    private void handleAdminDeleteUser(User admin) {
        int userId = reader.readInt("Enter User ID to delete: ", 1, Integer.MAX_VALUE);
        Optional<User> userOpt = adminService.getUserById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("\n[ERROR] User not found with ID: " + userId);
            return;
        }

        User target = userOpt.get();
        System.out.println("\nTarget User: " + target.getUsername() + " | " + target.getFullName() + " | " + target.getEmail());
        String confirm = reader.readLine("Are you sure you want to PERMANENTLY delete this user? (yes/no): ");
        if ("yes".equalsIgnoreCase(confirm) || "y".equalsIgnoreCase(confirm)) {
            var result = adminService.deleteUser(admin, userId);
            System.out.println("\n" + (result.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + result.getMessage());
        } else {
            System.out.println("\n[CANCELLED] Deletion cancelled by administrator.");
        }
    }

    private void handleAdminViewAuditLogs() {
        System.out.println("\n--- Recent Administrative Audit Logs ---");
        List<AuditLog> logs = adminService.getRecentAuditLogs(20);
        if (logs.isEmpty()) {
            System.out.println("No audit log records recorded yet.");
        } else {
            for (AuditLog log : logs) {
                System.out.println(log);
            }
        }
    }

    private void printUserProfile(User user) {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println(" User ID       : " + user.getId());
        System.out.println(" Full Name     : " + user.getFullName());
        System.out.println(" Username      : " + user.getUsername());
        System.out.println(" Email         : " + user.getEmail());
        System.out.println(" Mobile        : " + user.getMobile());
        System.out.println(" Location      : " + (user.getLocation() != null ? user.getLocation() : "N/A"));
        System.out.println(" Account Status: " + user.getStatus());
        System.out.println(" User Role     : " + user.getUserRole());
        System.out.println(" Failed Logins : " + user.getFailedAttempts() + " / 3");
        System.out.println(" Last Modified : " + user.getLastModifiedDate());
        System.out.println(" Registered On : " + user.getCreatedAt());

        // Stage 3: Display associated loan applications
        List<LoanApplication> userLoans = loanService.getUserLoans(user.getId());
        System.out.println(" Loan Requests : " + userLoans.size() + " application(s)");
        if (!userLoans.isEmpty()) {
            for (LoanApplication l : userLoans) {
                System.out.printf("  * [Loan #%d] %s: $%,.2f (%d mo, %.1f%%) - Status: %s%n",
                        l.getId(), l.getLoanType(), l.getLoanAmount(), l.getLoanDuration(), l.getInterestRate(), l.getStatus());
            }
        }
        System.out.println("----------------------------------------------------------------");
    }

    private void printUserTable(List<User> users) {
        System.out.printf("%-4s | %-12s | %-20s | %-25s | %-12s | %-8s | %-10s%n",
                "ID", "Username", "Full Name", "Email", "Mobile", "Status", "Role");
        System.out.println("-".repeat(105));
        for (User u : users) {
            System.out.printf("%-4d | %-12s | %-20s | %-25s | %-12s | %-8s | %-10s%n",
                    u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getMobile(), u.getStatus(), u.getUserRole());
        }
    }

    private void printLoanTable(List<LoanApplication> loans) {
        System.out.printf("%-5s | %-18s | %-15s | %-14s | %-10s | %-8s | %-10s%n",
                "ID", "Applicant", "Loan Type", "Amount", "Duration", "Rate", "Status");
        System.out.println("-".repeat(95));
        for (LoanApplication l : loans) {
            System.out.printf("%-5d | %-18s | %-15s | $%,12.2f | %-7d mo | %-6.2f%% | %-10s%n",
                    l.getId(),
                    (l.getApplicantName() != null ? l.getApplicantName() : "User #" + l.getUserId()),
                    l.getLoanType(),
                    l.getLoanAmount(),
                    l.getLoanDuration(),
                    l.getInterestRate(),
                    l.getStatus());
        }
    }

    private void printLoanDetails(LoanApplication loan) {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                  LOAN APPLICATION DETAILS                      ");
        System.out.println("----------------------------------------------------------------");
        System.out.println(" Loan ID        : " + loan.getId());
        System.out.println(" Applicant Name : " + (loan.getApplicantName() != null ? loan.getApplicantName() : "N/A"));
        System.out.println(" Email Address  : " + (loan.getApplicantEmail() != null ? loan.getApplicantEmail() : "N/A"));
        System.out.println(" Mobile Number  : " + (loan.getApplicantMobile() != null ? loan.getApplicantMobile() : "N/A"));
        System.out.println(" Loan Type      : " + loan.getLoanType());
        System.out.printf(" Loan Amount    : $%,.2f%n", loan.getLoanAmount());
        System.out.printf(" Loan Duration  : %d months%n", loan.getLoanDuration());
        System.out.printf(" Interest Rate  : %.2f%%%n", loan.getInterestRate());
        System.out.printf(" Annual Income  : $%,.2f%n", loan.getAnnualIncome());
        System.out.println(" Purpose        : " + loan.getPurpose());
        System.out.println(" Status         : " + loan.getStatus());
        if (loan.getRejectionReason() != null) {
            System.out.println(" Rejection Reason: " + loan.getRejectionReason());
        }
        System.out.println(" Submitted Date : " + loan.getApplicationDate());
        System.out.println(" Last Modified  : " + loan.getLastModifiedDate());
        System.out.println("----------------------------------------------------------------");
    }

    // -------------------------------------------------------------------------
    // 5. Forget Password Flow (Stage 1)
    // -------------------------------------------------------------------------
    private void handleForgotPassword() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                      FORGET PASSWORD                           ");
        System.out.println("----------------------------------------------------------------");
        String identifier = reader.readMandatoryString("Enter registered Username OR Email: ", "Identifier");

        System.out.println("Verifying account...");
        var userOpt = userService.getUserByIdentifier(identifier);
        if (userOpt.isEmpty()) {
            System.out.println("\n[ERROR] No registered user found matching: " + identifier);
            return;
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            System.out.println("\n[LOCKED] This account is currently locked/inactive. Password reset is not permitted.");
            System.out.println("Please contact system support / administrator for account reactivation.");
            return;
        }

        System.out.println("User verified: " + user.getFullName() + " (" + user.getUsername() + ")");
        System.out.println("Please create a new password meeting policy requirements.");

        String newPassword;
        while (true) {
            newPassword = reader.readMandatoryString("Enter New Password: ", "New Password");
            if (ValidationUtil.isValidPassword(newPassword)) {
                break;
            }
            System.out.println(">> Error: Password must be min 8 chars with uppercase, lowercase, digit & special char.");
        }

        String confirmPassword = reader.readMandatoryString("Confirm New Password: ", "Confirm Password");
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("\n[ERROR] Passwords do not match. Password reset cancelled.");
            return;
        }

        PasswordResetResult resetResult = userService.resetPassword(identifier, newPassword);
        System.out.println("\n" + (resetResult.isSuccess() ? "[SUCCESS] " : "[FAILED] ") + resetResult.getMessage());
    }

    // -------------------------------------------------------------------------
    // 6. Database Information & Diagnostics
    // -------------------------------------------------------------------------
    private void handleDbInfo() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                 DATABASE DIAGNOSTICS & CONFIG                  ");
        System.out.println("----------------------------------------------------------------");
        System.out.println(" JDBC URL : " + DatabaseConfig.getUrl());
        System.out.println(" DB User  : " + DatabaseConfig.getUser());
        boolean connected = DatabaseConfig.testConnection();
        System.out.println(" Status   : " + (connected ? "CONNECTED (Ready)" : "DISCONNECTED (Check MySQL service & credentials)"));
        System.out.println(" Config file: shared/src/main/resources/db.properties");
    }
}
