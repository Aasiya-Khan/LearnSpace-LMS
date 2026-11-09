package dao;

import database.DBConnection;
import java.sql.*;

public class EnrollmentDAO {

    public void insert(int studentId, int courseId) throws SQLException {
        String sql = "INSERT INTO Enrollments (student_id, course_id) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    public boolean exists(int studentId, int courseId) throws SQLException {
        String sql = "SELECT 1 FROM Enrollments WHERE student_id = ? AND course_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}