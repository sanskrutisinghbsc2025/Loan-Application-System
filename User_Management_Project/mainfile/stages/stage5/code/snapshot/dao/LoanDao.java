package com.sit.loanapp.dao;

import com.sit.loanapp.model.LoanApplication;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for LoanApplication persistence operations.
 */
public interface LoanDao {

    /**
     * Persists a newly submitted loan application into the database.
     */
    boolean applyForLoan(LoanApplication app);

    /**
     * Retrieves loan application by its primary key ID, joined with user details.
     */
    Optional<LoanApplication> findById(int id);

    /**
     * Retrieves all loan applications submitted by a specific user.
     */
    List<LoanApplication> findByUserId(int userId);

    /**
     * Retrieves all loan applications across all users (for admin review).
     */
    List<LoanApplication> findAll();

    /**
     * Retrieves all loan applications filtered by status (e.g. 'Pending').
     */
    List<LoanApplication> findByStatus(String status);

    /**
     * Updates loan status ('Approved', 'Rejected') and records rejection reason if applicable.
     */
    boolean updateLoanStatus(int loanId, String status, String rejectionReason);

    /**
     * Approves a loan application and persists calculated EMI, total interest, and total payable amount (Stage 5).
     */
    boolean updateLoanApproval(int loanId, double emiAmount, double totalInterest, double totalPayable);
}
