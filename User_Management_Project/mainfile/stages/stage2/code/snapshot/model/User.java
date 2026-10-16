package com.sit.loanapp.model;

import java.sql.Timestamp;

/**
 * Represents a user in the Loan Application System.
 * Supports Stage 1 user attributes and Stage 2 role differentiation (ROLE_USER vs ROLE_ADMIN).
 */
public class User {

    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    private int id;
    private String fname;
    private String lname;
    private String mobile;
    private String email;
    private String location;
    private Timestamp lastModifiedDate;
    private String status;
    private String username;
    private String password;
    private int failedAttempts;
    private String userRole;
    private Timestamp createdAt;

    public User() {
        this.status = "Active";
        this.failedAttempts = 0;
        this.userRole = ROLE_USER;
        this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
    }

    public User(String fname, String lname, String mobile, String email, String location,
                String username, String password) {
        this();
        this.fname = fname;
        this.lname = lname;
        this.mobile = mobile;
        this.email = email;
        this.location = location;
        this.username = username;
        this.password = password;
    }

    public User(String fname, String lname, String mobile, String email, String location,
                String username, String password, String userRole) {
        this(fname, lname, mobile, email, location, username, password);
        this.userRole = userRole;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getFullName() {
        return fname + " " + lname;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(this.status);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.userRole) || "ADMIN".equalsIgnoreCase(this.userRole);
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", mobile='" + mobile + '\'' +
                ", email='" + email + '\'' +
                ", location='" + location + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", status='" + status + '\'' +
                ", username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", failedAttempts=" + failedAttempts +
                ", userRole='" + userRole + '\'' +
                '}';
    }
}
