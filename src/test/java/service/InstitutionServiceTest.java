package service;

import model.Institution;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InstitutionServiceTest extends TestBase {

    private final AuthService auth = new AuthService();
    private final InstitutionService service = new InstitutionService();
    private User admin;
    private User instructor;
    private User student;

    @BeforeEach
    void createUsers() throws Exception {
        admin = auth.register("Admin", "admin@test.com", "secret123", "INSTITUTION_ADMIN");
        instructor = auth.register("Teacher", "teacher@test.com", "secret123", "INSTRUCTOR");
        student = auth.register("Student", "student@test.com", "secret123", "STUDENT");
    }

    @Test
    @DisplayName("UT-04 A student cannot create an institution")
    void studentCannotCreate() {
        assertThrows(IllegalArgumentException.class,
                () -> service.create(student, "Fake School", ""));
    }

    @Test
    @DisplayName("UT-05 100 join codes are 8 valid characters and all unique")
    void joinCodesUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            String code = service.generateCode();
            assertEquals(8, code.length());
            assertTrue(code.matches("[A-HJ-NP-Z2-9]{8}"));
            codes.add(code);
        }
        assertEquals(100, codes.size());
    }

    @Test
    @DisplayName("UT-06 Instructor join request is saved as PENDING")
    void instructorJoinIsPending() throws Exception {
        Institution inst = service.create(admin, "ABC Institute", "");
        service.join(instructor, inst.getJoinCode());
        assertNotNull(service.forPending(instructor));
        assertNull(service.forMember(instructor));
    }

    @Test
    @DisplayName("UT-07 Admin approval makes the instructor ACTIVE")
    void approveMakesActive() throws Exception {
        Institution inst = service.create(admin, "ABC Institute", "");
        service.join(instructor, inst.getJoinCode());
        service.approve(admin, instructor.getUserId());
        assertNotNull(service.forMember(instructor));
        assertNull(service.forPending(instructor));
    }

    @Test
    @DisplayName("UT-08 Invalid code is rejected and saves nothing")
    void invalidCodeRejected() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> service.join(student, "WRONG123"));
        assertNull(service.forMember(student));
        assertNull(service.forPending(student));
    }

    @Test
    @DisplayName("FR-07 Student with a valid lowercase code joins as ACTIVE")
    void studentJoinsActive() throws Exception {
        Institution inst = service.create(admin, "ABC Institute", "");
        service.join(student, inst.getJoinCode().toLowerCase());
        assertNotNull(service.forMember(student));
    }

    @Test
    @DisplayName("FR-06 Rejecting removes the request so the instructor can retry")
    void rejectRemovesRequest() throws Exception {
        Institution inst = service.create(admin, "ABC Institute", "");
        service.join(instructor, inst.getJoinCode());
        service.reject(admin, instructor.getUserId());
        assertNull(service.forPending(instructor));
        assertNull(service.forMember(instructor));
    }
}