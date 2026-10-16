package com.sit.loanapp.model;

import java.sql.Timestamp;

/**
 * Domain entity representing a loan application in Stage 3.
 */
public class LoanApplication {

    public static final String STATUS_PENDING = "Pending";
    public static final String STATUS_APPROVED = "Approved";
    public static final String STATUS_REJECTED = "Rejected";
    public static final String STATUS_NEEDS_MORE_INFO = "Needs More Info";

    private int id;
    private int userId;
    private String applicantName;
    private String applicantEmail;
    private String applicantMobile;
    private double loanAmount;
    private String loanType;
    private int loanDuration; // Duration in months
    private double interestRate;
    private double annualIncome;
    private String purpose;
    private String status;
    private String rejectionReason;
    private Timestamp applicationDate;
    private Timestamp lastModifiedDate;

    public LoanApplication() {
        this.status = STATUS_PENDING;
        this.applicationDate = new Timestamp(System.currentTimeMillis());
        this.lastModifiedDate = new Timestamp(System.currentTimeMillis());
    }

    public LoanApplication(int userId, double loanAmount, String loanType, int loanDuration,
                           double interestRate, double annualIncome, String purpose) {
        this();
        this.userId = userId;
        this.loanAmount = loanAmount;
        this.loanType = loanType;
        this.loanDuration = loanDuration;
        this.interestRate = interestRate;
        this.annualIncome = annualIncome;
        this.purpose = purpose;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplicantEmail() {
        return applicantEmail;
    }

    public void setApplicantEmail(String applicantEmail) {
        this.applicantEmail = applicantEmail;
    }

    public String getApplicantMobile() {
        return applicantMobile;
    }

    public void setApplicantMobile(String applicantMobile) {
        this.applicantMobile = applicantMobile;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public int getLoanDuration() {
        return loanDuration;
    }

    public void setLoanDuration(int loanDuration) {
        this.loanDuration = loanDuration;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public double getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(double annualIncome) {
        this.annualIncome = annualIncome;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Timestamp getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Timestamp applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Timestamp getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Timestamp lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return String.format("LoanApplication[ID=%d, User=%s, Amount=%.2f, Type=%s, Duration=%d mo, Rate=%.2f%%, Status=%s]",
                id, (applicantName != null ? applicantName : "UserID:" + userId), loanAmount, loanType, loanDuration, interestRate, status);
    }
}
