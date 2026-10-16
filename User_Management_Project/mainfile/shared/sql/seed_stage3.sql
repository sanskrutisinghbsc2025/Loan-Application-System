-- =============================================================================
-- Loan Application System - Stage 3 Seed Data
-- =============================================================================

USE loan_app_db;

-- Insert sample loan applications linked to users created in seed_stage2
INSERT INTO loan_applications (user_id, loan_amount, loan_type, loan_duration, interest_rate, annual_income, purpose, status, application_date, last_modified_date)
SELECT u.id, 500000.0, 'Home Loan', 240, 7.5, 1200000.0, 'Purchasing residential apartment in Pune', 'Pending', NOW(), NOW()
FROM users u WHERE u.username = 'amrutap'
LIMIT 1;

INSERT INTO loan_applications (user_id, loan_amount, loan_type, loan_duration, interest_rate, annual_income, purpose, status, application_date, last_modified_date)
SELECT u.id, 150000.0, 'Personal Loan', 36, 10.5, 650000.0, 'Home renovation and medical expenses', 'Approved', NOW(), NOW()
FROM users u WHERE u.username = 'amrutap'
LIMIT 1;

INSERT INTO loan_applications (user_id, loan_amount, loan_type, loan_duration, interest_rate, annual_income, purpose, status, application_date, last_modified_date)
SELECT u.id, 350000.0, 'Education Loan', 48, 5.0, 480000.0, 'Higher education tuition fees', 'Pending', NOW(), NOW()
FROM users u WHERE u.username = 'johndoe'
LIMIT 1;
