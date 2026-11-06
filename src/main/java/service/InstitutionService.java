package service;

import dao.InstitutionDAO;
import model.Institution;
import model.InstitutionAdmin;
import model.Instructor;
import model.User;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;

public class InstitutionService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final InstitutionDAO dao = new InstitutionDAO();
    private final SecureRandom random = new SecureRandom();

    public Institution create(User admin, String name, String description) throws SQLException {
        if (!(admin instanceof InstitutionAdmin))
            throw new IllegalArgumentException("Only institution admins can create an institution");
        if (name.isBlank())
            throw new IllegalArgumentException("Institution name is required");
        if (dao.findByAdmin(admin.getUserId()) != null)
            throw new IllegalArgumentException("You already manage an institution");

        String code;
        do {
            code = generateCode();
        } while (dao.findByCode(code) != null);

        dao.insert(admin.getUserId(), name.trim(), description.trim(), code);
        return dao.findByAdmin(admin.getUserId());
    }

    public Institution join(User user, String code) throws SQLException {
        if (user instanceof InstitutionAdmin)
            throw new IllegalArgumentException("Admins create institutions; they don't join them");
        if (code.isBlank())
            throw new IllegalArgumentException("Enter a join code");

        Institution inst = dao.findByCode(code.trim().toUpperCase());
        if (inst == null)
            throw new IllegalArgumentException("No institution has that code");
        if (forMember(user) != null)
            throw new IllegalArgumentException("You already belong to an institution");
        if (forPending(user) != null)
            throw new IllegalArgumentException("You already have a request waiting for approval");

        String status = (user instanceof Instructor) ? "PENDING" : "ACTIVE";
        dao.addMember(inst.getInstitutionId(), user.getUserId(), user.getRole(), status);
        return inst;
    }

    public List<User> pendingInstructors(User admin) throws SQLException {
        return dao.findPendingInstructors(ownInstitution(admin).getInstitutionId());
    }

    public void approve(User admin, int instructorId) throws SQLException {
        dao.setStatus(ownInstitution(admin).getInstitutionId(), instructorId, "ACTIVE");
    }

    public void reject(User admin, int instructorId) throws SQLException {
        dao.deleteMember(ownInstitution(admin).getInstitutionId(), instructorId);
    }

    public Institution forAdmin(User admin) throws SQLException {
        return dao.findByAdmin(admin.getUserId());
    }

    public Institution forMember(User user) throws SQLException {
        return dao.findByMember(user.getUserId(), "ACTIVE");
    }

    public Institution forPending(User user) throws SQLException {
        return dao.findByMember(user.getUserId(), "PENDING");
    }

    private Institution ownInstitution(User admin) throws SQLException {
        if (!(admin instanceof InstitutionAdmin))
            throw new IllegalArgumentException("Only institution admins can manage requests");
        Institution inst = dao.findByAdmin(admin.getUserId());
        if (inst == null)
            throw new IllegalArgumentException("Create your institution first");
        return inst;
    }

    String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++)
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        return sb.toString();
    }
}