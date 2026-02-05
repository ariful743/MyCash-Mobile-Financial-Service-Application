package app;

import dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Scanner;

public class dbReset {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("--- 🛠️ MySQL Database Reset Tool ---");
        System.out.print("Enter MySQL Username (default root): ");
        String user = scanner.nextLine();
        if (user.isEmpty())
            user = "root";

        System.out.print("Enter MySQL Password: ");
        String pass = scanner.nextLine();

        DatabaseConnection.setCredentials(user, pass);

        String[] tables = { "transaction_requests", "transactions", "users" };

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            System.out.println("⏳ Resetting database...");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            for (String table : tables) {
                try {
                    stmt.execute("TRUNCATE TABLE " + table);
                    System.out.println("✅ Table [" + table + "] cleared.");
                } catch (Exception e) {
                    System.out.println("❌ Failed to truncate " + table + ": " + e.getMessage());
                }
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            System.out.println("\n🚀 DATABASE FULLY RESET!");
        } catch (Exception e) {
            System.out.println("\n❌ ERROR: " + e.getMessage());
            System.out.println("Please make sure your database server is running and credentials are correct.");
        }
    }
}
