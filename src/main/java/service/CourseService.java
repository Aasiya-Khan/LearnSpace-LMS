package service;

import dao.EnrollmentDAO;
import dao.CourseDAO;
import model.Course;
import model.Institution;
import model.Instructor;
import model.User;
import java.sql.SQLException;
import java.util.List;

public class CourseService {

    private final CourseDAO dao = new CourseDAO();
    private final InstitutionService institutionService = new InstitutionService();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    public Course create(User instructor, String title, String description,
                         boolean institutionOnly) throws SQLException {
        if (!(instructor instanceof Instructor))
            throw new IllegalArgumentException("Only instructors can create courses");
        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title is required");

        Integer institutionId = null;
        String visibility = "PUBLIC";
        if (institutionOnly) {
            Institution inst = institutionService.forMember(instructor);
            if (inst == null)
                throw new IllegalArgumentException(
                        "You must be an active member of an institution to publish institution-only courses");
            institutionId = inst.getInstitutionId();
            visibility = "INSTITUTION";
        }

        int id = dao.insert(instructor.getUserId(), institutionId, title.trim(),
                description == null ? "" : description.trim(), visibility);
        return dao.findById(id);
    }

    public List<Course> myCourses(User instructor) throws SQLException {
        return dao.findByInstructor(instructor.getUserId());
    }

    public boolean canPublishToInstitution(User instructor) throws SQLException {
        return institutionService.forMember(instructor) != null;
    }
    public List<Course> institutionCourses(User user) throws SQLException {
        Institution inst = institutionService.forMember(user);
        if (inst == null) return List.of();
        return dao.findByInstitution(inst.getInstitutionId());
    }

    public List<Course> publicCourses() throws SQLException {
        return dao.findPublic();
    }
    public boolean canView(User user, Course course) throws SQLException {
        if (course.getInstructorId() == user.getUserId()) return true;
        if ("PUBLIC".equals(course.getVisibility()))
            return enrollmentDAO.exists(user.getUserId(), course.getCourseId());
        Institution inst = institutionService.forMember(user);
        return inst != null && course.getInstitutionId() != null
                && course.getInstitutionId().equals(inst.getInstitutionId());
    }
}