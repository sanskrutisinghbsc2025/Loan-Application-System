#!/usr/bin/env bash
# ==============================================================================
# Loan Application System - Linux Launcher Script (KDE Plasma / Bash)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "================================================================="
echo "   Starting Loan Application System - Stage 1 (KDE Linux)        "
echo "================================================================="

# Detect Java
if ! command -v java &> /dev/null; then
    if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
        export PATH="$JAVA_HOME/bin:$PATH"
    else
        echo "Error: 'java' command not found. Please install Java 17+ or set JAVA_HOME."
        echo "Run: sudo dnf install java-21-openjdk / sudo apt install openjdk-21-jdk"
        exit 1
    fi
fi

# Detect Maven
if ! command -v mvn &> /dev/null; then
    if [ -n "$MAVEN_HOME" ] && [ -x "$MAVEN_HOME/bin/mvn" ]; then
        export PATH="$MAVEN_HOME/bin:$PATH"
    else
        echo "Error: 'mvn' command not found. Please install Apache Maven."
        echo "Run: sudo dnf install maven / sudo apt install maven"
        exit 1
    fi
fi

echo "Java Version:"
java -version
echo "-----------------------------------------------------------------"

# Run with Maven
echo "Compiling and running Loan Application System..."
mvn clean compile exec:java -Dexec.mainClass="com.sit.loanapp.Main"
