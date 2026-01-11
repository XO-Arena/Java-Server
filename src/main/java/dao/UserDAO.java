package dao;

import dbutil.DBUtil;
import dto.UserDTO;
import enums.UserGender;
import enums.UserState;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.PasswordUtil;

public class UserDAO {

    public boolean register(String username, String password, UserGender gender) {
        String sql = "INSERT INTO users (username, password, gender, score) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            String hashedPassword = PasswordUtil.hash(password);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, gender.name());
            stmt.setInt(4, 300);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            String hashedPassword = PasswordUtil.hash(password);
            stmt.setString(2, hashedPassword);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        UserGender.valueOf(rs.getString("gender"))
                );
                user.setScore(rs.getInt("score"));
                return user;
            }

            return null;

        } catch (SQLException e) {
            return null;
        }
    }

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

    public boolean cleanAllUsers() {
        String sql = "DELETE FROM users";

        try (Connection conn = DBUtil.getConnection(); Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            return true;

        } catch (SQLException e) {
            return false;
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT username, gender, score FROM users WHERE username = ?";

        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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
            return null;
        }
        return null;
    }
     public List<UserDTO> getLeaderboard() throws SQLException {
        List<UserDTO> leaderboard = new ArrayList<>();

        String sql = "SELECT username, score, gender FROM users ORDER BY score DESC";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                UserDTO player = new UserDTO(
                        rs.getString("username"),
                        rs.getString("gender") != null ? enums.UserGender.valueOf(rs.getString("gender").toUpperCase()) : null,
                        rs.getInt("score"),
                        UserState.OFFLINE
                );
                leaderboard.add(player);
            }
        }

        return leaderboard;
    }

    // get total users in the appliaction
    public int getTotalRegisteredUsers() {
        String sql = "SELECT COUNT(*)FROM users";
        try (Connection conn = DBUtil.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}
