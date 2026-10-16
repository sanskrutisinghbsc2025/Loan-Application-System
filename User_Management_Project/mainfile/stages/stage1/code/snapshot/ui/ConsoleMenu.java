package com.sit.loanapp.ui;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.model.User;
import com.sit.loanapp.service.UserService;
import com.sit.loanapp.service.UserService.LoginResult;
import com.sit.loanapp.service.UserService.PasswordResetResult;
import com.sit.loanapp.service.UserService.RegistrationResult;
import com.sit.loanapp.service.UserServiceImpl;
import com.sit.loanapp.util.ValidationUtil;

import java.util.Scanner;

/**
 * Interactive Console Interface for Stage 1 User Management System.
 * Uses Scanner for all input operations.
 */
public class ConsoleMenu {

    private final UserService userService;
    private final InputReader reader;

    public ConsoleMenu(Scanner scanner) {
        this.userService = new UserServiceImpl();
        this.reader = new InputReader(scanner);
    }

    public ConsoleMenu(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.reader = new InputReader(scanner);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printMainMenuHeader();
            System.out.println(" 1. Register New User");
            System.out.println(" 2. User Login");
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
        System.out.println("               LOAN APPLICATION SYSTEM - STAGE 1                ");
        System.out.println("                   User Management Console                      ");
        System.out.println("================================================================");
    }

    // -------------------------------------------------------------------------
    // 1. User Registration Flow
    // -------------------------------------------------------------------------
    private void handleRegistration() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                     USER REGISTRATION                          ");
        System.out.println("----------------------------------------------------------------");

        String fname = reader.readMandatoryString("Enter First Name (fname)*: ", "First Name");
        String lname = reader.readMandatoryString("Enter Last Name (lname)*: ", "Last Name");

        // Mobile validation loop
        String mobile;
        while (true) {
            mobile = reader.readMandatoryString("Enter Mobile Number (10 digits)*: ", "Mobile Number");
            if (ValidationUtil.isValidMobile(mobile)) {
                break;
            }
            System.out.println(">> Error: Mobile number must be exactly 10 digits.");
        }

        // Email validation loop
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

        // Password validation loop with policy guidance
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
    // 2. User Login Flow
    // -------------------------------------------------------------------------
    private void handleLogin() {
        System.out.println("\n----------------------------------------------------------------");
        System.out.println("                         USER LOGIN                             ");
        System.out.println("----------------------------------------------------------------");
        String identifier = reader.readMandatoryString("Enter Username OR Email: ", "Identifier");
        String password = reader.readMandatoryString("Enter Password: ", "Password");

        LoginResult result = userService.login(identifier, password);

        if (result.isSuccess()) {
            User user = result.getUser();
            System.out.println("\n================================================================");
            System.out.println(" " + result.getMessage()); // Example: "Welcome Amruta Patil"
            System.out.println("================================================================");
            showPostLoginMenu(user);
        } else {
            System.out.println("\n[LOGIN FAILED] " + result.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 3. Post-Login Menu Flow
    // -------------------------------------------------------------------------
    private void showPostLoginMenu(User user) {
        boolean inSession = true;
        while (inSession) {
            System.out.println("\n----------------------------------------------------------------");
            System.out.println(" Welcome, " + user.getFname() + " " + user.getLname());
            System.out.println(" Account Status: " + user.getStatus() + " | Role: " + user.getUserRole());
            System.out.println("----------------------------------------------------------------");
            System.out.println(" 1. View Profile Details");
            System.out.println(" 2. Apply for Loan (Available in Stage 3)");
            System.out.println(" 3. Logout");
            System.out.println("----------------------------------------------------------------");
            int choice = reader.readInt("Select an option (1-3): ", 1, 3);

            switch (choice) {
                case 1 -> {
                    System.out.println("\n--- USER PROFILE ---");
                    System.out.println(" Full Name     : " + user.getFullName());
                    System.out.println(" Username      : " + user.getUsername());
                    System.out.println(" Email         : " + user.getEmail());
                    System.out.println(" Mobile        : " + user.getMobile());
                    System.out.println(" Location      : " + (user.getLocation() != null ? user.getLocation() : "N/A"));
                    System.out.println(" Account Status: " + user.getStatus());
                    System.out.println(" Last Modified : " + user.getLastModifiedDate());
                    System.out.println(" Member Since  : " + user.getCreatedAt());
                }
                case 2 -> {
                    System.out.println("\n>> Loan Application module will be unlocked in Stage 3.");
                }
                case 3 -> {
                    System.out.println("\n>> Successfully logged out. Returning to main menu...");
                    inSession = false;
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // 4. Forget Password Flow
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
            System.out.println("Please contact system support for account reactivation.");
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
    // 5. Database Information & Diagnostics
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
