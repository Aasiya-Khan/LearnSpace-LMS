package service;

import dao.UserDAO;
import model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User register(String name, String email, String password, String role) throws SQLException {
        if (name.isBlank() || email.isBlank() || password.isBlank())
            throw new IllegalArgumentException("All fields are required");
        email = email.trim().toLowerCase();
        if (!email.contains("@"))
            throw new IllegalArgumentException("Invalid email");
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        if (userDAO.findByEmail(email) != null)
            throw new IllegalArgumentException("Email already in use");

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        userDAO.insert(name.trim(), email, hash, role);
        return userDAO.findByEmail(email);
    }

    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash()))
            return null;
        return user;
    }
}