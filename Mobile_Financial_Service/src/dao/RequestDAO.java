package dao;

import models.RequestStatus;
import models.TransactionRequest;
import models.TransactionType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO extends BaseDAO {

    public boolean createTransactionRequest(String customerPhone, String agentPhone, double amount,
            TransactionType type) {
        String query = "INSERT INTO transaction_requests (customer_phone, agent_phone, amount, type, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, customerPhone);
            stmt.setString(2, agentPhone);
            stmt.setDouble(3, amount);
            stmt.setString(4, type.name());
            stmt.setString(5, RequestStatus.PENDING.name());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TransactionRequest> getPendingRequests(String receiverPhone) {
        List<TransactionRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM transaction_requests WHERE status = ? AND agent_phone = ? ORDER BY created_at DESC";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, RequestStatus.PENDING.name());
            stmt.setString(2, receiverPhone);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public TransactionRequest getTransactionRequest(int requestId) {
        String query = "SELECT * FROM transaction_requests WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRequest(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateRequestStatus(int requestId, RequestStatus status) {
        String query = "UPDATE transaction_requests SET status = ?, processed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRequestStatus(Connection conn, int requestId, RequestStatus status) throws SQLException {
        String query = "UPDATE transaction_requests SET status = ?, processed_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, requestId);
            return stmt.executeUpdate() > 0;
        }
    }

    private TransactionRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        return new TransactionRequest(
                rs.getInt("id"),
                rs.getString("customer_phone"),
                rs.getString("agent_phone"),
                rs.getDouble("amount"),
                TransactionType.valueOf(rs.getString("type")),
                RequestStatus.valueOf(rs.getString("status")),
                rs.getString("created_at"),
                rs.getString("processed_at"));
    }
}
