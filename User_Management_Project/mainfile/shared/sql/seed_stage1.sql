-- =============================================================================
-- Loan Application System - Stage 1 Seed Data
-- =============================================================================

USE loan_app_db;

-- Sample Active User (matching the Stage 1 specification example: "Welcome Amruta Patil")
-- Password: "Password@123" (meets >=8 chars, uppercase, lowercase, digit, special char)
INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, status, username, password, failed_attempts, user_role)
VALUES 
('Amruta', 'Patil', '9876543210', 'amruta.patil@example.com', 'Pune', NOW(), 'Active', 'amrutap', 'Password@123', 0, 'USER')
ON DUPLICATE KEY UPDATE fname=VALUES(fname);

-- Sample User for Testing Account Lockout (2 failed attempts already recorded)
-- Password: "Secure@2024"
INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, status, username, password, failed_attempts, user_role)
VALUES 
('John', 'Doe', '9123456780', 'john.doe@example.com', 'Mumbai', NOW(), 'Active', 'johndoe', 'Secure@2024', 2, 'USER')
ON DUPLICATE KEY UPDATE fname=VALUES(fname);

-- Sample Pre-Locked / Inactive User (3 failed attempts, status Inactive)
-- Password: "Locked@123"
INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, status, username, password, failed_attempts, user_role)
VALUES 
('Sarah', 'Smith', '9988776655', 'sarah.smith@example.com', 'Bangalore', NOW(), 'Inactive', 'sarahs', 'Locked@123', 3, 'USER')
ON DUPLICATE KEY UPDATE fname=VALUES(fname);
