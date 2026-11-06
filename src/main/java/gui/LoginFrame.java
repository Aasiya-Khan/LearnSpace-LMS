package gui;

import model.User;
import service.AuthService;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);

    public LoginFrame() {
        setTitle("LearnSpace – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        Theme.card(form);

        JLabel title = new JLabel("LearnSpace", SwingConstants.CENTER);
        Theme.wordmark(title, 30f);
        form.add(title);
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password"));
        form.add(passwordField);

        JButton loginBtn = Theme.primary(new JButton("Log in"));
        JButton registerBtn = new JButton("Create an account");
        form.add(loginBtn);
        form.add(registerBtn);

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });
        getRootPane().setDefaultButton(loginBtn);

        add(form);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        try {
            User user = auth.login(email, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Wrong email or password",
                        "Login failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            openDashboard(user);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDashboard(User user) {
        JFrame dashboard = switch (user.getRole()) {
            case "STUDENT" -> new StudentDashboard(user);
            case "INSTRUCTOR" -> new InstructorDashboard(user);
            default -> new AdminDashboard(user);
        };
        dashboard.setVisible(true);
    }
}