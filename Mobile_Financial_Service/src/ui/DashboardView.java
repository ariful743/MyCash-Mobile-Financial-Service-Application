package ui;

import models.*;
import dao.UserDAO;
import dao.TransactionDAO;
import dao.RequestDAO;
import services.TransactionService;
import services.OperationResult;
import app.SwingMain;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DashboardView extends JPanel {
    private SwingMain mainFrame;
    private User currentUser;
    private UserDAO userDAO;
    private TransactionDAO transactionDAO;
    private RequestDAO requestDAO;
    private TransactionService service;

    private JLabel balanceLabel;
    private JLabel nameLabel;

    public DashboardView(SwingMain mainFrame, User user, UserDAO userDAO, TransactionService service) {
        this.mainFrame = mainFrame;
        this.currentUser = user;
        this.userDAO = userDAO;
        this.service = service;
        // The service already has these, but we need them for direct DAO calls here
        // (e.g., statements)
        this.transactionDAO = new TransactionDAO();
        this.requestDAO = new RequestDAO();

        setLayout(new BorderLayout());
        setBackground(SwingUtils.BG_COLOR);

        setupHeader();
        setupContent();
        setupBottomBar();
    }

    private void setupHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SwingUtils.PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(800, 150));
        headerPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        String greeting = (currentUser.getRole() == UserRole.BANK) ? "MyCash" : "Hello, " + currentUser.getName();
        nameLabel = new JLabel(greeting);
        nameLabel.setFont(SwingUtils.HEADER_FONT);
        nameLabel.setForeground(Color.WHITE);

        JPanel balanceCard = new JPanel(new BorderLayout());
        balanceCard.setOpaque(false);

        JLabel lblBalanceTitle = new JLabel();
        lblBalanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblBalanceTitle.setForeground(new Color(255, 255, 255, 200));

        balanceLabel = new JLabel();
        balanceLabel.setForeground(Color.WHITE);

        if (currentUser.getRole() == UserRole.BANK) {
            lblBalanceTitle.setText("Account Status");
            balanceLabel.setText("Banking Authority");
            balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        } else {
            lblBalanceTitle.setText("Available Balance");
            balanceLabel.setText("৳ " + currentUser.getBalance());
            balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        }

        balanceCard.add(lblBalanceTitle, BorderLayout.NORTH);
        balanceCard.add(balanceLabel, BorderLayout.CENTER);

        headerPanel.add(nameLabel, BorderLayout.WEST);
        headerPanel.add(balanceCard, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void setupContent() {
        JPanel contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        contentPanel.setBackground(SwingUtils.BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);

        switch (currentUser.getRole()) {
            case CUSTOMER:
                gridPanel.add(createActionCard("Send Money", "💸", this::handleSendMoney));
                gridPanel.add(createActionCard("Cash Out", "🏧", this::handleCashOut));
                gridPanel.add(createActionCard("Cash In", "💰", this::handleCashIn));
                gridPanel.add(createActionCard("Statement", "📄", this::handleStatement));
                break;
            case BANK:
                gridPanel.add(createActionCard("All Customers", "👥", this::handleViewCustomers));
                gridPanel.add(createActionCard("All Agents", "🏢", this::handleViewAgents));
                gridPanel.add(createActionCard("Pending Requests", "📋", this::handlePendingRequests));
                gridPanel.add(createActionCard("Statement", "📄", this::handleStatement));
                break;
            case AGENT:
                gridPanel.add(createActionCard("Add Money", "➕", this::handleAddMoney));
                gridPanel.add(createActionCard("Pending Requests", "📋", this::handlePendingRequests));
                gridPanel.add(createActionCard("Statement", "📄", this::handleStatement));
                break;
        }

        contentPanel.add(gridPanel);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupBottomBar() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(SwingUtils.BG_COLOR);
        bottomPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        JButton logoutBtn = SwingUtils.createOutlinedButton("Logout");
        logoutBtn.setBorder(BorderFactory.createLineBorder(SwingUtils.ERROR_COLOR));
        logoutBtn.setForeground(SwingUtils.ERROR_COLOR);
        logoutBtn.addActionListener(e -> mainFrame.logout());

        bottomPanel.add(logoutBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JButton createActionCard(String title, String icon, Runnable action) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setPreferredSize(new Dimension(160, 120));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));

        JLabel textLabel = new JLabel(title, SwingConstants.CENTER);
        textLabel.setFont(SwingUtils.BUTTON_FONT);
        textLabel.setForeground(SwingUtils.TEXT_COLOR);
        textLabel.setBorder(new EmptyBorder(0, 0, 15, 0));

        btn.add(iconLabel, BorderLayout.CENTER);
        btn.add(textLabel, BorderLayout.SOUTH);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void refreshUser() {
        User updated = userDAO.getUserByPhone(currentUser.getPhoneNumber());
        if (updated != null) {
            this.currentUser = updated;
            balanceLabel
                    .setText(updated.getRole() == UserRole.BANK ? "Banking Authority" : "৳ " + updated.getBalance());
        }
    }

    private void handleSendMoney() {
        showTransactionDialog("Send Money", (rcv, amt) -> service.sendMoney(currentUser, rcv, amt));
    }

    private void handleCashOut() {
        showTransactionDialog("Cash Out", (agt, amt) -> service.cashOut(currentUser, agt, amt));
    }

    private void handleCashIn() {
        showTransactionDialog("Cash In Request", (agt, amt) -> service.cashIn(currentUser, agt, amt));
    }

    private void handleStatement() {
        List<String[]> history = (currentUser.getRole() == UserRole.BANK)
                ? transactionDAO.getAllTransactions()
                : transactionDAO.getTransactionHistory(currentUser.getPhoneNumber());

        String title = (currentUser.getRole() == UserRole.BANK) ? "All System Transactions" : "Transaction Statement";
        String[] columns = { "Time", "Type", "Detail", "Amount" };

        JTable table = new JTable(history.toArray(new String[0][]), columns);
        SwingUtils.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(400);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void handleViewCustomers() {
        showUserListDialog(UserRole.CUSTOMER, "All Registered Customers");
    }

    private void handleViewAgents() {
        showUserListDialog(UserRole.AGENT, "All Registered Agents");
    }

    private void showUserListDialog(UserRole role, String title) {
        List<String[]> users = userDAO.getAllUsersByType(role);
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No " + role.name().toLowerCase() + "s found.");
            return;
        }

        String[] columns = { "Name", "Phone Number", "Current Balance" };
        JTable table = new JTable(users.toArray(new String[0][]), columns);
        SwingUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.PLAIN_MESSAGE);
    }

    private void handleAddMoney() {
        String amtStr = JOptionPane.showInputDialog(this, "Enter Amount to Request from Bank:");
        if (amtStr == null)
            return;
        try {
            double amount = Double.parseDouble(amtStr);
            OperationResult result = service.requestAddMoney(currentUser, amount);
            JOptionPane.showMessageDialog(this, result.getMessage(), result.isSuccess() ? "Success" : "Error",
                    result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            refreshUser();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Amount");
        }
    }

    private void handlePendingRequests() {
        String receiverId = (currentUser.getRole() == UserRole.BANK) ? "BANK" : currentUser.getPhoneNumber();
        List<TransactionRequest> requests = requestDAO.getPendingRequests(receiverId);

        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending requests.");
            return;
        }

        String[] columns = { "ID", "Type", "Requester", "Amount", "Time" };
        Object[][] data = new Object[requests.size()][5];
        for (int i = 0; i < requests.size(); i++) {
            TransactionRequest req = requests.get(i);
            data[i][0] = req.getId();
            data[i][1] = req.getType();
            data[i][2] = req.getCustomerPhone();
            data[i][3] = "৳ " + req.getAmount();
            data[i][4] = req.getCreatedAt();
        }

        JTable table = new JTable(data, columns);
        SwingUtils.styleTable(table);

        JPanel actionPanel = new JPanel();
        JButton approveBtn = SwingUtils.createRoundedButton("Approve");
        JButton rejectBtn = SwingUtils.createRoundedButton("Reject");
        rejectBtn.setBackground(SwingUtils.ERROR_COLOR);

        approveBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            int id = (int) table.getValueAt(row, 0);
            OperationResult result = service.approveTransactionRequest(id, currentUser);
            JOptionPane.showMessageDialog(this, result.getMessage());
            refreshUser();
            SwingUtilities.getWindowAncestor(table).dispose();
            handlePendingRequests();
        });

        rejectBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1)
                return;
            int id = (int) table.getValueAt(row, 0);
            OperationResult result = service.rejectTransactionRequest(id);
            JOptionPane.showMessageDialog(this, result.getMessage());
            SwingUtilities.getWindowAncestor(table).dispose();
            handlePendingRequests();
        });

        actionPanel.add(approveBtn);
        actionPanel.add(rejectBtn);

        JPanel dialogPanel = new JPanel(new BorderLayout());
        dialogPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        dialogPanel.add(actionPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, dialogPanel, "Pending Requests", JOptionPane.PLAIN_MESSAGE);
    }

    interface BiFunctionResult {
        OperationResult apply(String s, Double d);
    }

    private void showTransactionDialog(String title, BiFunctionResult action) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        JTextField phoneField = new JTextField();
        JTextField amountField = new JTextField();
        panel.add(new JLabel("Recipient/Agent Mobile Number:"));
        panel.add(phoneField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, title, JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                double amount = Double.parseDouble(amountField.getText());
                OperationResult opResult = action.apply(phoneField.getText(), amount);
                JOptionPane.showMessageDialog(this, opResult.getMessage(), opResult.isSuccess() ? "Success" : "Error",
                        opResult.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                refreshUser();
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Input");
            }
        }
    }
}
