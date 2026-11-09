package service;

import dao.CourseDAO;
import dao.EnrollmentDAO;
import model.Course;
import model.Student;
import model.User;
import java.sql.SQLException;
import java.util.List;

public class EnrollmentService {

    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final CourseDAO courseDAO = new CourseDAO();

    public void enrol(User user, int courseId) throws SQLException {
        if (!(user instanceof Student))
            throw new IllegalArgumentException("Only students can enrol in courses");
        Course course = courseDAO.findById(courseId);
        if (course == null)
            throw new IllegalArgumentException("Course not found");
        if (!"PUBLIC".equals(course.getVisibility()))
            throw new IllegalArgumentException("Only public courses can be enrolled in from the marketplace");
        if (enrollmentDAO.exists(user.getUserId(), courseId))
            throw new IllegalArgumentException("You are already enrolled in this course");
        enrollmentDAO.insert(user.getUserId(), courseId);
    }

    public boolean isEnrolled(User user, int courseId) throws SQLException {
        return enrollmentDAO.exists(user.getUserId(), courseId);
    }

    public List<Course> enrolledCourses(User user) throws SQLException {
        return courseDAO.findEnrolled(user.getUserId());
    }
}