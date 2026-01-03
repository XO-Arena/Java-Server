package dao;

import dbutil.DBUtil;
import enums.UserGender;
import enums.UserState;
import models.User;

import java.sql.*;

public class UserDAO {

    /* =========================
       REGISTER
       ========================= */
    public boolean register(String username, String password, UserGender gender) {
        String sql = "INSERT INTO users (username, password, gender, score, state) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            // TODO: The password should be hashed before storing
            stmt.setString(2, password);
            stmt.setString(3, gender.name());
            stmt.setInt(4, 300);
            stmt.setString(5, UserState.ONLINE.name());

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
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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
       UPDATE STATE
       ========================= */


    /* =========================
       UPDATE SCORE
       ========================= */
    public boolean updateUserScore(String username, int newScore) {
        String sql = "UPDATE users SET score = ? WHERE username = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newScore);
            stmt.setString(2, username);

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {

            return false;
        }
    }

    /* =========================
       FIND USER (NO PASSWORD)
       ========================= */
   
}
