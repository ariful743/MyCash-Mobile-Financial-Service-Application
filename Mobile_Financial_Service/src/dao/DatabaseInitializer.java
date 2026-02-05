package dao;

import java.sql.*;

public class DatabaseInitializer {

    public static void initialize() {
        createUsersTable();
        createTransactionsTable();
        createRequestsTable();
    }

    private static void createUsersTable() {
        String query = "CREATE TABLE IF NOT EXISTS users (" +
                "phone_number VARCHAR(20) PRIMARY KEY, " +
                "name VARCHAR(100), " +
                "pin VARCHAR(20), " +
                "balance DOUBLE, " +
                "type VARCHAR(20))";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(query);

            // Ensure type column is wide enough (for existing tables)
            stmt.execute("ALTER TABLE users MODIFY COLUMN type VARCHAR(20)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTransactionsTable() {
        String query = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender VARCHAR(20), " +
                "receiver VARCHAR(20), " +
                "type VARCHAR(20), " +
                "amount DOUBLE, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createRequestsTable() {
        String query = "CREATE TABLE IF NOT EXISTS transaction_requests (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_phone VARCHAR(20), " +
                "agent_phone VARCHAR(20), " +
                "amount DOUBLE, " +
                "type VARCHAR(20), " +
                "status VARCHAR(20), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "processed_at TIMESTAMP NULL)";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
