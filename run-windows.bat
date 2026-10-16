@echo off
REM ==============================================================================
REM Loan Application System - Windows Launcher Script
REM ==============================================================================

title Loan Application System - Stage 1
cd /d "%~dp0"

echo =================================================================
echo    Starting Loan Application System - Stage 1 (Windows)
echo =================================================================

REM Check Java
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME (
        set "PATH=%JAVA_HOME%\bin;%PATH%"
    ) else (
        echo [ERROR] Java is not installed or not in PATH.
        echo Please install JDK 17+ from https://adoptium.net/ and set JAVA_HOME.
        pause
        exit /b 1
    )
)

REM Check Maven
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH.
    echo Please install Apache Maven and add its bin folder to PATH.
    pause
    exit /b 1
)

echo [INFO] Environment verified. Compiling and launching application...
call mvn clean compile exec:java -Dexec.mainClass="com.sit.loanapp.Main"

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Application exited with an error.
)

pause
