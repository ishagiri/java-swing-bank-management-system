import java.sql.Timestamp;

public record TransactionRecord(int id, String accountNo, String type, double amount, Timestamp transactionDate) {}
