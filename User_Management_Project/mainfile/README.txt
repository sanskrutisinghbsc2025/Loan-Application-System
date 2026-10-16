================================================================================
  LOAN APPLICATION SYSTEM - COMPLETE ENTERPRISE SUITE (STAGES 1 TO 5)
================================================================================

PROJECT OVERVIEW:
This project is an enterprise-grade Core Java console application built using
JDBC, MySQL, and Maven. It implements all 5 project stages:
- Stage 1: User Registration, Authentication, Account Lockout, and Password Reset.
- Stage 2: Admin Management System with RBAC, Search, User Editing, Status Toggles
  (Activation/Deactivation), User Deletion, User Notifications, and Audit Logging.
- Stage 3: Loan Application Management System with loan submission, auto-interest
  rate assignment, applicant status tracking, admin review, approvals, rejections,
  and decision notifications.
- Stage 4: PAN Validation, CIBIL Scoring Engine, Interest Rate Range Calculator,
  Auto-Generated Credentials (Username & Password generation), and Credential Emailing.
- Stage 5: Loan Approval with EMI Calculation, Total Interest & Total Payable computation,
  JDBC database persistence, and comprehensive 7-point email notifications.

The architecture is strictly organized into decoupled stages, ensuring complete
preservation of stage requirements, isolated implementation notes, and reusable
shared infrastructure.

================================================================================
DIRECTORY STRUCTURE:
================================================================================

mainfile/
├── README.txt                      <- Root project documentation and execution guide
├── pom.xml                         <- Maven build & dependency configuration
├── run-linux.sh                    <- Executable launcher for KDE Plasma / Linux
├── run-windows.bat                 <- Double-clickable launcher for Windows
├── prompts/
│   └── ANTIGRAVITY_PROMPT.txt      <- Exact preservation of the project prompt
├── rules/
│   └── PROJECT_RULES.txt           <- Overarching project standards and lifecycle
├── stages/
│   ├── stage1/                     <- Stage 1 User Management Module
│   │   ├── source-files/           <- Unmodified Stage 1 PDF document
│   │   ├── requirements/           <- Extracted Stage 1 requirements
│   │   ├── code/                   <- Stage 1 overview & snapshot
│   │   ├── rules/                  <- Stage 1 acceptance checklist
│   │   └── notes/
│   │
│   ├── stage2/                     <- Stage 2 Admin Management Module
│   │   ├── source-files/           <- Unmodified Stage 2 PDF document
│   │   ├── requirements/           <- Extracted Stage 2 requirements
│   │   ├── code/                   <- Stage 2 overview & snapshot
│   │   ├── rules/                  <- Stage 2 acceptance checklist
│   │   └── notes/
│   │
│   ├── stage3/                     <- Stage 3 Loan Application Module
│   │   ├── source-files/           <- Unmodified Stage 3 PDF document
│   │   ├── requirements/           <- Extracted Stage 3 requirements
│   │   ├── code/                   <- Stage 3 overview & snapshot
│   │   ├── rules/                  <- Stage 3 acceptance checklist
│   │   └── notes/
│   │
│   ├── stage4/                     <- Stage 4 PAN & CIBIL Engine Module
│   │   ├── source-files/           <- Unmodified Stage 4 PDF document
│   │   ├── requirements/           <- Extracted Stage 4 requirements
│   │   ├── code/                   <- Stage 4 overview & snapshot
│   │   ├── rules/                  <- Stage 4 acceptance checklist
│   │   └── notes/
│   │
│   └── stage5/                     <- Stage 5 Loan Approval & EMI Module
│       ├── source-files/           <- Unmodified Stage 5 PDF document
│       │   └── Stage 5.pdf
│       ├── requirements/           <- Extracted Stage 5 requirements
│       │   └── Stage-5-requirements.txt
│       ├── code/                   <- Stage 5 overview & snapshot
│       │   ├── Stage5Overview.txt
│       │   └── snapshot/
│       ├── rules/                  <- Stage 5 rules and specifications
│       │   └── STAGE5_RULES.txt
│       └── notes/                  <- Stage 5 architectural notes
│           └── STAGE5_ARCHITECTURE_NOTES.txt
│
├── shared/
│   ├── src/                        <- Shared Core Java application source code
│   │   └── main/
│   │       ├── java/com/sit/loanapp/
│   │       │   ├── config/         <- DatabaseConfig & connection provider
│   │       │   ├── dao/            <- UserDao, LoanDao, & JDBC implementations
│   │       │   ├── model/          <- User, AuditLog, LoanApplication entities
│   │       │   ├── service/        <- UserService, AdminService, LoanService, CibilService, NotificationService
│   │       │   ├── ui/             <- ConsoleMenu CLI & safe InputReader
│   │       │   ├── util/           <- ValidationUtil (regex & policies)
│   │       │   └── Main.java       <- Application entry point
│   │       └── resources/
│   │           ├── db.properties   <- Local database credentials
│   │           └── db.properties.example
│   ├── tests/                      <- Automated unit & integration tests
│   │   └── java/com/sit/loanapp/
│   │       ├── ValidationUtilTest.java
│   │       ├── UserDaoTest.java
│   │       ├── UserServiceTest.java
│   │       ├── AdminServiceTest.java
│   │       ├── LoanServiceTest.java
│   │       └── CibilServiceTest.java
│   ├── sql/                        <- Database scripts
│   │   ├── schema.sql              <- MySQL database & table definitions
│   │   ├── seed_stage1.sql         <- Stage 1 sample users
│   │   ├── seed_stage2.sql         <- Stage 2 admin & users
│   │   ├── seed_stage3.sql         <- Stage 3 sample loan applications
│   │   └── seed_stage4.sql         <- Stage 4 sample PAN & CIBIL data
│   └── docs/                       <- In-depth technical guides
│       ├── ARCHITECTURE.md
│       ├── DATABASE_SETUP.md
│       ├── DECISIONS_AND_ASSUMPTIONS.md
│       ├── LINUX_SETUP.md
│       └── WINDOWS_SETUP.md

================================================================================
STAGE 4 FEATURES (PAN & CIBIL ENGINE):
================================================================================

1. Interest Rate Calculator with PAN Validation:
   - Option 5 in Main Menu.
   - Accepts PAN card number (strictly validated: 5 letters + 4 digits + 1 letter).
   - Accepts Loan Type (Home Loan, Personal Loan, Education Loan, Vehicle Loan).
   - Generates CIBIL score (600–900).
   - Calculates personalized interest rate within loan type range:
     * Home Loan: 6.5% – 8.5%
     * Personal Loan: 9.0% – 12.0%
     * Education Loan: 4.0% – 6.0%
     * Vehicle Loan: 7.0% – 9.0%
   - Output string:
     "For PAN ABCDE1234F, with a CIBIL score of 720, the interest rate for Home Loan is 7.5%."

2. CIBIL Score Check & Auto-Generated Credentials:
   - Option 4 in Main Menu.
   - Validates personal details (letters-only names, 10-digit mobile, email, location, PAN).
   - Auto-generates Username: First 3 letters of fname + First 3 letters of lname + Last 4 digits of mobile.
     Example: Shivani + Chaudhari + 9876543210 -> ShiCha3210.
   - Auto-generates Password: 8–10 chars with letters, numbers, and symbols (e.g. @Shiv!123).
   - Dispatches Credentials Email (Subject: "Your Username and Password for Loan Application is").
   - Displays CIBIL score (600–900) to console and persists to database.
   - Prompts user to immediately Apply for a Loan using their newly generated account!

================================================================================
STAGE 5 FEATURES (LOAN APPROVAL WITH EMI & 7-POINT EMAIL NOTIFICATION):
================================================================================

1. Automated EMI Calculation:
   - Formula: EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
     Where:
     P = Principal Loan Amount
     r = Monthly interest rate = (interestRate / 12) / 100
     n = Loan duration in months
   - Total Payable Amount = EMI * n
   - Total Interest Amount = Total Payable Amount - P

2. Admin Review & One-Click Approval:
   - Console Admin Menu -> "Review Loan Applications" -> Select Loan -> Approve.
   - Automatically computes EMI, Total Interest, and Total Payable.
   - Atomically updates loan status to 'Approved' and stores calculated EMI metrics in MySQL.
   - Records an administrative audit log entry in audit_logs table.

3. 7-Parameter Approval Email Notification:
   - Real-time notification dispatched to applicant containing:
     1. Sanctioned Loan Amount
     2. Loan Type
     3. Loan Tenure (months)
     4. Applied Interest Rate (%)
     5. Monthly EMI Amount
     6. Total Interest Amount
     7. Total Payable Amount

================================================================================
QUICK START GUIDE:
================================================================================

1. PREREQUISITES:
   - Java Development Kit (JDK 17 or 21)
   - Apache Maven 3.8+
   - MySQL Server 8.0+ or MariaDB 10.5+

2. DATABASE SETUP:
   Initialize schema and seed accounts:
     mysql -u root -p < shared/sql/schema.sql
     mysql -u root -p < shared/sql/seed_stage5.sql

3. LAUNCHING THE APPLICATION:
   - On KDE Linux:
       ./run-linux.sh
   - On Windows:
       run-windows.bat
   - Via Maven:
       mvn clean compile exec:java

4. RUNNING AUTOMATED TESTS:
   Run the comprehensive test suite (runs against in-memory MySQL mode):
       mvn test
================================================================================
