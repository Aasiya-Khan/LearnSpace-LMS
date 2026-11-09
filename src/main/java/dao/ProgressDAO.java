package dao;

import database.DBConnection;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ProgressDAO {

    public void markComplete(int studentId, int lessonId) throws SQLException {
        String sql = "INSERT INTO Lesson_Progress (student_id, lesson_id, completed, completed_at) "
                + "VALUES (?, ?, TRUE, NOW()) "
                + "ON DUPLICATE KEY UPDATE completed = TRUE, completed_at = NOW()";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, lessonId);
            ps.executeUpdate();
        }
    }

    public Set<Integer> completedLessonIds(int studentId, int courseId) throws SQLException {
        String sql = "SELECT lp.lesson_id FROM Lesson_Progress lp "
                + "JOIN Lessons l ON l.lesson_id = lp.lesson_id "
                + "WHERE lp.student_id = ? AND l.course_id = ? AND lp.completed = TRUE";
        Set<Integer> ids = new HashSet<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }
}