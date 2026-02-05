
package ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class SwingUtils {

    // Modern FinTech Palette
    public static final Color PRIMARY_COLOR = new Color(26, 35, 126); // Deep Blue
    public static final Color ACCENT_COLOR = new Color(0, 191, 165); // Teal
    public static final Color BG_COLOR = new Color(245, 246, 250); // Soft Gray
    public static final Color CARD_BG_COLOR = Color.WHITE; // White
    public static final Color TEXT_COLOR = new Color(33, 33, 33); // Dark Gray
    public static final Color TEXT_SECONDARY = new Color(117, 117, 117);// Light Gray Text
    public static final Color SUCCESS_COLOR = new Color(67, 160, 71);
    public static final Color ERROR_COLOR = new Color(211, 47, 47);

    // Fonts
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font SUBHEADER_FONT = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 15);

    // Custom Components
    public static JButton createRoundedButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(getBackground().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(getBackground().brighter());
                } else {
                    g2.setColor(getBackground());
                }

                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose();

                super.paintComponent(g);
            }
        };
        btn.setFont(BUTTON_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(12, 24, 12, 24));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createOutlinedButton(String text) {
        JButton btn = createRoundedButton(text);
        btn.setBackground(Color.WHITE);
        btn.setForeground(PRIMARY_COLOR);
        btn.setBorder(new CompoundBorder(
                new LineBorder(PRIMARY_COLOR, 1, true),
                new EmptyBorder(10, 24, 10, 24)));
        return btn;
    }

    public static void styleTextField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10)));
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(CARD_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        return panel;
    }

    // Helper Methods
    public static String captureOutput(Runnable task) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream newOut = new PrintStream(baos);

        try {
            System.setOut(newOut);
            task.run();
        } finally {
            System.setOut(originalOut);
        }
        return baos.toString().trim();
    }

    public static void showResultDialog(Component parent, String output) {
        if (output.isEmpty())
            return;

        boolean isError = output.contains("❌") || output.toLowerCase().contains("fail")
                || output.toLowerCase().contains("error");
        int type = isError ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        String title = isError ? "Transaction Failed" : "Success";

        JOptionPane.showMessageDialog(parent, output, title, type);
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(BODY_FONT);

        // Force Header Style (Fixed Blue/White)
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel header = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                header.setBackground(new Color(0, 51, 204)); // Strong Blue
                header.setForeground(Color.WHITE);
                header.setFont(BUTTON_FONT);
                header.setHorizontalAlignment(JLabel.CENTER);
                header.setOpaque(true);
                header.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                return header;
            }
        });
    }
}
