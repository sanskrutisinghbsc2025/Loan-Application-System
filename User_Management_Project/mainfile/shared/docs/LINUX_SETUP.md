# Linux Setup Guide (KDE Plasma / Ubuntu / Fedora / Arch)

## 1. Prerequisites
Install Java 17 or 21, Maven, and MySQL/MariaDB using your distribution package manager:

### Fedora / RHEL:
```bash
sudo dnf install java-21-openjdk java-21-openjdk-devel maven mariadb-server mariadb
sudo systemctl enable --now mariadb
```

### Ubuntu / Debian:
```bash
sudo apt update
sudo apt install openjdk-21-jdk maven mysql-server mysql-client
sudo systemctl enable --now mysql
```

### Arch Linux:
```bash
sudo pacman -S jdk21-openjdk maven mariadb
sudo mariadb-install-db --user=mysql --basedir=/usr --datadir=/var/lib/mysql
sudo systemctl enable --now mariadb
```

## 2. Initialize Database
```bash
mysql -u root -p < shared/sql/schema.sql
mysql -u root -p < shared/sql/seed_stage1.sql
```

## 3. Run the Application
Use the provided launch script:
```bash
chmod +x run-linux.sh
./run-linux.sh
```

Or run via Maven directly:
```bash
mvn clean compile exec:java
```

Or build an executable JAR:
```bash
mvn clean package
java -jar target/loan-app-stage1-1.0.0.jar
```
