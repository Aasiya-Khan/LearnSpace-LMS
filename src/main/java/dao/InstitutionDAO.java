package dao;

import database.DBConnection;
import model.Institution;
import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InstitutionDAO {

    public void insert(int adminId, String name, String description, String joinCode) throws SQLException {
        String sql = "INSERT INTO Institutions (admin_id, institution_name, description, join_code) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setString(2, name);
            ps.setString(3, description);
            ps.setString(4, joinCode);
            ps.executeUpdate();
        }
    }

    public Institution findByAdmin(int adminId) throws SQLException {
        return findOne("SELECT * FROM Institutions WHERE admin_id = ?", adminId);
    }

    public Institution findByCode(String joinCode) throws SQLException {
        return findOne("SELECT * FROM Institutions WHERE join_code = ?", joinCode);
    }

    public Institution findByMember(int userId, String status) throws SQLException {
        String sql = "SELECT i.* FROM Institutions i "
                + "JOIN Institution_Members m ON m.institution_id = i.institution_id "
                + "WHERE m.user_id = ? AND m.status = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? build(rs) : null;
            }
        }
    }

    public void addMember(int institutionId, int userId, String memberRole, String status) throws SQLException {
        String sql = "INSERT INTO Institution_Members (institution_id, user_id, member_role, status) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, institutionId);
            ps.setInt(2, userId);
            ps.setString(3, memberRole);
            ps.setString(4, status);
            ps.executeUpdate();
        }
    }

    public List<User> findPendingInstructors(int institutionId) throws SQLException {
        String sql = "SELECT u.* FROM Users u "
                + "JOIN Institution_Members m ON m.user_id = u.user_id "
                + "WHERE m.institution_id = ? AND m.status = 'PENDING'";
        List<User> list = new ArrayList<>();
        UserDAO userDAO = new UserDAO();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, institutionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(userDAO.build(rs));
                }
            }
        }
        return list;
    }

    public void setStatus(int institutionId, int userId, String status) throws SQLException {
        String sql = "UPDATE Institution_Members SET status = ? WHERE institution_id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, institutionId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void deleteMember(int institutionId, int userId) throws SQLException {
        String sql = "DELETE FROM Institution_Members WHERE institution_id = ? AND user_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, institutionId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    private Institution findOne(String sql, Object value) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? build(rs) : null;
            }
        }
    }

    private Institution build(ResultSet rs) throws SQLException {
        return new Institution(
                rs.getInt("institution_id"),
                rs.getInt("admin_id"),
                rs.getString("institution_name"),
                rs.getString("description"),
                rs.getString("join_code"));
    }
}