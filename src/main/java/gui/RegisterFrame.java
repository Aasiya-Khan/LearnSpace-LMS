package gui;

import service.AuthService;
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private final AuthService auth = new AuthService();
    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JComboBox<String> roleBox =
            new JComboBox<>(new String[]{"Student", "Instructor", "Institution Admin"});

    public RegisterFrame() {
        setTitle("LearnSpace – Register");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        Theme.card(form);
        form.add(new JLabel("Full name"));
        form.add(nameField);
        form.add(new JLabel("Email"));
        form.add(emailField);
        form.add(new JLabel("Password (min 6 characters)"));
        form.add(passwordField);
        form.add(new JLabel("I am a"));
        form.add(roleBox);

        JButton registerBtn = Theme.primary(new JButton("Register"));
        JButton backBtn = new JButton("Back to login");
        form.add(registerBtn);
        form.add(backBtn);

        registerBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> backToLogin());
        getRootPane().setDefaultButton(registerBtn);

        add(form);
        pack();
        setLocationRelativeTo(null);
    }

    private String selectedRole() {
        return switch (roleBox.getSelectedIndex()) {
            case 0 -> "STUDENT";
            case 1 -> "INSTRUCTOR";
            default -> "INSTITUTION_ADMIN";
        };
    }

    private void doRegister() {
        try {
            auth.register(nameField.getText(), emailField.getText(),
                    new String(passwordField.getPassword()), selectedRole());
            JOptionPane.showMessageDialog(this, "Account created. Please log in.");
            backToLogin();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Check your details", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToLogin() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}