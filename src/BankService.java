import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BankService {

    public boolean isUsernameTaken(String username) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean isAccountNoTaken(String accountNo) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE account_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void registerUser(String name, String username, String plainPassword, String accountNo) throws SQLException {
        if (isUsernameTaken(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already registered!");
        }
        if (isAccountNoTaken(accountNo)) {
            throw new IllegalArgumentException("Account number '" + accountNo + "' is already registered!");
        }

        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        String sql = "INSERT INTO users (name, username, password, account_no, balance) VALUES (?, ?, ?, ?, 0.0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, username);
            stmt.setString(3, hashedPassword);
            stmt.setString(4, accountNo);
            stmt.executeUpdate();
        }
    }

    public UserSession login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT id, name, username, password, account_no FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                        return new UserSession(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("username"),
                                rs.getString("account_no")
                        );
                    }
                }
            }
        }
        return null;
    }

    public double getBalance(String accountNo) throws SQLException {
        String sql = "SELECT balance FROM users WHERE account_no = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    throw new SQLException("Account not found: " + accountNo);
                }
            }
        }
    }

    public double deposit(String accountNo, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        String updateSql = "UPDATE users SET balance = balance + ? WHERE account_no = ?";
        String insertTxSql = "INSERT INTO transactions (account_no, type, amount, transaction_date) VALUES (?, 'DEPOSIT', ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Failed to connect to the database.");
            }
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountNo);
                    int rows = updateStmt.executeUpdate();
                    if (rows == 0) {
                        throw new SQLException("Account not found: " + accountNo);
                    }
                }

                try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql)) {
                    txStmt.setString(1, accountNo);
                    txStmt.setDouble(2, amount);
                    txStmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return getBalance(accountNo);
    }

    public double withdraw(String accountNo, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero.");
        }

        String checkBalanceSql = "SELECT balance FROM users WHERE account_no = ? FOR UPDATE";
        String updateSql = "UPDATE users SET balance = balance - ? WHERE account_no = ? AND balance >= ?";
        String insertTxSql = "INSERT INTO transactions (account_no, type, amount, transaction_date) VALUES (?, 'WITHDRAWAL', ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Failed to connect to the database.");
            }
            conn.setAutoCommit(false);
            try {
                // Ensure sufficient balance
                try (PreparedStatement checkStmt = conn.prepareStatement(checkBalanceSql)) {
                    checkStmt.setString(1, accountNo);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Account not found: " + accountNo);
                        }
                        double currentBalance = rs.getDouble("balance");
                        if (currentBalance < amount) {
                            throw new IllegalStateException("Insufficient balance! Current balance: ₹" + String.format("%.2f", currentBalance));
                        }
                    }
                }

                // Deduct balance atomically
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, amount);
                    updateStmt.setString(2, accountNo);
                    updateStmt.setDouble(3, amount);
                    int rows = updateStmt.executeUpdate();
                    if (rows == 0) {
                        throw new IllegalStateException("Insufficient balance or account update failed.");
                    }
                }

                // Record transaction
                try (PreparedStatement txStmt = conn.prepareStatement(insertTxSql)) {
                    txStmt.setString(1, accountNo);
                    txStmt.setDouble(2, amount);
                    txStmt.executeUpdate();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return getBalance(accountNo);
    }

    public List<TransactionRecord> getTransactionHistory(String accountNo) throws SQLException {
        String sql = "SELECT id, account_no, type, amount, transaction_date FROM transactions WHERE account_no = ? ORDER BY transaction_date DESC, id DESC LIMIT 50";
        List<TransactionRecord> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accountNo);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(new TransactionRecord(
                            rs.getInt("id"),
                            rs.getString("account_no"),
                            rs.getString("type"),
                            rs.getDouble("amount"),
                            rs.getTimestamp("transaction_date")
                    ));
                }
            }
        }
        return history;
    }
}
