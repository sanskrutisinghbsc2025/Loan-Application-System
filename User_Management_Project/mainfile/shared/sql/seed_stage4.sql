-- =============================================================================
-- Loan Application System - Stage 4 Seed Data
-- =============================================================================

USE loan_app_db;

-- Update sample users with PAN and CIBIL score
UPDATE users SET pancard = 'ABCDE1234F', cibil_score = 750 WHERE username = 'amrutap';
UPDATE users SET pancard = 'BKWPR5432M', cibil_score = 680 WHERE username = 'johndoe';
UPDATE users SET pancard = 'CKLPS9876T', cibil_score = 610 WHERE username = 'sarahs';
