-- =============================================================================
-- Stage 5 Seed Data: Loan Approval with EMI Calculation and Notification
-- =============================================================================

USE loan_app_db;

-- Update existing loan #1 or insert an approved sample loan with EMI data
-- Example: John Doe (User ID 2) Home Loan:
-- Principal = $250,000.00, Rate = 7.5%, Duration = 120 months (10 years)
-- Monthly Rate = (7.5 / 12) / 100 = 0.00625
-- factor = (1 + 0.00625)^120 = 2.11206466
-- EMI = [250000 * 0.00625 * 2.11206466] / [2.11206466 - 1] = 3300.10103 / 1.11206466 = $2,967.54
-- Total Payable = $2,967.54 * 120 = $356,105.37
-- Total Interest = $356,105.37 - $250,000.00 = $106,105.37

INSERT INTO loan_applications (
    user_id, loan_amount, loan_type, loan_duration, interest_rate, annual_income,
    purpose, status, rejection_reason, emi_amount, total_interest, total_payable,
    application_date, last_modified_date
) VALUES (
    2, 250000.00, 'Home Loan', 120, 7.5, 95000.00,
    'Purchasing new primary residence', 'Approved', NULL, 2967.54, 106105.37, 356105.37,
    NOW(), NOW()
);

-- Seed audit log record for Stage 5 loan approval
INSERT INTO audit_logs (
    admin_username, action, target_user_id, target_username, details, created_at
) VALUES (
    'admin', 'APPROVE_LOAN', 2, 'John Doe',
    'Approved Home Loan for $250,000.00 (120 mo @ 7.50%). Calculated EMI: $2,967.54/mo, Total Payable: $356,105.37',
    NOW()
);
