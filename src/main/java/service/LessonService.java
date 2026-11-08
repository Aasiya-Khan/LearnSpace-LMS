package service;

import dao.CourseDAO;
import dao.LessonDAO;
import model.Course;
import model.Lesson;
import model.User;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class LessonService {

    private final LessonDAO lessonDAO = new LessonDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final FileStorageService files = new FileStorageService();

    public Lesson addLesson(User instructor, int courseId, String title, int order,
                            File video, File pdf) throws SQLException, IOException {
        Course course = courseDAO.findById(courseId);
        if (course == null)
            throw new IllegalArgumentException("Course not found");
        if (course.getInstructorId() != instructor.getUserId())
            throw new IllegalArgumentException("You can only add lessons to your own courses");
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Lesson title is required");
        if (video == null && pdf == null)
            throw new IllegalArgumentException("Choose a video or a PDF");
        if (order < 1)
            throw new IllegalArgumentException("Order must be 1 or more");
        for (Lesson l : lessonDAO.findByCourse(courseId))
            if (l.getLessonOrder() == order)
                throw new IllegalArgumentException("Lesson order " + order + " is already used");

        String videoPath = files.copy(video, FileStorageService.VIDEOS);
        String pdfPath = files.copy(pdf, FileStorageService.DOCUMENTS);
        int id = lessonDAO.insert(courseId, title.trim(), order, videoPath, pdfPath);
        return new Lesson(id, courseId, title.trim(), order, videoPath, pdfPath);
    }

    public List<Lesson> lessons(int courseId) throws SQLException {
        return lessonDAO.findByCourse(courseId);
    }

    public int nextOrder(int courseId) throws SQLException {
        return lessonDAO.nextOrder(courseId);
    }
}