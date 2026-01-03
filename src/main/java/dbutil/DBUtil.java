package dbutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBUtil {

    private static final String HOST_URL = "jdbc:derby://localhost:1527/xo_database;create=true";
    private static final String DB_NAME = "xo_database";
    private static final String USER = "root";
    private static final String PASS = "root";

    // Static block runs once when the class is loaded
    static {
        initDatabase();
        initTables();
    }

    // Create database if it does not exist
    private static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(HOST_URL, USER, PASS)) {
            System.out.println("[DB] Connected to database: " + DB_NAME);
        } catch (SQLException e) {
            System.out.println("[DB] Cannot connect to database: " + DB_NAME);
        }
    }

    // Create tables if they do not exist
    private static void initTables() {
        String sql
                = "CREATE TABLE users ("
                + "username VARCHAR(50) PRIMARY KEY, "
                + "password VARCHAR(256) NOT NULL, "
                + "gender VARCHAR(10) CHECK (gender IN ('MALE','FEMALE')), "
                + "score INT DEFAULT 0"
                + ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("[DB] Table 'users' created.");

        } catch (SQLException e) {
            if ("X0Y32".equals(e.getSQLState())) // Table exists
            {
                System.out.println("[DB] Table 'users' already exists, skipping creation.");
            }

        }
    }

    // Get connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                HOST_URL,
                USER,
                PASS
        );
    }
}
