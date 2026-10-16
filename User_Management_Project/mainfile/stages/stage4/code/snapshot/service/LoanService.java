package com.sit.loanapp.service;

import com.sit.loanapp.model.LoanApplication;
import com.sit.loanapp.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing loan application workflows, interest rates,
 * approvals, rejections, and notifications (Stage 3).
 */
public interface LoanService {

    class LoanSubmissionResult {
        private final boolean success;
        private final String message;
        private final LoanApplication application;

        public LoanSubmissionResult(boolean success, String message, LoanApplication application) {
            this.success = success;
            this.message = message;
            this.application = application;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public LoanApplication getApplication() {
            return application;
        }
    }

    class LoanActionResult {
        private final boolean success;
        private final String message;

        public LoanActionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Retrieves the standard interest rate based on loan type.
     */
    double getDefaultInterestRate(String loanType);

    /**
     * Submits a new loan application for an authenticated user.
     */
    LoanSubmissionResult applyForLoan(User applicant, double amount, String loanType,
                                      int durationMonths, double annualIncome, String purpose);

    /**
     * Retrieves all loan applications submitted by a user.
     */
    List<LoanApplication> getUserLoans(int userId);

    /**
     * Retrieves all loan applications for administrator review.
     */
    List<LoanApplication> getAllLoans(User admin);

    /**
     * Retrieves single loan application by ID.
     */
    Optional<LoanApplication> getLoanById(int loanId);

    /**
     * Approves a loan application, updates DB, sends approved email, and logs audit record.
     */
    LoanActionResult approveLoan(User admin, int loanId);

    /**
     * Rejects a loan application with a required reason, updates DB, sends rejection email, and logs audit record.
     */
    LoanActionResult rejectLoan(User admin, int loanId, String reason);
}
