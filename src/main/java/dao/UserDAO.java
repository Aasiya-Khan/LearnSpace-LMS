package dao;

import database.DBConnection;
import model.*;
import java.sql.*;

public class UserDAO {

    public int insert(String name, String email, String hash, String role) throws SQLException {
        String sql = "INSERT INTO Users (name, email, password_hash, role) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.setString(4, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM Users WHERE email = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? build(rs) : null;
            }
        }
    }

    User build(ResultSet rs) throws SQLException {
        int id = rs.getInt("user_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        return switch (rs.getString("role")) {
            case "STUDENT" -> new Student(id, name, email, hash);
            case "INSTRUCTOR" -> new Instructor(id, name, email, hash);
            default -> new InstitutionAdmin(id, name, email, hash);
        };
    }
}