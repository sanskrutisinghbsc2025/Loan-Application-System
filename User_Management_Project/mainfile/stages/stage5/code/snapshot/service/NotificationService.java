package com.sit.loanapp.service;

import com.sit.loanapp.model.User;

/**
 * Interface defining notification dispatch mechanisms across all stages.
 */
public interface NotificationService {

    String EMAIL_SUBJECT_ACCOUNT = "Account Update Notification";
    String EMAIL_SUBJECT_LOAN_APPROVED = "Your Loan Application has been Approved!";
    String EMAIL_SUBJECT_LOAN_REJECTED = "Your Loan Application has been Rejected";
    String EMAIL_SUBJECT_CREDENTIALS = "Your Username and Password for Loan Application is";

    // Stage 2 Account Notifications
    void sendActivationNotification(User user);
    void sendDeactivationNotification(User user);
    void sendDeletionNotification(User user);
    void sendEditNotification(User user);

    // Stage 3 & 5 Loan Notifications
    void sendLoanApprovedNotification(String userEmail, String userName, double loanAmount);
    void sendLoanApprovedNotification(String userEmail, String userName, com.sit.loanapp.model.LoanApplication loan);
    void sendLoanRejectedNotification(String userEmail, String userName, String rejectionReason);

    // Stage 4 Credentials Notification
    void sendCredentialsNotification(String userEmail, String userFname, String username, String password);
}
