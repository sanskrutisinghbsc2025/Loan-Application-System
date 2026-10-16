package com.sit.loanapp.model;

import java.sql.Timestamp;

/**
 * Represents an audit record for administrative actions (Stage 2).
 */
public class AuditLog {

    private int id;
    private String adminUsername;
    private String action;
    private Integer targetUserId;
    private String targetUsername;
    private String details;
    private Timestamp createdAt;

    public AuditLog() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public AuditLog(String adminUsername, String action, Integer targetUserId, String targetUsername, String details) {
        this();
        this.adminUsername = adminUsername;
        this.action = action;
        this.targetUserId = targetUserId;
        this.targetUsername = targetUsername;
        this.details = details;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Integer targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUsername() {
        return targetUsername;
    }

    public void setTargetUsername(String targetUsername) {
        this.targetUsername = targetUsername;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] Action: %s | Admin: %s | Target: %s (ID: %s) | Details: %s",
                createdAt, action, adminUsername, targetUsername, targetUserId, details);
    }
}
