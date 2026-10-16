import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static String url = "jdbc:mysql://localhost:3306/bankdb";
    private static String user = "root";
    private static String password = "";

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        
        // 1. Try reading from db.properties file in working directory or classpath
        File propFile = new File("db.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                props.load(fis);
            } catch (Exception e) {
                System.err.println("Could not read db.properties file: " + e.getMessage());
            }
        } else {
            try (InputStream is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                }
            } catch (Exception e) {
                System.err.println("Could not read db.properties from classpath: " + e.getMessage());
            }
        }

        if (props.containsKey("db.url")) {
            url = props.getProperty("db.url").trim();
        }
        if (props.containsKey("db.user")) {
            user = props.getProperty("db.user").trim();
        }
        if (props.containsKey("db.password")) {
            password = props.getProperty("db.password").trim();
        }

        // 2. Override with environment variables if present
        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPass = System.getenv("DB_PASSWORD");

        if (envUrl != null && !envUrl.isBlank()) {
            url = envUrl.trim();
        }
        if (envUser != null && !envUser.isBlank()) {
            user = envUser.trim();
        }
        if (envPass != null) {
            password = envPass.trim();
        }
    }

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver NOT Found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database Connection Failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }
}