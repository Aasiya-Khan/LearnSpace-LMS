package service;

import model.Course;
import model.Institution;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EnrollmentServiceTest extends TestBase {

    private final AuthService auth = new AuthService();
    private final InstitutionService institutions = new InstitutionService();
    private final CourseService courses = new CourseService();
    private final EnrollmentService enrollments = new EnrollmentService();
    private User admin;
    private User instructor;
    private User student;
    private Course publicCourse;
    private Course instCourse;

    @BeforeEach
    void setUp() throws Exception {
        admin = auth.register("Admin", "admin@test.com", "secret123", "INSTITUTION_ADMIN");
        instructor = auth.register("Teacher", "teacher@test.com", "secret123", "INSTRUCTOR");
        student = auth.register("Student", "student@test.com", "secret123", "STUDENT");
        Institution inst = institutions.create(admin, "ABC Institute", "");
        institutions.join(instructor, inst.getJoinCode());
        institutions.approve(admin, instructor.getUserId());
        publicCourse = courses.create(instructor, "Python for Everyone", "", false);
        instCourse = courses.create(instructor, "Java Basics", "", true);
    }

    @Test
    @DisplayName("UT-14 Marketplace returns only PUBLIC courses")
    void marketplaceOnlyPublic() throws Exception {
        var list = courses.publicCourses();
        assertEquals(1, list.size());
        assertEquals("Python for Everyone", list.get(0).getTitle());
    }

    @Test
    @DisplayName("UT-15 Enrolling twice in the same course is rejected")
    void noDoubleEnrolment() throws Exception {
        enrollments.enrol(student, publicCourse.getCourseId());
        assertThrows(IllegalArgumentException.class,
                () -> enrollments.enrol(student, publicCourse.getCourseId()));
    }

    @Test
    @DisplayName("FR-14 Enrolled course appears in My learning")
    void enrolledAppears() throws Exception {
        enrollments.enrol(student, publicCourse.getCourseId());
        assertTrue(enrollments.isEnrolled(student, publicCourse.getCourseId()));
        assertEquals(1, enrollments.enrolledCourses(student).size());
    }

    @Test
    @DisplayName("FR-14 Institution courses cannot be enrolled from the marketplace")
    void cannotEnrolInstitutionCourse() {
        assertThrows(IllegalArgumentException.class,
                () -> enrollments.enrol(student, instCourse.getCourseId()));
    }

    @Test
    @DisplayName("FR-14 Only students can enrol")
    void onlyStudents() {
        assertThrows(IllegalArgumentException.class,
                () -> enrollments.enrol(instructor, publicCourse.getCourseId()));
    }
    @Test
    @DisplayName("IT-07 Non-member cannot open an institution course")
    void nonMemberBlocked() throws Exception {
        assertFalse(courses.canView(student, instCourse));
    }

    @Test
    @DisplayName("FR-15 Active member can open an institution course")
    void memberCanView() throws Exception {
        Institution inst = institutions.forAdmin(admin);
        institutions.join(student, inst.getJoinCode());
        assertTrue(courses.canView(student, instCourse));
    }

    @Test
    @DisplayName("FR-15 Public course opens only after enrolling")
    void publicNeedsEnrolment() throws Exception {
        assertFalse(courses.canView(student, publicCourse));
        enrollments.enrol(student, publicCourse.getCourseId());
        assertTrue(courses.canView(student, publicCourse));
    }

    @Test
    @DisplayName("FR-15 Instructor can always open their own course")
    void ownerCanView() throws Exception {
        assertTrue(courses.canView(instructor, instCourse));
        assertTrue(courses.canView(instructor, publicCourse));
    }
}