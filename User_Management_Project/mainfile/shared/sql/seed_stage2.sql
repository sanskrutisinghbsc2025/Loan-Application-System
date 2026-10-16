-- =============================================================================
-- Loan Application System - Stage 2 Seed Data
-- =============================================================================

USE loan_app_db;

-- Predefined Administrator Account (Stage 2 Requirement)
-- Username: admin | Password: Admin@1234 | Role: ROLE_ADMIN
INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, status, username, password, failed_attempts, user_role)
VALUES 
('System', 'Administrator', '9999999999', 'admin@bank.com', 'Headquarters', NOW(), 'Active', 'admin', 'Admin@1234', 0, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE user_role='ROLE_ADMIN', status='Active';

-- Sample Regular Users for Admin Operations & Search Testing
INSERT INTO users (fname, lname, mobile, email, location, lastmodifieddate, status, username, password, failed_attempts, user_role)
VALUES 
('Amruta', 'Patil', '9876543210', 'amruta.patil@example.com', 'Pune', NOW(), 'Active', 'amrutap', 'Password@123', 0, 'ROLE_USER'),
('John', 'Doe', '9123456780', 'john.doe@example.com', 'Mumbai', NOW(), 'Active', 'johndoe', 'Secure@2024', 2, 'ROLE_USER'),
('Sarah', 'Smith', '9988776655', 'sarah.smith@example.com', 'Bangalore', NOW(), 'Inactive', 'sarahs', 'Locked@123', 3, 'ROLE_USER'),
('Rahul', 'Sharma', '9812345678', 'rahul.sharma@example.com', 'Delhi', NOW(), 'Active', 'rahuls', 'Pass@1234', 0, 'ROLE_USER')
ON DUPLICATE KEY UPDATE fname=VALUES(fname);
