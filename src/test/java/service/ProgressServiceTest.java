package service;

import dao.CourseDAO;
import model.Course;
import model.Lesson;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProgressServiceTest extends TestBase {

    private final AuthService auth = new AuthService();
    private final CourseService courses = new CourseService();
    private final LessonService lessons = new LessonService();
    private final EnrollmentService enrollments = new EnrollmentService();
    private final ProgressService progress = new ProgressService();
    private final List<File> cleanup = new ArrayList<>();
    private User instructor;
    private User student;
    private Course course;
    private List<Lesson> lessonList;

    @BeforeEach
    void setUp() throws Exception {
        instructor = auth.register("Teacher", "teacher@test.com", "secret123", "INSTRUCTOR");
        student = auth.register("Student", "student@test.com", "secret123", "STUDENT");
        Course c = courses.create(instructor, "Python for Everyone", "", false);
        File pdf = File.createTempFile("notes", ".pdf");
        Files.writeString(pdf.toPath(), "%PDF-1.4 test");
        cleanup.add(pdf);
        for (int i = 1; i <= 4; i++) {
            Lesson l = lessons.addLesson(instructor, c.getCourseId(), "Lesson " + i, i, null, pdf);
            cleanup.add(new File(l.getPdfPath()));
        }
        course = new CourseDAO().findById(c.getCourseId());   // reload so lessonCount = 4
        lessonList = lessons.lessons(course.getCourseId());
    }

    @AfterEach
    void removeFiles() {
        for (File f : cleanup) f.delete();
    }

    @Test
    @DisplayName("UT-16 Two of four lessons complete gives 50%")
    void halfway() throws Exception {
        enrollments.enrol(student, course.getCourseId());
        progress.markComplete(student, course, lessonList.get(0));
        progress.markComplete(student, course, lessonList.get(1));
        assertEquals(50, progress.percent(student, course));
        assertEquals(50, ProgressService.percent(2, 4));
    }

    @Test
    @DisplayName("FR-16 Marking the same lesson twice counts once")
    void noDoubleCount() throws Exception {
        enrollments.enrol(student, course.getCourseId());
        progress.markComplete(student, course, lessonList.get(0));
        progress.markComplete(student, course, lessonList.get(0));
        assertEquals(25, progress.percent(student, course));
    }

    @Test
    @DisplayName("FR-16 A student who is not enrolled cannot record progress")
    void notEnrolledBlocked() {
        assertThrows(IllegalArgumentException.class,
                () -> progress.markComplete(student, course, lessonList.get(0)));
    }

    @Test
    @DisplayName("FR-16 A course with no lessons is 0%, not an error")
    void zeroLessons() {
        assertEquals(0, ProgressService.percent(0, 0));
    }
}