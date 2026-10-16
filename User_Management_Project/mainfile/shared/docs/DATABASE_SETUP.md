# Database Setup Guide

This guide covers setting up MySQL or MariaDB for the Loan Application System.

## 1. Prerequisites
- MySQL Server 8.0+ OR MariaDB 10.5+ installed and running.
- Root or privileged database user.

## 2. Initialize Database and Schema

### Linux (KDE Plasma / Terminal):
Open a terminal and run:
```bash
mysql -u root -p < shared/sql/schema.sql
mysql -u root -p < shared/sql/seed_stage1.sql
```

### Windows (Command Prompt / PowerShell):
Open Command Prompt and run:
```cmd
mysql -u root -p < shared\sql\schema.sql
mysql -u root -p < shared\sql\seed_stage1.sql
```

Or execute both scripts inside MySQL Workbench / phpMyAdmin / DBeaver.

## 3. Configure Database Credentials

The application reads configuration from `shared/src/main/resources/db.properties` or from environment variables:

### Option A: Via `db.properties` file:
Edit `shared/src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/loan_app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YourPasswordHere
```

### Option B: Via Environment Variables (Recommended for CI / production):
```bash
export DB_URL="jdbc:mysql://localhost:3306/loan_app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USER="root"
export DB_PASSWORD="YourPasswordHere"
```

## 4. Test User Accounts Included in Seed Data

| Username | Email | Password | Status | Failed Attempts | Note |
|---|---|---|---|---|---|
| `amrutap` | `amruta.patil@example.com` | `Password@123` | Active | 0 | Normal active user |
| `johndoe` | `john.doe@example.com` | `Secure@2024` | Active | 2 | 1 attempt remaining before lockout |
| `sarahs` | `sarah.smith@example.com` | `Locked@123` | Inactive | 3 | Locked user (contact support) |
