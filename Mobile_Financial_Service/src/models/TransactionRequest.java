package models;

public class TransactionRequest {
    private int id;
    private String customerPhone;
    private String agentPhone;
    private double amount;
    private TransactionType type;
    private RequestStatus status;
    private String createdAt;
    private String processedAt;

    public TransactionRequest(int id, String customerPhone, String agentPhone, double amount,
            TransactionType type, RequestStatus status, String createdAt, String processedAt) {
        this.id = id;
        this.customerPhone = customerPhone;
        this.agentPhone = agentPhone;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.createdAt = createdAt;
        this.processedAt = processedAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    // Setters
    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
}
