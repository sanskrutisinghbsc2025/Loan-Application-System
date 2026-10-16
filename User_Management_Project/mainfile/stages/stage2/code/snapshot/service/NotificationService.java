package com.sit.loanapp.service;

import com.sit.loanapp.model.User;

/**
 * Interface defining notification dispatch mechanisms for administrative actions.
 * Strictly implements the Stage 2 email subject and template requirements.
 */
public interface NotificationService {

    String EMAIL_SUBJECT = "Account Update Notification";

    /**
     * Dispatches notification upon account activation.
     */
    void sendActivationNotification(User user);

    /**
     * Dispatches notification upon account deactivation.
     */
    void sendDeactivationNotification(User user);

    /**
     * Dispatches notification upon account deletion.
     */
    void sendDeletionNotification(User user);

    /**
     * Dispatches notification upon user details update.
     */
    void sendEditNotification(User user);
}
