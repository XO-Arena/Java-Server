package dbutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static final String HOST_URL = "jdbc:mysql://localhost:1527/";
    private static final String DB_NAME  = "xo_database";
    private static final String USER     = "root";
    private static final String PASS     = "root";

    // Static block runs once when the class is loaded
    static {
        initDatabase();
        initTables();
    }

    // Create database if it does not exist
    private static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(HOST_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS " + DB_NAME
            );
            System.out.println("[DB] Database initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Create tables if they do not exist
    private static void initTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            String sql =
                    "CREATE TABLE IF NOT EXISTS users ("
                  + "username VARCHAR(50) PRIMARY KEY, "
                  + "password VARCHAR(255) NOT NULL, "
                  + "gender ENUM('MALE','FEMALE'), "
                  + "score INT DEFAULT 0"
                  + ")";

            stmt.executeUpdate(sql);

            System.out.println("[DB] Tables initialized.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                HOST_URL + DB_NAME,
                USER,
                PASS
        );
    }
}
