# Bank Management System (Java Swing + MySQL)

A desktop banking application developed in Java Swing with persistent storage, database transactions, input validation, and password security powered by MySQL.

---

## Features

- **User Registration & Account Creation:** Create a new bank account with full input validation, unique username check, unique account number verification, and SHA-256 password hashing.
- **Secure Authentication:** Log in with your registered username and hashed password.
- **Balance Inquiry:** Real-time balance retrieval directly from MySQL.
- **Deposit & Withdrawal:**
  - Strict input validation (positive amounts, reasonable limits).
  - Insufficient funds checking to prevent overdrafts.
  - ACID-compliant database transactions (`commit`/`rollback`) ensuring atomic balance updates and transaction logging.
- **Transaction History:** View chronological records of all deposits and withdrawals with timestamps.
- **Safe Database Design:** Prepared statements used throughout to prevent SQL injection.
- **Secure Configuration:** Database credentials managed via `db.properties` or environment variables, excluded from version control.

---

## Technologies Used

- **Language:** Java (JDK 21 / 25)
- **GUI Framework:** Java Swing & AWT
- **Database:** MySQL Server 8.0+
- **Connectivity:** JDBC (MySQL Connector/J 26.7.0 / 8.x)
- **Security:** SHA-256 password hashing via Java Cryptography Architecture
- **Architecture:** Layered design (Presentation Layer, Business Service Layer, JDBC Persistence Layer)

---

## Project Structure

```
BankManagementSystem/
├── bin/                          # Compiled bytecode (.class files)
├── lib/                          # External libraries
│   └── mysql-connector-j-26.7.0.jar
├── src/                          # Java source files
│   ├── App.java                  # Application entrypoint
│   ├── BankService.java          # Transactional business logic & DAO
│   ├── DatabaseConnection.java   # Configurable JDBC connection manager
│   ├── Main.java                 # Swing UI (Login & Dashboard)
│   ├── PasswordUtil.java         # SHA-256 password hashing & verification
│   ├── TransactionRecord.java    # Transaction data record
│   └── UserSession.java          # Logged-in session record
├── .vscode/                      # VS Code project settings
│   └── settings.json
├── db.properties                 # Local database credentials (git-ignored)
├── db.properties.example         # Template database configuration
├── schema.sql                    # Safe database & table initialization script
└── README.md
```

---

## Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher (OpenJDK 25 LTS recommended).
- **MySQL Server:** Version 8.0 or higher running on `localhost:3306`.

---

## Database Setup

1. Make sure your MySQL Server service is running.
2. Initialize the database schema safely using `schema.sql`:
   ```powershell
   # Using MySQL CLI
   mysql -u root -p < schema.sql
   ```
   *(Note: `schema.sql` uses `CREATE TABLE IF NOT EXISTS`, so existing data will not be overwritten.)*

3. Configure your database credentials in `db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/bankdb
   db.user=root
   db.password=your_mysql_password
   ```
   *Alternative:* You can set environment variables `DB_URL`, `DB_USER`, and `DB_PASSWORD`.

---

## Build and Run

### 1. Compile the Project (Clean Build)

Open PowerShell or Command Prompt in the project root directory:

```powershell
# PowerShell:
javac -cp "lib/mysql-connector-j-26.7.0.jar" -d bin (Get-Item src/*.java)

# Command Prompt (cmd):
javac -cp "lib/mysql-connector-j-26.7.0.jar" -d bin src/*.java
```

### 2. Run the Application

```powershell
# Launch the application
java -cp "bin;lib/mysql-connector-j-26.7.0.jar" Main
```

*(You can also run `java -cp "bin;lib/mysql-connector-j-26.7.0.jar" App`)*

---

## Troubleshooting

- **`ClassNotFoundException: com.mysql.cj.jdbc.Driver`**:
  Ensure the classpath flag `-cp "bin;lib/mysql-connector-j-26.7.0.jar"` is specified with exact spelling and semicolons (`;`) on Windows.
- **`Database Connection Failed`**:
  Verify MySQL service is running (`Get-Service MySQL*` in PowerShell) and credentials in `db.properties` match your MySQL setup.
