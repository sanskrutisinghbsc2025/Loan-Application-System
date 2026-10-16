package com.sit.loanapp;

import com.sit.loanapp.config.DatabaseConfig;
import com.sit.loanapp.ui.ConsoleMenu;

import java.util.Scanner;

/**
 * Main application entry point for the Loan Application System (Stage 1).
 */
public class Main {

    public static void main(String[] args) {
        printBanner();

        // Check database connection at startup and inform user
        boolean dbConnected = DatabaseConfig.testConnection();
        if (!dbConnected) {
            System.out.println("(!) Notice: Unable to connect to MySQL database at startup.");
            System.out.println("    Target URL: " + DatabaseConfig.getUrl());
            System.out.println("    Please ensure MySQL/MariaDB is running and credentials in");
            System.out.println("    shared/src/main/resources/db.properties are configured.");
            System.out.println("    (The application will continue so you can inspect menus or setup DB)");
        } else {
            System.out.println("[DB] Connected successfully to MySQL database (" + DatabaseConfig.getUrl() + ")");
        }

        try (Scanner scanner = new Scanner(System.in)) {
            ConsoleMenu menu = new ConsoleMenu(scanner);
            menu.start();
        } catch (Exception e) {
            System.err.println("Fatal Application Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printBanner() {
        System.out.println("****************************************************************");
        System.out.println("*            LOAN APPLICATION SYSTEM - STAGE 1                 *");
        System.out.println("*          User Management & Authentication Platform           *");
        System.out.println("*               Core Java / JDBC / MySQL                       *");
        System.out.println("****************************************************************");
    }
}
