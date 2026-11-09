package service;

import dao.ProgressDAO;
import model.Course;
import model.Lesson;
import model.Student;
import model.User;
import java.sql.SQLException;
import java.util.Set;

public class ProgressService {

    private final ProgressDAO dao = new ProgressDAO();
    private final CourseService courseService = new CourseService();

    public void markComplete(User user, Course course, Lesson lesson) throws SQLException {
        if (!(user instanceof Student))
            throw new IllegalArgumentException("Only students track progress");
        if (lesson.getCourseId() != course.getCourseId())
            throw new IllegalArgumentException("That lesson is not part of this course");
        if (!courseService.canView(user, course))
            throw new IllegalArgumentException("You don't have access to this course");
        dao.markComplete(user.getUserId(), lesson.getLessonId());
    }

    public Set<Integer> completedLessonIds(User user, int courseId) throws SQLException {
        return dao.completedLessonIds(user.getUserId(), courseId);
    }

    public int percent(User user, Course course) throws SQLException {
        return percent(completedLessonIds(user, course.getCourseId()).size(), course.getLessonCount());
    }

    public static int percent(int done, int total) {
        return total == 0 ? 0 : Math.round(done * 100f / total);
    }
}