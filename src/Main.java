import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main {

    private static final BankService bankService = new BankService();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Check database connectivity on startup
            if (!DatabaseConnection.testConnection()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Warning: Could not connect to MySQL database at jdbc:mysql://localhost:3306/bankdb.\n"
                        + "Please ensure MySQL Server is running and credentials in db.properties are correct.",
                        "Database Connection Warning",
                        JOptionPane.WARNING_MESSAGE
                );
            }

            createAndShowLoginGui();
        });
    }

    private static void createAndShowLoginGui() {
        JFrame frame = new JFrame("Bank Management System");
        frame.setSize(600, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 600, 450);
        panel.setBackground(new Color(240, 245, 250));
        panel.setLayout(null);

        // Login Title
        JLabel loginTitle = new JLabel("BANK MANAGEMENT SYSTEM", SwingConstants.CENTER);
        loginTitle.setBounds(100, 40, 400, 40);
        loginTitle.setFont(new Font("Arial", Font.BOLD, 22));

        // Username
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(130, 120, 100, 30);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField userField = new JTextField();
        userField.setBounds(230, 120, 220, 30);

        // Password
        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(130, 170, 100, 30);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPasswordField passField = new JPasswordField();
        passField.setBounds(230, 170, 220, 30);

        // Login button
        JButton loginButton = new JButton("LOGIN");
        loginButton.setBounds(230, 230, 100, 35);
        loginButton.setFont(new Font("Arial", Font.BOLD, 13));

        // Register button
        JButton registerButton = new JButton("REGISTER");
        registerButton.setBounds(340, 230, 110, 35);
        registerButton.setFont(new Font("Arial", Font.BOLD, 13));

        // Register button action
        registerButton.addActionListener(e -> showRegisterDialog(frame));

        // Login button action
        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Please enter username and password!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                UserSession session = bankService.login(username, password);
                if (session != null) {
                    passField.setText("");
                    frame.setVisible(false);
                    showDashboard(frame, session);
                } else {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Invalid username or password!",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Database Error during login: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Add components
        panel.add(loginTitle);
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(loginButton);
        panel.add(registerButton);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void showRegisterDialog(JFrame parentFrame) {
        JFrame registerFrame = new JFrame("Create Account");
        registerFrame.setSize(500, 450);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setLocationRelativeTo(parentFrame);
        registerFrame.setResizable(false);

        JPanel regPanel = new JPanel();
        regPanel.setBounds(0, 0, 500, 450);
        regPanel.setBackground(new Color(240, 245, 250));
        regPanel.setLayout(null);

        // Register Title
        JLabel registerTitle = new JLabel("CREATE BANK ACCOUNT", SwingConstants.CENTER);
        registerTitle.setBounds(100, 30, 300, 30);
        registerTitle.setFont(new Font("Arial", Font.BOLD, 20));

        // Name
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(70, 90, 100, 25);
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField nameField = new JTextField();
        nameField.setBounds(180, 90, 220, 30);

        // Username
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(70, 140, 100, 25);
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField usernameField = new JTextField();
        usernameField.setBounds(180, 140, 220, 30);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(70, 190, 100, 25);
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPasswordField passwordField = new JPasswordField();
        passwordField.setBounds(180, 190, 220, 30);

        // Account Number
        JLabel accountLabel = new JLabel("Account No:");
        accountLabel.setBounds(70, 240, 100, 25);
        accountLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField accountField = new JTextField();
        accountField.setBounds(180, 240, 220, 30);

        // Create Account button
        JButton createButton = new JButton("CREATE ACCOUNT");
        createButton.setBounds(160, 310, 180, 35);
        createButton.setFont(new Font("Arial", Font.BOLD, 13));

        createButton.addActionListener(event -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String account = accountField.getText().trim();

            if (name.isEmpty() || username.isEmpty() || password.isEmpty() || account.isEmpty()) {
                JOptionPane.showMessageDialog(
                        registerFrame,
                        "Please fill all fields!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!account.matches("\\d{4,20}")) {
                JOptionPane.showMessageDialog(
                        registerFrame,
                        "Account number must contain between 4 and 20 digits!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (password.length() < 4) {
                JOptionPane.showMessageDialog(
                        registerFrame,
                        "Password must be at least 4 characters long!",
                        "Validation Error",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                bankService.registerUser(name, username, password, account);
                JOptionPane.showMessageDialog(
                        registerFrame,
                        "Account Created Successfully!\nYou can now log in with your credentials.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                registerFrame.dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        registerFrame,
                        ex.getMessage(),
                        "Registration Error",
                        JOptionPane.WARNING_MESSAGE
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        registerFrame,
                        "Database Error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Add components
        regPanel.add(registerTitle);
        regPanel.add(nameLabel);
        regPanel.add(nameField);
        regPanel.add(usernameLabel);
        regPanel.add(usernameField);
        regPanel.add(passwordLabel);
        regPanel.add(passwordField);
        regPanel.add(accountLabel);
        regPanel.add(accountField);
        regPanel.add(createButton);

        registerFrame.add(regPanel);
        registerFrame.setVisible(true);
    }

    private static void showDashboard(JFrame loginFrame, UserSession session) {
        JFrame dashboard = new JFrame("Bank Dashboard - " + session.name());
        dashboard.setSize(600, 560);
        dashboard.setLocationRelativeTo(null);
        dashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dashboard.setResizable(false);

        dashboard.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loginFrame.setVisible(true);
            }
        });

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 600, 560);
        panel.setBackground(new Color(240, 245, 250));
        panel.setLayout(null);

        // Welcome Header
        JLabel welcome = new JLabel("WELCOME TO BANK DASHBOARD", SwingConstants.CENTER);
        welcome.setBounds(50, 30, 500, 30);
        welcome.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel userDetails = new JLabel("User: " + session.name() + "  |  Account No: " + session.accountNo(), SwingConstants.CENTER);
        userDetails.setBounds(50, 65, 500, 25);
        userDetails.setFont(new Font("Arial", Font.PLAIN, 14));
        userDetails.setForeground(new Color(60, 60, 60));

        // Check Balance Button
        JButton balanceButton = new JButton("CHECK BALANCE");
        balanceButton.setBounds(180, 120, 220, 40);
        balanceButton.setFont(new Font("Arial", Font.BOLD, 13));

        balanceButton.addActionListener(event -> {
            try {
                double currentBalance = bankService.getBalance(session.accountNo());
                JOptionPane.showMessageDialog(
                        dashboard,
                        String.format("Account No: %s\nAccount Holder: %s\nCurrent Balance: ₹%.2f",
                                session.accountNo(), session.name(), currentBalance),
                        "Current Balance",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        dashboard,
                        "Failed to retrieve balance: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Deposit Button
        JButton depositButton = new JButton("DEPOSIT");
        depositButton.setBounds(180, 180, 220, 40);
        depositButton.setFont(new Font("Arial", Font.BOLD, 13));

        depositButton.addActionListener(event -> {
            String input = JOptionPane.showInputDialog(
                    dashboard,
                    "Enter amount to deposit (₹):",
                    "Deposit Money",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (input == null) {
                return; // User cancelled
            }

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(dashboard, "Please enter an amount!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dashboard, "Please enter a positive amount!", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (amount > 10000000) {
                    JOptionPane.showMessageDialog(dashboard, "Amount exceeds maximum deposit limit of ₹1,00,00,000!", "Limit Exceeded", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double newBalance = bankService.deposit(session.accountNo(), amount);
                JOptionPane.showMessageDialog(
                        dashboard,
                        String.format("₹%.2f deposited successfully!\nNew Balance: ₹%.2f", amount, newBalance),
                        "Deposit Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(dashboard, "Please enter valid numbers only!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dashboard, "Deposit failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Withdraw Button
        JButton withdrawButton = new JButton("WITHDRAW");
        withdrawButton.setBounds(180, 240, 220, 40);
        withdrawButton.setFont(new Font("Arial", Font.BOLD, 13));

        withdrawButton.addActionListener(event -> {
            String input = JOptionPane.showInputDialog(
                    dashboard,
                    "Enter amount to withdraw (₹):",
                    "Withdraw Money",
                    JOptionPane.PLAIN_MESSAGE
            );

            if (input == null) {
                return; // User cancelled
            }

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(dashboard, "Please enter an amount!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dashboard, "Please enter a positive amount!", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                double newBalance = bankService.withdraw(session.accountNo(), amount);
                JOptionPane.showMessageDialog(
                        dashboard,
                        String.format("₹%.2f withdrawn successfully!\nRemaining Balance: ₹%.2f", amount, newBalance),
                        "Withdrawal Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (NumberFormatException error) {
                JOptionPane.showMessageDialog(dashboard, "Please enter valid numbers only!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalStateException error) {
                JOptionPane.showMessageDialog(dashboard, error.getMessage(), "Insufficient Balance", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dashboard, "Withdrawal failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Transaction History Button
        JButton historyButton = new JButton("TRANSACTION HISTORY");
        historyButton.setBounds(180, 300, 220, 40);
        historyButton.setFont(new Font("Arial", Font.BOLD, 13));

        historyButton.addActionListener(event -> {
            try {
                List<TransactionRecord> transactions = bankService.getTransactionHistory(session.accountNo());
                if (transactions.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            dashboard,
                            "No transactions recorded for this account yet.",
                            "Transaction History",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format("%-22s %-14s %14s\n", "Date & Time", "Type", "Amount"));
                    sb.append("----------------------------------------------------\n");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    for (TransactionRecord tx : transactions) {
                        String dateStr = tx.transactionDate() != null ? sdf.format(tx.transactionDate()) : "N/A";
                        sb.append(String.format("%-22s %-14s ₹%13.2f\n", dateStr, tx.type(), tx.amount()));
                    }

                    JTextArea textArea = new JTextArea(sb.toString(), 16, 42);
                    textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    textArea.setEditable(false);
                    textArea.setCaretPosition(0);

                    JScrollPane scrollPane = new JScrollPane(textArea);
                    JOptionPane.showMessageDialog(
                            dashboard,
                            scrollPane,
                            "Transaction History - " + session.accountNo(),
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(
                        dashboard,
                        "Failed to retrieve transaction history: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Logout Button
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setBounds(180, 360, 220, 40);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));

        logoutButton.addActionListener(event -> {
            dashboard.dispose();
            loginFrame.setVisible(true);
        });

        // Add components to panel
        panel.add(welcome);
        panel.add(userDetails);
        panel.add(balanceButton);
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(historyButton);
        panel.add(logoutButton);

        dashboard.add(panel);
        dashboard.setVisible(true);
    }
}