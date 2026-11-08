package dao;

import database.DBConnection;
import model.Lesson;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LessonDAO {

    public int insert(int courseId, String title, int order, String videoPath, String pdfPath)
            throws SQLException {
        String sql = "INSERT INTO Lessons (course_id, title, lesson_order, video_path, pdf_path) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, courseId);
            ps.setString(2, title);
            ps.setInt(3, order);
            ps.setString(4, videoPath);   // setString with null stores NULL
            ps.setString(5, pdfPath);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public List<Lesson> findByCourse(int courseId) throws SQLException {
        String sql = "SELECT * FROM Lessons WHERE course_id = ? ORDER BY lesson_order";
        List<Lesson> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Lesson(
                            rs.getInt("lesson_id"),
                            rs.getInt("course_id"),
                            rs.getString("title"),
                            rs.getInt("lesson_order"),
                            rs.getString("video_path"),
                            rs.getString("pdf_path")));
                }
            }
        }
        return list;
    }

    public int nextOrder(int courseId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(lesson_order), 0) + 1 FROM Lessons WHERE course_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}