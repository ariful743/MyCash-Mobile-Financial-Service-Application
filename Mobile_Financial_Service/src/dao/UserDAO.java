package dao;

import models.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends BaseDAO {

    public User getUserByPhone(String phone) {
        String query = "SELECT * FROM users WHERE phone_number = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String pin = rs.getString("pin");
                    double balance = rs.getDouble("balance");
                    String type = rs.getString("type");

                    if (UserRole.AGENT.name().equalsIgnoreCase(type)) {
                        return new Agent(name, phone, pin, balance);
                    } else if (UserRole.BANK.name().equalsIgnoreCase(type)) {
                        return new Bank(name, phone, pin, balance);
                    } else {
                        return new Customer(name, phone, pin, balance);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerUser(String phone, String name, String pin, String type, double initialBalance) {
        String query = "INSERT INTO users (phone_number, name, pin, balance, type) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, phone);
            stmt.setString(2, name);
            stmt.setString(3, pin);
            stmt.setDouble(4, initialBalance);
            stmt.setString(5, type);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("❌ Error: Phone number already exists.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public List<String[]> getAllUsersByType(UserRole role) {
        List<String[]> users = new ArrayList<>();
        String query = "SELECT name, phone_number, balance FROM users WHERE type = ? ORDER BY name ASC";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(new String[] {
                            rs.getString("name"),
                            rs.getString("phone_number"),
                            "৳ " + String.format("%.2f", rs.getDouble("balance"))
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateBalance(String phoneNumber, double newBalance) {
        String query = "UPDATE users SET balance = ? WHERE phone_number = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, phoneNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBalance(Connection conn, String phoneNumber, double newBalance) throws SQLException {
        String query = "UPDATE users SET balance = ? WHERE phone_number = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, phoneNumber);
            return stmt.executeUpdate() > 0;
        }
    }
}