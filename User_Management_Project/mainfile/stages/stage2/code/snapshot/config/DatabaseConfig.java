package com.sit.loanapp.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connection configurations and provides JDBC connections.
 * Supports db.properties, environment variables, and fallback settings.
 */
public class DatabaseConfig {

    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();

        // 1. Try loading from working directory db.properties
        File localFile = new File("db.properties");
        boolean loaded = false;
        if (localFile.exists()) {
            try (InputStream is = new FileInputStream(localFile)) {
                props.load(is);
                loaded = true;
            } catch (Exception ignored) {}
        }

        // 2. Fall back to classpath db.properties
        if (!loaded) {
            try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    loaded = true;
                }
            } catch (Exception ignored) {}
        }

        // 3. Populate settings with environment variable precedence
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        url = (envUrl != null && !envUrl.trim().isEmpty()) 
                ? envUrl 
                : props.getProperty("db.url", "jdbc:mysql://localhost:3306/loan_app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");

        user = (envUser != null) 
                ? envUser 
                : props.getProperty("db.user", "root");

        password = (envPass != null) 
                ? envPass 
                : props.getProperty("db.password", "");

        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            // Driver will be dynamically checked during getConnection
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getConnection(String customUrl, String customUser, String customPass) throws SQLException {
        return DriverManager.getConnection(customUrl, customUser, customPass);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    public static void setCustomConfig(String customUrl, String customUser, String customPass) {
        url = customUrl;
        user = customUser;
        password = customPass;
    }
}
