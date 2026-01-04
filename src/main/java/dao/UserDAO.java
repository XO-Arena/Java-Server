package dao;

import dbutil.DBUtil;
import enums.UserGender;
import models.User;

import java.sql.*;

public class UserDAO {

    /* =========================
       REGISTER
       ========================= */
    public boolean register(String username, String password, UserGender gender) {
        String sql = "INSERT INTO users (username, password, gender, score) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            // TODO: The password should be hashed before storing
            stmt.setString(2, password);
            stmt.setString(3, gender.name());
            stmt.setInt(4, 300);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    /* =========================
       LOGIN
       ========================= */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            // TODO: The password should be hashed before setting
            stmt.setString(2, password);

            ResultSet userRS = stmt.executeQuery();
            userRS.first();
            User user = new User(userRS.getString("username"), UserGender.valueOf(userRS.getString("gender")));
            return user;

        } catch (SQLException e) {
            return null;
        }
    }

    /* =========================
       UPDATE SCORE
       ========================= */
    public boolean updateUserScore(String username, int newScore) {
        String sql = "UPDATE users SET score = ? WHERE username = ?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newScore);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {

            return false;
        }
    }

    /* =========================
       DELETE USER
       ========================= */
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    /* =========================
       CLEAN ALL USERS
       ========================= */
    public boolean cleanAllUsers() {
        String sql = "DELETE FROM users";

        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            return true;

        } catch (SQLException e) {
            return false;
        }
    }
    /* =========================
       FIND USER (NO PASSWORD)
       ========================= */
    // this function return the user by the username
    public User getUserByUsername(String username) {
        String sql = "SELECT username, gender, score FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            // as the pointer is aready null 
            // to avoid the null pointer exeption
            if (rs.next()) {
            User user = new User(
                    rs.getString("username"),
                    UserGender.valueOf(rs.getString("gender"))
            );
            user.setScore(rs.getInt("score"));
            return user;
        }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
