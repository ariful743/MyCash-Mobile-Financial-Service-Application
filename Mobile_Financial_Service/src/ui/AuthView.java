package ui;

import models.*;
import dao.UserDAO;
import app.SwingMain;
import javax.swing.*;
import java.awt.*;

public class AuthView extends JPanel {
    private SwingMain mainFrame;
    private UserDAO userDAO;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private String selectedRole = "CUSTOMER";
    private JLabel nameDisplayLabel;

    public AuthView(SwingMain mainFrame, UserDAO userDAO) {
        this.mainFrame = mainFrame;
        this.userDAO = userDAO;

        setLayout(new GridBagLayout());
        setBackground(SwingUtils.BG_COLOR);

        JPanel mainContainer = SwingUtils.createCardPanel();
        mainContainer.setLayout(new BorderLayout());
        mainContainer.setPreferredSize(new Dimension(400, 600));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel titleLabel = new JLabel("MyCash");
        titleLabel.setFont(SwingUtils.HEADER_FONT);
        titleLabel.setForeground(SwingUtils.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Mobile Financial Service Application");
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        subtitleLabel.setForeground(SwingUtils.SUCCESS_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(subtitleLabel);

        mainContainer.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(SwingUtils.CARD_BG_COLOR);

        cardPanel.add(createLoginPanel(), "LOGIN");
        cardPanel.add(createRegisterPanel(), "REGISTER");

        mainContainer.add(cardPanel, BorderLayout.CENTER);
        add(mainContainer);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SwingUtils.CARD_BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JTextField phoneField = new JTextField(15);
        SwingUtils.styleTextField(phoneField);

        JPasswordField pinField = new JPasswordField(15);
        SwingUtils.styleTextField(pinField);

        JButton loginBtn = SwingUtils.createRoundedButton("Login");

        nameDisplayLabel = new JLabel(" ");
        nameDisplayLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        nameDisplayLabel.setForeground(SwingUtils.PRIMARY_COLOR);
        nameDisplayLabel.setHorizontalAlignment(SwingConstants.LEFT);

        phoneField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                checkName();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                checkName();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                checkName();
            }

            private void checkName() {
                String phone = phoneField.getText();
                if (phone.length() == 11) {
                    new SwingWorker<User, Void>() {
                        @Override
                        protected User doInBackground() {
                            return userDAO.getUserByPhone(phone);
                        }

                        @Override
                        protected void done() {
                            try {
                                User user = get();
                                if (user != null) {
                                    nameDisplayLabel.setText("👤 Account: " + user.getName());
                                    nameDisplayLabel.setForeground(SwingUtils.SUCCESS_COLOR);
                                } else {
                                    nameDisplayLabel.setText("❓ Account not found");
                                    nameDisplayLabel.setForeground(SwingUtils.ERROR_COLOR);
                                }
                            } catch (Exception ex) {
                                nameDisplayLabel.setText("⚠️ DB Error");
                                nameDisplayLabel.setForeground(SwingUtils.ERROR_COLOR);
                            }
                        }
                    }.execute();
                } else {
                    nameDisplayLabel.setText(" ");
                }
            }
        });

        JButton goToRegBtn = new JButton("Create New Account");
        goToRegBtn.setFont(SwingUtils.BODY_FONT);
        goToRegBtn.setForeground(SwingUtils.PRIMARY_COLOR);
        goToRegBtn.setBorderPainted(false);
        goToRegBtn.setContentAreaFilled(false);
        goToRegBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel togglePanel = new JPanel(new GridLayout(1, 3, 5, 0));
        togglePanel.setBackground(Color.WHITE);

        JButton btnUser = SwingUtils.createRoundedButton("Customer");
        JButton btnAgent = SwingUtils.createRoundedButton("Agent");
        JButton btnBank = SwingUtils.createRoundedButton("Bank");

        styleToggleButton(btnUser, true);
        styleToggleButton(btnAgent, false);
        styleToggleButton(btnBank, false);

        JLabel lblPhone = new JLabel("Mobile Number");
        lblPhone.setFont(SwingUtils.BODY_FONT);

        btnUser.addActionListener(e -> {
            selectedRole = UserRole.CUSTOMER.name();
            styleToggleButton(btnUser, true);
            styleToggleButton(btnAgent, false);
            styleToggleButton(btnBank, false);
            lblPhone.setText("Mobile Number");
        });

        btnAgent.addActionListener(e -> {
            selectedRole = UserRole.AGENT.name();
            styleToggleButton(btnAgent, true);
            styleToggleButton(btnUser, false);
            styleToggleButton(btnBank, false);
            lblPhone.setText("Mobile Number");
        });

        btnBank.addActionListener(e -> {
            selectedRole = UserRole.BANK.name();
            styleToggleButton(btnBank, true);
            styleToggleButton(btnUser, false);
            styleToggleButton(btnAgent, false);
            lblPhone.setText("Account No (11 digit)");
        });

        togglePanel.add(btnUser);
        togglePanel.add(btnAgent);
        togglePanel.add(btnBank);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel roleLabel = new JLabel("Select Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(roleLabel, gbc);

        gbc.gridy = 1;
        panel.add(togglePanel, gbc);
        gbc.gridy = 2;
        panel.add(lblPhone, gbc);
        gbc.gridy = 3;
        panel.add(phoneField, gbc);
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 5, 10, 5);
        panel.add(nameDisplayLabel, gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 5, 5, 5);
        JLabel lblPin = new JLabel("PIN");
        lblPin.setFont(SwingUtils.BODY_FONT);
        panel.add(lblPin, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 5, 10, 5);
        panel.add(pinField, gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 5, 10, 5);
        panel.add(loginBtn, gbc);
        gbc.gridy = 8;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(goToRegBtn, gbc);

        loginBtn.addActionListener(e -> {
            String phone = phoneField.getText();
            String pin = new String(pinField.getPassword());
            if (phone.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userDAO.getUserByPhone(phone);
            if (user != null && user.validatePin(pin)) {
                if (!selectedRole.equals(user.getRole().name())) {
                    JOptionPane.showMessageDialog(this, "Access Denied: Account is not a " + selectedRole,
                            "Role Mismatch", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                mainFrame.showDashboard(user);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Phone or PIN", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        goToRegBtn.addActionListener(e -> cardLayout.show(cardPanel, "REGISTER"));
        return panel;
    }

    private void styleToggleButton(JButton btn, boolean isSelected) {
        if (isSelected) {
            btn.setBackground(SwingUtils.PRIMARY_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(SwingUtils.PRIMARY_COLOR, 2));
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.BLACK);
            btn.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        }
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(SwingUtils.CARD_BG_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(15);
        SwingUtils.styleTextField(nameField);
        JTextField phoneField = new JTextField(15);
        SwingUtils.styleTextField(phoneField);
        JPasswordField pinField = new JPasswordField(15);
        SwingUtils.styleTextField(pinField);

        UserRole[] roles = UserRole.values();
        JComboBox<UserRole> typeBox = new JComboBox<>(roles);
        typeBox.setFont(SwingUtils.BODY_FONT);
        typeBox.setBackground(Color.WHITE);

        JButton regBtn = SwingUtils.createRoundedButton("Register");
        JButton backBtn = new JButton("Back");
        backBtn.setFont(SwingUtils.BODY_FONT);
        backBtn.setForeground(SwingUtils.TEXT_SECONDARY);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Full Name"), gbc);
        gbc.gridy = 1;
        panel.add(nameField, gbc);
        gbc.gridy = 2;
        JLabel lblMobile = new JLabel("Mobile Number");
        panel.add(lblMobile, gbc);
        gbc.gridy = 3;
        panel.add(phoneField, gbc);
        gbc.gridy = 4;
        panel.add(new JLabel("Set PIN (4-digit)"), gbc);
        gbc.gridy = 5;
        panel.add(pinField, gbc);
        gbc.gridy = 6;
        panel.add(new JLabel("Account Type"), gbc);
        gbc.gridy = 7;
        panel.add(typeBox, gbc);
        gbc.gridy = 8;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(regBtn, gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(0, 5, 5, 5);
        panel.add(backBtn, gbc);

        regBtn.addActionListener(e -> {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String pin = new String(pinField.getPassword());
            UserRole role = (UserRole) typeBox.getSelectedItem();

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double initialBal = (role == UserRole.AGENT) ? 10000.0 : (role == UserRole.CUSTOMER ? 50.0 : 0.0);
            if (userDAO.registerUser(phone, name, pin, role.name(), initialBal)) {
                JOptionPane.showMessageDialog(this, "Registration Successful!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                cardLayout.show(cardPanel, "LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Registration Failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        typeBox.addActionListener(e -> {
            UserRole role = (UserRole) typeBox.getSelectedItem();
            lblMobile.setText(role == UserRole.BANK ? "Account No (11 digit)" : "Mobile Number");
        });

        backBtn.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));
        return panel;
    }
}
