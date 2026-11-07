package service;

import model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends TestBase {

    private final AuthService auth = new AuthService();

    @Test
    @DisplayName("UT-01 Register a valid user stores a BCrypt hash")
    void registerValidUser() throws Exception {
        User u = auth.register("Test Student", "student@test.com", "secret123", "STUDENT");
        assertNotNull(u);
        assertEquals("STUDENT", u.getRole());
        assertNotEquals("secret123", u.getPasswordHash());
        assertTrue(u.getPasswordHash().startsWith("$2a$"));
    }

    @Test
    @DisplayName("UT-02 Duplicate email is rejected")
    void duplicateEmailRejected() throws Exception {
        auth.register("A", "dup@test.com", "secret123", "STUDENT");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> auth.register("B", "DUP@test.com", "secret123", "STUDENT"));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    @DisplayName("UT-03 Login: correct password returns user, wrong returns null")
    void loginCorrectAndWrong() throws Exception {
        auth.register("Teacher", "login@test.com", "secret123", "INSTRUCTOR");

        User ok = auth.login("login@test.com", "secret123");
        assertNotNull(ok);
        assertEquals("INSTRUCTOR", ok.getRole());

        assertNull(auth.login("login@test.com", "wrong"));
        assertNull(auth.login("nobody@test.com", "secret123"));
    }
}