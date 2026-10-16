package com.sit.loanapp.service;

import com.sit.loanapp.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete NotificationService implementation that formats and logs simulated emails to the console
 * while keeping an in-memory history of sent notifications for testing and verification.
 */
public class ConsoleNotificationService implements NotificationService {

    public record SentEmail(String toEmail, String subject, String body) {}

    private final List<SentEmail> sentEmails = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Stage 2: Account Notifications
    // -------------------------------------------------------------------------
    @Override
    public void sendActivationNotification(User user) {
        String body = String.format("Dear %s, your account has been successfully activated by the admin. You can now log in to the system.",
                user.getFname());
        dispatchEmail(user.getEmail(), EMAIL_SUBJECT_ACCOUNT, body);
    }

    @Override
    public void sendDeactivationNotification(User user) {
        String body = String.format("Dear %s, your account has been deactivated. Please contact support for more information.",
                user.getFname());
        dispatchEmail(user.getEmail(), EMAIL_SUBJECT_ACCOUNT, body);
    }

    @Override
    public void sendDeletionNotification(User user) {
        String body = String.format("Dear %s, your account has been deleted from the system. If this was a mistake, please contact support immediately.",
                user.getFname());
        dispatchEmail(user.getEmail(), EMAIL_SUBJECT_ACCOUNT, body);
    }

    @Override
    public void sendEditNotification(User user) {
        String body = String.format("Dear %s, your account details have been updated. If you did not request this change, please contact support immediately.",
                user.getFname());
        dispatchEmail(user.getEmail(), EMAIL_SUBJECT_ACCOUNT, body);
    }

    // -------------------------------------------------------------------------
    // Stage 3: Loan Notifications
    // -------------------------------------------------------------------------
    @Override
    public void sendLoanApprovedNotification(String userEmail, String userName, double loanAmount) {
        String body = String.format("Dear %s, your loan application for $%,.2f has been approved. Please check your dashboard for further details.",
                userName, loanAmount);
        dispatchEmail(userEmail, EMAIL_SUBJECT_LOAN_APPROVED, body);
    }

    @Override
    public void sendLoanRejectedNotification(String userEmail, String userName, String rejectionReason) {
        String body = String.format("Dear %s, we regret to inform you that your loan application has been rejected. Reason: %s. Please contact support for more details.",
                userName, (rejectionReason != null ? rejectionReason : "Application criteria not met"));
        dispatchEmail(userEmail, EMAIL_SUBJECT_LOAN_REJECTED, body);
    }

    private void dispatchEmail(String toEmail, String subject, String body) {
        SentEmail email = new SentEmail(toEmail, subject, body);
        sentEmails.add(email);

        System.out.println("\n+----------------------------------------------------------------+");
        System.out.println("| [EMAIL NOTIFICATION DISPATCHED]                                |");
        System.out.println("| To      : " + toEmail);
        System.out.println("| Subject : " + subject);
        System.out.println("| Message : " + body);
        System.out.println("+----------------------------------------------------------------+");
    }

    public List<SentEmail> getSentEmails() {
        return Collections.unmodifiableList(sentEmails);
    }

    public void clearHistory() {
        sentEmails.clear();
    }
}
