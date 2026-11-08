package service;

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

class LessonServiceTest extends TestBase {

    private final AuthService auth = new AuthService();
    private final CourseService courses = new CourseService();
    private final LessonService lessons = new LessonService();
    private final List<File> cleanup = new ArrayList<>();
    private User instructor;
    private User other;
    private Course course;
    private File pdf;

    @BeforeEach
    void setUp() throws Exception {
        instructor = auth.register("Teacher", "teacher@test.com", "secret123", "INSTRUCTOR");
        other = auth.register("Other", "other@test.com", "secret123", "INSTRUCTOR");
        course = courses.create(instructor, "Java Basics", "", false);
        pdf = File.createTempFile("notes", ".pdf");
        Files.writeString(pdf.toPath(), "%PDF-1.4 test");
        cleanup.add(pdf);
    }

    @AfterEach
    void removeFiles() {
        for (File f : cleanup) f.delete();
    }

    private Lesson add(String title, int order) throws Exception {
        Lesson l = lessons.addLesson(instructor, course.getCourseId(), title, order, null, pdf);
        cleanup.add(new File(l.getPdfPath()));
        return l;
    }

    @Test
    @DisplayName("UT-11 Lessons come back in order 1, 2 even if added 2 first")
    void lessonsOrdered() throws Exception {
        add("Loops", 2);
        add("Variables", 1);
        List<Lesson> list = lessons.lessons(course.getCourseId());
        assertEquals(1, list.get(0).getLessonOrder());
        assertEquals("Variables", list.get(0).getTitle());
        assertEquals(2, list.get(1).getLessonOrder());
    }

    @Test
    @DisplayName("UT-12 PDF is copied into uploads/documents and its path saved")
    void pdfCopied() throws Exception {
        Lesson l = add("Intro", 1);
        assertTrue(l.getPdfPath().startsWith("uploads/documents/"));
        assertTrue(new File(l.getPdfPath()).isFile());
        assertNull(l.getVideoPath());
    }

    @Test
    @DisplayName("FR-10 Next order is one more than the highest")
    void nextOrder() throws Exception {
        assertEquals(1, lessons.nextOrder(course.getCourseId()));
        add("Intro", 1);
        assertEquals(2, lessons.nextOrder(course.getCourseId()));
    }

    @Test
    @DisplayName("FR-10 Duplicate order is rejected")
    void duplicateOrder() throws Exception {
        add("Intro", 1);
        assertThrows(IllegalArgumentException.class, () -> add("Again", 1));
    }

    @Test
    @DisplayName("FR-11 A lesson needs a video or a PDF")
    void needsAFile() {
        assertThrows(IllegalArgumentException.class,
                () -> lessons.addLesson(instructor, course.getCourseId(), "Empty", 1, null, null));
    }

    @Test
    @DisplayName("FR-10 Another instructor cannot add lessons to my course")
    void notOwner() {
        assertThrows(IllegalArgumentException.class,
                () -> lessons.addLesson(other, course.getCourseId(), "Hack", 1, null, pdf));
    }
}