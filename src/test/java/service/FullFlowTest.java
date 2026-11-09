package service;

import dao.CourseDAO;
import model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import database.DBConnection;
import static org.junit.jupiter.api.Assertions.*;

class FullFlowTest extends TestBase {

    @Test
    @DisplayName("IT-ALL Full LearnSpace story: institution → course → enrol → learn → delete")
    void fullStory() throws Exception {
        AuthService auth = new AuthService();
        InstitutionService institutions = new InstitutionService();
        CourseService courses = new CourseService();
        LessonService lessons = new LessonService();
        EnrollmentService enrollments = new EnrollmentService();
        ProgressService progress = new ProgressService();

        // IT-01: three roles register and log in
        auth.register("Ali", "ali@abc.edu", "secret123", "INSTITUTION_ADMIN");
        auth.register("Ayesha", "ayesha@abc.edu", "secret123", "INSTRUCTOR");
        auth.register("Sara", "sara@abc.edu", "secret123", "STUDENT");
        User admin = auth.login("ali@abc.edu", "secret123");
        User teacher = auth.login("ayesha@abc.edu", "secret123");
        User student = auth.login("sara@abc.edu", "secret123");
        assertTrue(admin instanceof InstitutionAdmin);
        assertTrue(teacher instanceof Instructor);
        assertTrue(student instanceof Student);

        // IT-02: institution + approval
        Institution abc = institutions.create(admin, "ABC Institute", "");
        institutions.join(teacher, abc.getJoinCode());
        assertNotNull(institutions.forPending(teacher));
        institutions.approve(admin, teacher.getUserId());
        assertNotNull(institutions.forMember(teacher));

        // IT-03: student joins
        institutions.join(student, abc.getJoinCode().toLowerCase());
        assertNotNull(institutions.forMember(student));

        // IT-04: courses + lessons with a file
        Course java = courses.create(teacher, "Java Basics", "", true);
        Course python = courses.create(teacher, "Python for Everyone", "", false);
        File pdf = File.createTempFile("notes", ".pdf");
        Files.writeString(pdf.toPath(), "%PDF-1.4 test");
        Lesson l1 = lessons.addLesson(teacher, java.getCourseId(), "Intro", 1, null, pdf);
        Lesson l2 = lessons.addLesson(teacher, java.getCourseId(), "Loops", 2, null, pdf);
        assertEquals(1, courses.institutionCourses(student).size());

        // IT-05: marketplace + enrol
        assertEquals(1, courses.publicCourses().size());
        enrollments.enrol(student, python.getCourseId());
        assertEquals(1, enrollments.enrolledCourses(student).size());

        // IT-06: open + progress
        Course javaReloaded = new CourseDAO().findById(java.getCourseId());
        assertTrue(courses.canView(student, javaReloaded));
        progress.markComplete(student, javaReloaded, l1);
        assertEquals(50, progress.percent(student, javaReloaded));

        // IT-07: outsider is blocked
        User outsider = auth.register("Omar", "omar@xyz.edu", "secret123", "STUDENT");
        assertFalse(courses.canView(outsider, javaReloaded));
        assertTrue(courses.institutionCourses(outsider).isEmpty());

        // IT-08: deleting a course cascades to its lessons and progress
        try (Connection c = DBConnection.getConnection();
             PreparedStatement del = c.prepareStatement("DELETE FROM Courses WHERE course_id = ?")) {
            del.setInt(1, java.getCourseId());
            del.executeUpdate();
        }
        assertEquals(0, lessons.lessons(java.getCourseId()).size());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement q = c.prepareStatement("SELECT COUNT(*) FROM Lesson_Progress");
             ResultSet rs = q.executeQuery()) {
            rs.next();
            assertEquals(0, rs.getInt(1));
        }

        // tidy up copied files
        pdf.delete();
        new File(l1.getPdfPath()).delete();
        new File(l2.getPdfPath()).delete();
    }
}