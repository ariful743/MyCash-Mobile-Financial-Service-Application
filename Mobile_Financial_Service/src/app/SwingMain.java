package app;

import models.*;
import dao.*;
import services.*;
import ui.*;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class SwingMain extends JFrame {

    private final UserDAO userDAO;
    private final TransactionService service;
    private final JPanel mainPanel;
    private final CardLayout cardLayout;

    public SwingMain() {
        // Initialize services
        this.userDAO = new UserDAO();
        this.service = new TransactionService();

        // Setup Frame
        setTitle("MyCash Mobile Financial Service");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Setup Layout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Add Views
        AuthView authView = new AuthView(this, userDAO);
        mainPanel.add(authView, "AUTH");

        add(mainPanel);

        // Start at Auth
        cardLayout.show(mainPanel, "AUTH");
    }

    public void showDashboard(User user) {
        // Create Dashboard fresh each time to get correct user data/type
        DashboardView dashboard = new DashboardView(this, user, userDAO, service);
        mainPanel.add(dashboard, "DASHBOARD");
        cardLayout.show(mainPanel, "DASHBOARD");
    }

    public void logout() {
        cardLayout.show(mainPanel, "AUTH");
    }

    public static void main(String[] args) {
        // Set Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            checkDatabase(); // Check connection before showing UI
            DatabaseInitializer.initialize(); // Initialize tables
            new SwingMain().setVisible(true);
        });
    }

    private static void checkDatabase() {
        boolean connected = false;
        while (!connected) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                connected = true;
            } catch (SQLException e) {
                System.err.println("Database Connection Failed: " + e.getMessage());
                e.printStackTrace();
                JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
                JTextField userField = new JTextField("root");
                JPasswordField passField = new JPasswordField();

                panel.add(new JLabel("DB User:"));
                panel.add(userField);
                panel.add(new JLabel("DB Password:"));
                panel.add(passField);

                int result = JOptionPane.showConfirmDialog(null, panel,
                        "Database Connection Error. Enter Credentials:",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);

                if (result == JOptionPane.OK_OPTION) {
                    DatabaseConnection.setCredentials(userField.getText(), new String(passField.getPassword()));
                } else {
                    System.exit(0);
                }
            }
        }
    }
}
