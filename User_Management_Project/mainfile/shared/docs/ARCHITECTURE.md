# System Architecture

## Layered Architecture Pattern

The system follows a clean 4-tier layered architecture designed for separation of concerns and forward-extensibility across all project stages:

```
+-------------------------------------------------------------+
|              Presentation Layer (CLI / UI)                  |
|  - ConsoleMenu: Interactive terminal interface (Scanner)    |
|  - InputReader: Safe input parsing & validation loop        |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                     Service Layer                           |
|  - UserService / UserServiceImpl:                           |
|      * Validates registration business rules                |
|      * Authentication & attempt counting                    |
|      * Account lockout enforcement (3 failures -> Inactive) |
|      * Password reset validation & orchestration            |
|  - ValidationUtil: Centralized regex & rule validators      |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|              Data Access Layer (DAO / JDBC)                 |
|  - UserDao / UserDaoImpl:                                   |
|      * PreparedStatements (100% parameter binding)          |
|      * Connection lifecycle via try-with-resources          |
|      * Queries & mutations on `users` table                 |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                    Persistence Layer                        |
|  - DatabaseConfig: Externalized credentials & connection pool|
|  - MySQL / MariaDB (Production)                             |
|  - H2 in-memory mode (Automated Test Suite)                 |
+-------------------------------------------------------------+
```

## Stage Isolation Model
- `stages/stage1/`: Holds Stage 1 original requirements document, extracted plain-text specs, stage-specific rules, acceptance checklists, and implementation notes.
- `shared/`: Holds reusable domain entities, DAO interfaces, service implementations, SQL scripts, test suites, and documentation.
- When subsequent stages (Stage 2: Admin System, Stage 3: Loan Application, Stage 4: CIBIL & PAN, Stage 5: EMI & Approval) are added, shared entities and services are extended additively without breaking earlier stages.
