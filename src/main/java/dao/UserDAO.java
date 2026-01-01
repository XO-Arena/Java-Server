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
   

    /* =========================
       UPDATE STATE
       ========================= */


    /* =========================
       UPDATE SCORE
       ========================= */


    /* =========================
       FIND USER (NO PASSWORD)
       ========================= */
   
}
