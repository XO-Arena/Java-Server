//package dbutil;
//
//import org.junit.jupiter.api.*;
//import java.sql.Connection;
//import java.sql.DatabaseMetaData;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class DBUtilTest {
//
//    private static Connection connection;
//
//    @BeforeAll
//    static void setup() {
//        try {
//            connection = DBUtil.getConnection();
//            assertNotNull(connection, "Connection should not be null");
//            System.out.println("[Test] Connected to DB successfully.");
//        } catch (SQLException e) {
//            fail("Failed to connect to DB: " + e.getMessage());
//        }
//    }
//
//    @Test
//    @Order(1)
//    @DisplayName("Check if 'users' table exists")
//    void testUsersTableExists() {
//        try {
//            DatabaseMetaData metaData = connection.getMetaData();
//            ResultSet tables = metaData.getTables(null, null, "USERS", null);
//
//            assertTrue(tables.next(), "Table 'users' should exist");
//            System.out.println("[Test] Table 'users' exists.");
//        } catch (SQLException e) {
//            fail("Error checking for 'users' table: " + e.getMessage());
//        }
//    }
//
//    @Test
//    @Order(2)
//    @DisplayName("Test connection validity")
//    void testConnectionValid() {
//        try {
//            assertTrue(connection.isValid(2), "Connection should be valid");
//        } catch (SQLException e) {
//            fail("Connection is not valid: " + e.getMessage());
//        }
//    }
//
//    @AfterAll
//    static void cleanup() {
//        try {
//            if (connection != null && !connection.isClosed()) {
//                connection.close();
//                System.out.println("[Test] Connection closed.");
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}
