package dao;

import models.TransactionType;
import models.UserRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO extends BaseDAO {

    public void logTransaction(String sender, String receiver, TransactionType type, double amount) {
        String query = "INSERT INTO transactions (sender, receiver, type, amount) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, type.name());
            stmt.setDouble(4, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void logTransaction(Connection conn, String sender, String receiver, TransactionType type, double amount)
            throws SQLException {
        String query = "INSERT INTO transactions (sender, receiver, type, amount) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sender);
            stmt.setString(2, receiver);
            stmt.setString(3, type.name());
            stmt.setDouble(4, amount);
            stmt.executeUpdate();
        }
    }

    public List<String[]> getTransactionHistory(String phone) {
        List<String[]> history = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE sender = ? OR receiver = ? ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phone);
            stmt.setString(2, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String typeStr = rs.getString("type");
                    String sender = rs.getString("sender");
                    String receiver = rs.getString("receiver");
                    double amount = rs.getDouble("amount");
                    String time = rs.getString("timestamp");

                    String detail = formatDetail(typeStr, sender, receiver, phone);
                    history.add(new String[] { time, typeStr, detail, "৳ " + amount });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public List<String[]> getAllTransactions() {
        List<String[]> history = new ArrayList<>();
        String query = "SELECT * FROM transactions ORDER BY timestamp DESC";

        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String typeStr = rs.getString("type");
                String sender = rs.getString("sender");
                String receiver = rs.getString("receiver");
                double amount = rs.getDouble("amount");
                String time = rs.getString("timestamp");

                String senderRole = getUserRole(sender);
                String receiverRole = getUserRole(receiver);

                String detail = String.format("%s (%s) -> %s (%s)", sender, senderRole, receiver, receiverRole);
                history.add(new String[] { time, typeStr, detail, "৳ " + amount });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    private String formatDetail(String typeStr, String sender, String receiver, String phone) {
        TransactionType type = TransactionType.valueOf(typeStr);
        switch (type) {
            case SEND_MONEY:
                return sender.equals(phone) ? "Sent to " + receiver : "Received from " + sender;
            case CASH_OUT:
            case APPROVED_CASH_OUT:
                return sender.equals(phone) ? "Cash Out (Approved)" : "Approved Cash Out for " + receiver;
            case CASH_IN:
            case APPROVED_CASH_IN:
                return sender.equals(phone) ? "Cash In to " + receiver : "Cash In from " + sender;
            case CASH_IN_REQ:
                return sender.equals(phone) ? "Cash In Request to " + receiver : "Cash In Request from " + sender;
            case CASH_OUT_REQ:
                return sender.equals(phone) ? "Cash Out Request to " + receiver : "Cash Out Request from " + sender;
            case REJECTED_REQ:
                return sender.equals(phone) ? "Rejected Request for " + receiver : "Request Rejected by " + sender;
            case ADD_MONEY:
                return "Added Money to Wallet";
            default:
                return "";
        }
    }

    private String getUserRole(String phone) {
        if (phone == null || phone.isEmpty())
            return "unknown";
        if (phone.equals("SYSTEM"))
            return "system";
        if (phone.equalsIgnoreCase("BANK"))
            return "authority";

        String query = "SELECT type FROM users WHERE phone_number = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("type");
                    if (UserRole.CUSTOMER.name().equalsIgnoreCase(type))
                        return "user";
                    if (UserRole.BANK.name().equalsIgnoreCase(type))
                        return "authority";
                    return type.toLowerCase();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "unknown";
    }
}
