package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // ⚠️ UPDATE USER AND PASSWORD HERE
    private static final String URL = "jdbc:mysql://localhost:3306/mycash_db";
    private static String USER = "root";
    private static String PASS = "1234";

    public static Connection getConnection() throws SQLException {
        try {
            // Explicitly load the driver so we get a clear ClassNotFoundException when it's missing
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Make sure the driver JAR is on the classpath.", e);
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void setCredentials(String user, String pass) {
        USER = user;
        PASS = pass;
    }
}