package service;

import model.Course;
import model.Institution;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest extends TestBase {

    private final AuthService auth = new AuthService();
    private final InstitutionService institutions = new InstitutionService();
    private final CourseService courses = new CourseService();
    private User admin;
    private User instructor;
    private User student;

    @BeforeEach
    void createUsers() throws Exception {
        admin = auth.register("Admin", "admin@test.com", "secret123", "INSTITUTION_ADMIN");
        instructor = auth.register("Teacher", "teacher@test.com", "secret123", "INSTRUCTOR");
        student = auth.register("Student", "student@test.com", "secret123", "STUDENT");
    }

    private Institution approveInstructor() throws Exception {
        Institution inst = institutions.create(admin, "ABC Institute", "");
        institutions.join(instructor, inst.getJoinCode());
        institutions.approve(admin, instructor.getUserId());
        return inst;
    }

    @Test
    @DisplayName("UT-09 Public course is saved with no institution")
    void publicCourse() throws Exception {
        Course c = courses.create(instructor, "Python for Everyone", "Intro", false);
        assertEquals("PUBLIC", c.getVisibility());
        assertNull(c.getInstitutionId());
    }

    @Test
    @DisplayName("UT-10 Non-member cannot create an institution course")
    void nonMemberRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> courses.create(instructor, "Java Basics", "", true));
    }

    @Test
    @DisplayName("FR-09 Pending instructor cannot create an institution course")
    void pendingRejected() throws Exception {
        Institution inst = institutions.create(admin, "ABC Institute", "");
        institutions.join(instructor, inst.getJoinCode());
        assertThrows(IllegalArgumentException.class,
                () -> courses.create(instructor, "Java Basics", "", true));
    }

    @Test
    @DisplayName("FR-08 Approved instructor creates an institution course")
    void activeMemberCreates() throws Exception {
        Institution inst = approveInstructor();
        Course c = courses.create(instructor, "Java Basics", "", true);
        assertEquals("INSTITUTION", c.getVisibility());
        assertEquals(inst.getInstitutionId(), c.getInstitutionId());
    }

    @Test
    @DisplayName("FR-08 A student cannot create a course")
    void studentCannotCreate() {
        assertThrows(IllegalArgumentException.class,
                () -> courses.create(student, "Hack", "", false));
    }

    @Test
    @DisplayName("FR-08 My courses lists only my own courses, newest first")
    void myCoursesOnlyMine() throws Exception {
        courses.create(instructor, "First", "", false);
        courses.create(instructor, "Second", "", false);
        assertEquals(2, courses.myCourses(instructor).size());
        assertEquals(0, courses.myCourses(admin).size());
    }
    @Test
    @DisplayName("UT-13 A student who is not a member sees no institution courses")
    void nonMemberSeesNothing() throws Exception {
        approveInstructor();
        courses.create(instructor, "Java Basics", "", true);
        assertTrue(courses.institutionCourses(student).isEmpty());
    }

    @Test
    @DisplayName("FR-12 An active member sees institution courses but not public ones")
    void memberSeesInstitutionCourses() throws Exception {
        Institution inst = approveInstructor();
        courses.create(instructor, "Java Basics", "", true);
        courses.create(instructor, "Python for Everyone", "", false);
        institutions.join(student, inst.getJoinCode());
        var list = courses.institutionCourses(student);
        assertEquals(1, list.size());
        assertEquals("Java Basics", list.get(0).getTitle());
    }

    @Test
    @DisplayName("FR-12 A member never sees another institution's courses")
    void otherInstitutionHidden() throws Exception {
        approveInstructor();
        courses.create(instructor, "Java Basics", "", true);
        User admin2 = auth.register("Admin2", "admin2@test.com", "secret123", "INSTITUTION_ADMIN");
        Institution other = institutions.create(admin2, "XYZ College", "");
        institutions.join(student, other.getJoinCode());
        assertTrue(courses.institutionCourses(student).isEmpty());
    }

    @Test
    @DisplayName("Course can be saved with a category, or without one")
    void courseCategory() throws Exception {
        java.util.Map<Integer, String> cats = courses.categories();
        assertFalse(cats.isEmpty(), "schema.sql adds starter categories");
        int firstId = cats.keySet().iterator().next();
        Course withCat = courses.create(instructor, "UI Design 101", "", false, firstId);
        assertEquals(cats.get(firstId), withCat.getCategoryName());
        Course without = courses.create(instructor, "Python for Everyone", "", false);
        assertNull(without.getCategoryName());
    }
}
