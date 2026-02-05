package services;

import dao.*;
import models.*;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionService {
    private final UserDAO userDAO;
    private final TransactionDAO transactionDAO;
    private final RequestDAO requestDAO;

    public TransactionService(UserDAO userDAO, TransactionDAO transactionDAO, RequestDAO requestDAO) {
        this.userDAO = userDAO;
        this.transactionDAO = transactionDAO;
        this.requestDAO = requestDAO;
    }

    // Default constructor for backward compatibility if needed, but preferred to
    // inject
    public TransactionService() {
        this(new UserDAO(), new TransactionDAO(), new RequestDAO());
    }

    public OperationResult sendMoney(User sender, String receiverPhone, double amount) {
        if (sender.getPhoneNumber().equals(receiverPhone)) {
            return OperationResult.error("Cannot send money to yourself.");
        }
        if (sender.getBalance() < amount) {
            return OperationResult.error("Insufficient Balance.");
        }

        User receiver = userDAO.getUserByPhone(receiverPhone);
        if (receiver == null) {
            return OperationResult.error("Receiver not found.");
        }

        double fee = 5.0; // Fixed fee for send money
        double totalToDeduct = amount + fee;

        if (sender.getBalance() < totalToDeduct) {
            return OperationResult.error("Insufficient Balance for amount + fee.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                userDAO.updateBalance(conn, sender.getPhoneNumber(), sender.getBalance() - totalToDeduct);
                userDAO.updateBalance(conn, receiver.getPhoneNumber(), receiver.getBalance() + amount);
                // System profit is not tracked in a user account for simplicity

                transactionDAO.logTransaction(conn, sender.getPhoneNumber(), receiverPhone, TransactionType.SEND_MONEY,
                        amount);

                conn.commit();
                sender.setBalance(sender.getBalance() - totalToDeduct);
                return OperationResult.ok("Send Money Successful! (Fee: 5.0)");
            } catch (SQLException e) {
                conn.rollback();
                return OperationResult.error("Transaction Failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            return OperationResult.error("Database connection error.");
        }
    }

    public OperationResult cashOut(User customer, String agentPhone, double amount) {
        return submitRequest(customer, agentPhone, amount, TransactionType.CASH_OUT);
    }

    public OperationResult cashIn(User customer, String agentPhone, double amount) {
        return submitRequest(customer, agentPhone, amount, TransactionType.CASH_IN);
    }

    private OperationResult submitRequest(User customer, String agentPhone, double amount, TransactionType type) {
        User agent = validateAgent(agentPhone, customer.getPhoneNumber());
        if (agent == null)
            return OperationResult.error("Invalid Agent Number.");

        if (requestDAO.createTransactionRequest(customer.getPhoneNumber(), agentPhone, amount, type)) {
            transactionDAO.logTransaction(customer.getPhoneNumber(), agentPhone,
                    TransactionType.valueOf(type.name() + "_REQ"), amount);
            return OperationResult.ok(type.name() + " Request Submitted! Waiting for agent approval.");
        } else {
            return OperationResult.error("Failed to create " + type.name() + " request.");
        }
    }

    public OperationResult requestAddMoney(User agent, double amount) {
        if (requestDAO.createTransactionRequest(agent.getPhoneNumber(), "BANK", amount, TransactionType.ADD_MONEY)) {
            transactionDAO.logTransaction(agent.getPhoneNumber(), "BANK", TransactionType.ADD_MONEY_REQ, amount);
            return OperationResult.ok("Add Money Request Submitted! Waiting for Bank approval.");
        } else {
            return OperationResult.error("Failed to submit Add Money request.");
        }
    }

    public OperationResult approveTransactionRequest(int requestId, User approver) {
        TransactionRequest request = requestDAO.getTransactionRequest(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return OperationResult.error("Request invalid or already processed.");
        }

        // Fetch fresh state of the approver and the requester
        User freshApprover = userDAO.getUserByPhone(approver.getPhoneNumber());
        User requester = userDAO.getUserByPhone(request.getCustomerPhone());

        if (freshApprover == null || requester == null) {
            return OperationResult.error("User not found.");
        }

        // --- Permission Checks ---
        if (request.getType() == TransactionType.ADD_MONEY) {
            if (freshApprover.getRole() != UserRole.BANK) {
                return OperationResult.error("Unauthorized: Only Banking Authority can approve Add Money.");
            }
        } else {
            if (freshApprover.getRole() != UserRole.AGENT) {
                return OperationResult.error("Unauthorized: Only Agents can approve this request.");
            }
            if (!request.getAgentPhone().equals(freshApprover.getPhoneNumber())) {
                return OperationResult.error("Unauthorized: This request was not sent to you.");
            }
        }
        // -------------------------

        double amount = request.getAmount();
        switch (request.getType()) {
            case CASH_OUT:
                return processCashOutApproval(requestId, requester, freshApprover, amount);
            case CASH_IN:
                return processCashInApproval(requestId, requester, freshApprover, amount);
            case ADD_MONEY:
                return processAddMoneyApproval(requestId, freshApprover, amount);
            default:
                return OperationResult.error("Unsupported request type.");
        }
    }

    private OperationResult processAddMoneyApproval(int requestId, User agent, double amount) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // System balance deduction is not tracked in a user account for simplicity
                userDAO.updateBalance(conn, agent.getPhoneNumber(), agent.getBalance() + amount);
                requestDAO.updateRequestStatus(conn, requestId, RequestStatus.APPROVED);
                transactionDAO.logTransaction(conn, "BANK", agent.getPhoneNumber(), TransactionType.APPROVED_ADD_MONEY,
                        amount);

                conn.commit();
                agent.setBalance(agent.getBalance() + amount);
                return OperationResult.ok("Add Money Approved for Agent!");
            } catch (SQLException e) {
                conn.rollback();
                return OperationResult.error("Approval Failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            return OperationResult.error("Database connection error.");
        }
    }

    private OperationResult processCashOutApproval(int requestId, User customer, User agent, double amount) {
        double totalCharge = amount * 0.0185;
        double totalToDeduct = amount + totalCharge;

        if (customer.getBalance() < totalToDeduct)
            return OperationResult.error("Customer has insufficient balance.");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                userDAO.updateBalance(conn, customer.getPhoneNumber(), customer.getBalance() - totalToDeduct);
                userDAO.updateBalance(conn, agent.getPhoneNumber(), agent.getBalance() + totalToDeduct);

                requestDAO.updateRequestStatus(conn, requestId, RequestStatus.APPROVED);
                transactionDAO.logTransaction(conn, customer.getPhoneNumber(), agent.getPhoneNumber(),
                        TransactionType.APPROVED_CASH_OUT, amount);

                conn.commit();
                customer.setBalance(customer.getBalance() - totalToDeduct);
                agent.setBalance(agent.getBalance() + totalToDeduct);
                return OperationResult.ok("Cashout Approved! (Fee: " + String.format("%.2f", totalCharge) + ")");
            } catch (SQLException e) {
                conn.rollback();
                return OperationResult.error("Transaction Failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            return OperationResult.error("Database connection error.");
        }
    }

    private OperationResult processCashInApproval(int requestId, User customer, User agent, double amount) {
        double agentCommission = amount * 0.005; // Agent gets 0.5% incentive for Cash In
        double netDeduction = amount - agentCommission;

        if (agent.getBalance() < netDeduction)
            return OperationResult.error("Agent has insufficient balance.");

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                userDAO.updateBalance(conn, agent.getPhoneNumber(), agent.getBalance() - netDeduction);
                userDAO.updateBalance(conn, customer.getPhoneNumber(), customer.getBalance() + amount);

                requestDAO.updateRequestStatus(conn, requestId, RequestStatus.APPROVED);
                transactionDAO.logTransaction(conn, agent.getPhoneNumber(), customer.getPhoneNumber(),
                        TransactionType.APPROVED_CASH_IN, amount);

                conn.commit();
                agent.setBalance(agent.getBalance() - netDeduction);
                customer.setBalance(customer.getBalance() + amount);
                return OperationResult.ok("Cash-In Approved! (Balance Deducted)");
            } catch (SQLException e) {
                conn.rollback();
                return OperationResult.error("Transaction Failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            return OperationResult.error("Database connection error.");
        }
    }

    public OperationResult rejectTransactionRequest(int requestId) {
        TransactionRequest request = requestDAO.getTransactionRequest(requestId);
        if (request == null || request.getStatus() != RequestStatus.PENDING) {
            return OperationResult.error("Request invalid or already processed.");
        }

        if (requestDAO.updateRequestStatus(requestId, RequestStatus.REJECTED)) {
            transactionDAO.logTransaction(request.getAgentPhone(), request.getCustomerPhone(),
                    TransactionType.REJECTED_REQ, request.getAmount());
            return OperationResult.ok("Request Rejected.");
        } else {
            return OperationResult.error("Failed to reject request.");
        }
    }

    private User validateAgent(String agentPhone, String customerPhone) {
        User agent = userDAO.getUserByPhone(agentPhone);
        if (agent == null || agent.getRole() != UserRole.AGENT || agent.getPhoneNumber().equals(customerPhone)) {
            return null;
        }
        return agent;
    }
}