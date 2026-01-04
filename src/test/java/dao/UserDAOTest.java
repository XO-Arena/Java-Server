package dao;

import enums.UserGender;
import models.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static UserDAO userDAO;

    private static final String TEST_USERNAME = "unitTestUser";
    private static final String TEST_PASSWORD = "pass123";
    private static final UserGender TEST_GENDER = UserGender.MALE;

    @BeforeAll
    static void setup() {
        userDAO = new UserDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Register new user successfully")
    void testRegisterNewUser() {
        boolean registered = userDAO.register(TEST_USERNAME, TEST_PASSWORD, TEST_GENDER);
        assertTrue(registered, "New user should be registered successfully");
    }

    @Test
    @Order(2)
    @DisplayName("Register existing user should fail")
    void testRegisterExistingUser() {
        boolean registered = userDAO.register(TEST_USERNAME, TEST_PASSWORD, TEST_GENDER);
        assertFalse(registered, "Registering existing user should fail");
    }

    @Test
    @Order(3)
    @DisplayName("Login with correct credentials")
    void testLoginSuccess() {
        User user = userDAO.login(TEST_USERNAME, TEST_PASSWORD);
        assertNotNull(user, "Login should return a user object");
        assertEquals(TEST_USERNAME, user.getUsername());
        assertEquals(TEST_GENDER, user.getGender());
    }

    @Test
    @Order(4)
    @DisplayName("Login with incorrect password should fail")
    void testLoginWrongPassword() {
        User user = userDAO.login(TEST_USERNAME, "wrongPass");
        assertNull(user, "Login with wrong password should return null");
    }

    @Test
    @Order(5)
    @DisplayName("Update score for existing user")
    void testUpdateScoreExistingUser() {
        int newScore = 500;
        boolean updated = userDAO.updateUserScore(TEST_USERNAME, newScore);
        assertTrue(updated, "Score update should succeed");

        User user = userDAO.getUserByUsername(TEST_USERNAME);
        assertEquals(newScore, user.getScore(), "User score should be updated correctly");
    }

    @Test
    @Order(6)
    @DisplayName("Update score for non-existing user should fail")
    void testUpdateScoreNonExistingUser() {
        boolean updated = userDAO.updateUserScore("nonExistentUser", 100);
        assertFalse(updated, "Score update for non-existing user should fail");
    }

    @AfterAll
    static void cleanup() {
        boolean cleaned = userDAO.cleanAllUsers();
        if (cleaned) {
            System.out.println("[Test] All test users deleted successfully.");
        } else {
            System.out.println("[Test] No users were deleted or error occurred.");
        }
    }
}
