# Decisions & Assumptions Log

## Stage 1 Decisions

1. **User Role Inclusion in Schema**:
   - **Decision**: Added `user_role VARCHAR(20) DEFAULT 'USER'` to the initial `users` schema.
   - **Rationale**: Stage 2 introduces Admin accounts and differentiation between 'USER' and 'ADMIN'. By adding this column with a default value of 'USER' in Stage 1, Stage 1 functionality remains completely unaffected while preventing breaking schema migrations when Stage 2 is integrated.

2. **Scanner Handling Utility**:
   - **Decision**: Built a robust `InputReader` utility wrapping `Scanner.nextLine().trim()`.
   - **Rationale**: Core Java console applications frequently suffer from buffer skipping when switching between `nextInt()` and `nextLine()`. Using `nextLine()` consistently and parsing integers with validation loops eliminates input skipping and improves user experience.

3. **In-Memory Testing Support**:
   - **Decision**: Integrated H2 database in test scope alongside MySQL Connector/J in production scope.
   - **Rationale**: Enables `mvn test` to run standalone in any CI/CD environment or evaluator machine without requiring a live MySQL daemon running, while running the identical SQL schema and DAO methods in MySQL compatibility mode.

4. **Lockout Policy Mechanics**:
   - **Decision**:
     * 1st failure: 2 attempts remaining.
     * 2nd failure: 1 attempt remaining.
     * 3rd failure: Account immediately marked `status = 'Inactive'`, `failed_attempts = 3`, and user is notified that the account is locked and to contact support.
     * Successful login: Resets `failed_attempts = 0`.
   - **Rationale**: Strictly fulfills the Stage 1 specification while ensuring user feedback is clear and immediate.

5. **Security & Credentials**:
   - **Decision**: Exclude passwords from version control and archives. Provide `db.properties.example` and prioritize environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`).
   - **Rationale**: Adheres to modern security practices and project requirements.
