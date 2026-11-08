package dao;

import database.DBConnection;
import model.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    private static final String SELECT =
            "SELECT c.*, u.name AS instructor_name, cat.category_name, "
                    + "(SELECT COUNT(*) FROM Lessons l WHERE l.course_id = c.course_id) AS lesson_count "
                    + "FROM Courses c "
                    + "JOIN Users u ON u.user_id = c.instructor_id "
                    + "LEFT JOIN Categories cat ON cat.category_id = c.category_id ";

    public int insert(int instructorId, Integer institutionId, String title,
                      String description, String visibility) throws SQLException {
        String sql = "INSERT INTO Courses (instructor_id, institution_id, title, description, visibility) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, instructorId);
            if (institutionId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, institutionId);
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, visibility);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    public Course findById(int courseId) throws SQLException {
        List<Course> list = query(SELECT + "WHERE c.course_id = ?", courseId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Course> findByInstructor(int instructorId) throws SQLException {
        return query(SELECT + "WHERE c.instructor_id = ? ORDER BY c.created_at DESC", instructorId);
    }

    public List<Course> findByInstitution(int institutionId) throws SQLException {
        return query(SELECT + "WHERE c.institution_id = ? AND c.visibility = 'INSTITUTION' "
                + "ORDER BY c.title", institutionId);
    }
    public List<Course> findPublic() throws SQLException {
        return query(SELECT + "WHERE c.visibility = 'PUBLIC' ORDER BY c.title", null);
    }

    public List<Course> findEnrolled(int studentId) throws SQLException {
        return query(SELECT + "JOIN Enrollments e ON e.course_id = c.course_id "
                + "WHERE e.student_id = ? ORDER BY e.enrolled_at DESC", studentId);
    }

    private List<Course> query(String sql, Object param) throws SQLException {
        List<Course> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(build(rs));
            }
        }
        return list;
    }

    private Course build(ResultSet rs) throws SQLException {
        int inst = rs.getInt("institution_id");
        Integer institutionId = rs.wasNull() ? null : inst;
        return new Course(
                rs.getInt("course_id"),
                rs.getInt("instructor_id"),
                institutionId,
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("visibility"),
                rs.getString("instructor_name"),
                rs.getString("category_name"),
                rs.getInt("lesson_count"));
    }
}