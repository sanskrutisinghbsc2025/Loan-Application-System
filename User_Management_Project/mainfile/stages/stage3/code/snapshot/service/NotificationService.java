package com.sit.loanapp.service;

import com.sit.loanapp.model.LoanApplication;
import com.sit.loanapp.model.User;

/**
 * Interface defining notification dispatch mechanisms for administrative and loan actions.
 * Strictly adheres to the Stage 2 & Stage 3 email subject and template specifications.
 */
public interface NotificationService {

    String EMAIL_SUBJECT_ACCOUNT = "Account Update Notification";
    String EMAIL_SUBJECT_LOAN_APPROVED = "Your Loan Application has been Approved!";
    String EMAIL_SUBJECT_LOAN_REJECTED = "Your Loan Application has been Rejected";

    // Stage 2 Account Notifications
    void sendActivationNotification(User user);
    void sendDeactivationNotification(User user);
    void sendDeletionNotification(User user);
    void sendEditNotification(User user);

    // Stage 3 Loan Notifications
    void sendLoanApprovedNotification(String userEmail, String userName, double loanAmount);
    void sendLoanRejectedNotification(String userEmail, String userName, String rejectionReason);
}
