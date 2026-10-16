package com.sit.loanapp.service;

import com.sit.loanapp.dao.LoanDao;
import com.sit.loanapp.dao.LoanDaoImpl;
import com.sit.loanapp.dao.UserDao;
import com.sit.loanapp.dao.UserDaoImpl;
import com.sit.loanapp.model.AuditLog;
import com.sit.loanapp.model.LoanApplication;
import com.sit.loanapp.model.User;
import com.sit.loanapp.util.ValidationUtil;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of LoanService managing loan submissions, interest rates,
 * admin review workflows, email notifications, and audit logging.
 */
public class LoanServiceImpl implements LoanService {

    private final LoanDao loanDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public LoanServiceImpl() {
        this(new LoanDaoImpl(), new UserDaoImpl(), new ConsoleNotificationService());
    }

    public LoanServiceImpl(LoanDao loanDao, UserDao userDao, NotificationService notificationService) {
        this.loanDao = loanDao;
        this.userDao = userDao;
        this.notificationService = notificationService;
    }

    @Override
    public double getDefaultInterestRate(String loanType) {
        if (loanType == null) return 8.5;
        String normalized = loanType.trim().toLowerCase();
        if (normalized.contains("personal")) {
            return 10.5;
        } else if (normalized.contains("home")) {
            return 7.5;
        } else if (normalized.contains("education")) {
            return 5.0;
        } else if (normalized.contains("car") || normalized.contains("vehicle")) {
            return 8.0;
        }
        return 9.0;
    }

    @Override
    public LoanSubmissionResult applyForLoan(User applicant, double amount, String loanType,
                                              int durationMonths, double annualIncome, String purpose) {
        if (applicant == null) {
            return new LoanSubmissionResult(false, "User session is invalid. Please log in again.", null);
        }

        // Validations
        if (amount <= 0) {
            return new LoanSubmissionResult(false, "Loan amount must be a positive number greater than 0.", null);
        }
        if (durationMonths <= 0) {
            return new LoanSubmissionResult(false, "Loan duration must be greater than 0 months.", null);
        }
        if (annualIncome <= 0) {
            return new LoanSubmissionResult(false, "Annual income must be a positive number greater than 0.", null);
        }
        if (ValidationUtil.isBlank(loanType)) {
            return new LoanSubmissionResult(false, "Loan type is mandatory.", null);
        }
        if (ValidationUtil.isBlank(purpose)) {
            return new LoanSubmissionResult(false, "Purpose of loan is mandatory.", null);
        }

        double rate = getDefaultInterestRate(loanType);
        LoanApplication app = new LoanApplication(applicant.getId(), amount, loanType, durationMonths, rate, annualIncome, purpose.trim());

        boolean saved = loanDao.applyForLoan(app);
        if (saved) {
            return new LoanSubmissionResult(true, "Your loan application for $" + String.format("%,.2f", amount) + " has been submitted successfully with status 'Pending'.", app);
        } else {
            return new LoanSubmissionResult(false, "Failed to persist loan application in the database.", null);
        }
    }

    @Override
    public List<LoanApplication> getUserLoans(int userId) {
        return loanDao.findByUserId(userId);
    }

    @Override
    public List<LoanApplication> getAllLoans(User admin) {
        if (admin == null || !admin.isAdmin()) {
            return Collections.emptyList();
        }
        return loanDao.findAll();
    }

    @Override
    public Optional<LoanApplication> getLoanById(int loanId) {
        return loanDao.findById(loanId);
    }

    @Override
    public LoanActionResult approveLoan(User admin, int loanId) {
        if (admin == null || !admin.isAdmin()) {
            return new LoanActionResult(false, "Access Denied: Only administrators can approve loans.");
        }

        Optional<LoanApplication> loanOpt = loanDao.findById(loanId);
        if (loanOpt.isEmpty()) {
            return new LoanActionResult(false, "Loan application not found (ID: " + loanId + ").");
        }

        LoanApplication loan = loanOpt.get();
        if (LoanApplication.STATUS_APPROVED.equalsIgnoreCase(loan.getStatus())) {
            return new LoanActionResult(false, "Loan application #" + loanId + " is already Approved.");
        }

        boolean updated = loanDao.updateLoanStatus(loanId, LoanApplication.STATUS_APPROVED, null);
        if (updated) {
            loan.setStatus(LoanApplication.STATUS_APPROVED);

            // Fetch user info for notification
            String recipientEmail = loan.getApplicantEmail();
            String recipientName = loan.getApplicantName();
            if (recipientEmail == null || recipientName == null) {
                userDao.findById(loan.getUserId()).ifPresent(u -> {
                    loan.setApplicantEmail(u.getEmail());
                    loan.setApplicantName(u.getFullName());
                });
                recipientEmail = loan.getApplicantEmail();
                recipientName = loan.getApplicantName();
            }

            // Dispatch Stage 3 approved email notification
            notificationService.sendLoanApprovedNotification(recipientEmail, recipientName, loan.getLoanAmount());

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "APPROVE_LOAN",
                    loan.getUserId(),
                    recipientName,
                    String.format("Approved %s (Loan ID: %d) for $%,.2f", loan.getLoanType(), loanId, loan.getLoanAmount())
            ));

            return new LoanActionResult(true, "Loan Application #" + loanId + " approved successfully.");
        }
        return new LoanActionResult(false, "Database error occurred while approving loan.");
    }

    @Override
    public LoanActionResult rejectLoan(User admin, int loanId, String reason) {
        if (admin == null || !admin.isAdmin()) {
            return new LoanActionResult(false, "Access Denied: Only administrators can reject loans.");
        }

        if (ValidationUtil.isBlank(reason)) {
            return new LoanActionResult(false, "Rejection reason is mandatory.");
        }

        Optional<LoanApplication> loanOpt = loanDao.findById(loanId);
        if (loanOpt.isEmpty()) {
            return new LoanActionResult(false, "Loan application not found (ID: " + loanId + ").");
        }

        LoanApplication loan = loanOpt.get();
        boolean updated = loanDao.updateLoanStatus(loanId, LoanApplication.STATUS_REJECTED, reason.trim());
        if (updated) {
            loan.setStatus(LoanApplication.STATUS_REJECTED);
            loan.setRejectionReason(reason.trim());

            // Fetch user info for notification
            String recipientEmail = loan.getApplicantEmail();
            String recipientName = loan.getApplicantName();
            if (recipientEmail == null || recipientName == null) {
                userDao.findById(loan.getUserId()).ifPresent(u -> {
                    loan.setApplicantEmail(u.getEmail());
                    loan.setApplicantName(u.getFullName());
                });
                recipientEmail = loan.getApplicantEmail();
                recipientName = loan.getApplicantName();
            }

            // Dispatch Stage 3 rejected email notification
            notificationService.sendLoanRejectedNotification(recipientEmail, recipientName, reason.trim());

            // Record audit log
            userDao.logAudit(new AuditLog(
                    admin.getUsername(),
                    "REJECT_LOAN",
                    loan.getUserId(),
                    recipientName,
                    String.format("Rejected %s (Loan ID: %d). Reason: %s", loan.getLoanType(), loanId, reason.trim())
            ));

            return new LoanActionResult(true, "Loan Application #" + loanId + " has been rejected.");
        }
        return new LoanActionResult(false, "Database error occurred while rejecting loan.");
    }
}
