# Windows Setup Guide (Windows 10 / 11)

## 1. Prerequisites
1. **JDK 17 or 21**:
   - Download Eclipse Temurin or Oracle JDK from https://adoptium.net/
   - Ensure `JAVA_HOME` is set in System Environment Variables and `%JAVA_HOME%\bin` is added to `Path`.
2. **Apache Maven**:
   - Download Maven from https://maven.apache.org/download.cgi
   - Add `bin` directory to `Path`.
3. **MySQL Server 8.0+**:
   - Install MySQL Community Server and MySQL Workbench.

## 2. Initialize Database
Open Command Prompt in the project root:
```cmd
mysql -u root -p < shared\sql\schema.sql
mysql -u root -p < shared\sql\seed_stage1.sql
```
*(Alternatively, copy and run the SQL contents in MySQL Workbench)*

## 3. Configure Database Credentials
Edit `shared\src\main\resources\db.properties` with your MySQL root password:
```properties
db.url=jdbc:mysql://localhost:3306/loan_app_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=YOUR_PASSWORD
```

## 4. Run the Application
Double-click `run-windows.bat` or run in Command Prompt:
```cmd
run-windows.bat
```

Or run directly via Maven:
```cmd
mvn clean compile exec:java
```
