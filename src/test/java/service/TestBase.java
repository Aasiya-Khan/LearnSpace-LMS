package service;

import database.DBConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import java.sql.Connection;
import java.sql.Statement;

public abstract class TestBase {

    @BeforeAll
    static void useTestDatabase() {
        System.setProperty("db.url",
                "jdbc:mysql://localhost:3306/LearnSpace_test?allowPublicKeyRetrieval=true&useSSL=false");
    }

    @BeforeEach
    void cleanDatabase() throws Exception {
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement()) {
            if (!c.getCatalog().equalsIgnoreCase("LearnSpace_test"))
                throw new IllegalStateException("Refusing to wipe database: " + c.getCatalog());

            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            String[] tables = {"Lesson_Progress", "Enrollments", "Lessons", "Courses",
                    "Announcements", "Institution_Members", "Institutions", "Users"};
            for (String t : tables) {
                st.execute("TRUNCATE TABLE " + t);
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}