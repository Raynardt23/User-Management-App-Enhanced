package com.example.dao;

import com.example.model.User;
import com.example.util.SecurityUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public UserDAO() {
    }

    // Create users table if it does not exist
    public void createUsersTableIfNotExists() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(100) NOT NULL,
                    first_name VARCHAR(255),
                    last_name VARCHAR(255),
                    role ENUM('admin', 'user') DEFAULT 'user',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Users table verified/created.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create users table", e);
        }
    }

    // Create default admin if not exists
    public void createDefaultAdmin() {
        try {
            if (findByUsername("admin") != null) {
                System.out.println("Admin user already exists.");
                return;
            }

            String sql = "INSERT INTO users (username, password, email, first_name, last_name, role) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, "admin");
                stmt.setString(2, SecurityUtil.hashPassword("admin123"));
                stmt.setString(3, SecurityUtil.encrypt("admin@example.com"));
                stmt.setString(4, SecurityUtil.encrypt("System"));
                stmt.setString(5, SecurityUtil.encrypt("Administrator"));
                stmt.setString(6, "admin");

                stmt.executeUpdate();
                System.out.println("Default admin created successfully.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== CREATE USER =====
    public boolean createUser(User user) {
        String sql = "INSERT INTO users (username, password, email, first_name, last_name, role) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SecurityUtil.escapeHtml(user.getUsername()));
            stmt.setString(2, user.getPassword()); // already hashed
            stmt.setString(3, SecurityUtil.encrypt(user.getEmail()));
            stmt.setString(4, SecurityUtil.encrypt(user.getFirstName()));
            stmt.setString(5, SecurityUtil.encrypt(user.getLastName()));
            stmt.setString(6, user.getRole() != null ? user.getRole() : "user");

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== FIND BY USERNAME =====
    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== FIND BY EMAIL =====
    public User findByEmail(String email) {
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String decryptedEmail = SecurityUtil.decrypt(rs.getString("email"));
                if (email.equals(decryptedEmail)) {
                    return extractUserFromResultSet(rs);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== UPDATE USER =====
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET email=?, first_name=?, last_name=?, role=?, updated_at=NOW() WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SecurityUtil.encrypt(user.getEmail()));
            stmt.setString(2, SecurityUtil.encrypt(user.getFirstName()));
            stmt.setString(3, SecurityUtil.encrypt(user.getLastName()));
            stmt.setString(4, user.getRole());
            stmt.setInt(5, user.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, SecurityUtil.hashPassword(newPassword));
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ===== GET ALL USERS =====
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ===== HELPER: EXTRACT USER =====
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password")); // hashed
        user.setEmail(SecurityUtil.decrypt(rs.getString("email")));
        user.setFirstName(SecurityUtil.decrypt(rs.getString("first_name")));
        user.setLastName(SecurityUtil.decrypt(rs.getString("last_name")));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}
