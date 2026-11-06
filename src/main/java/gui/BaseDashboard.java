package gui;

import model.User;
import javax.swing.*;
import java.awt.*;

public abstract class BaseDashboard extends JFrame {
    protected final User user;

    public BaseDashboard(User user, String roleTitle) {
        this.user = user;
        setTitle("LearnSpace – " + roleTitle);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        // ---- header: wordmark + welcome on the left, Log out on the right ----
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(Theme.wordmark(new JLabel("LearnSpace"), 20f));
        left.add(new JLabel("Welcome, " + user.getName() + "  (" + roleTitle + ")"));
        header.add(left, BorderLayout.WEST);

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        header.add(logout, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);
    }

    protected void showError(Exception ex) {
        if (ex instanceof IllegalArgumentException)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input", JOptionPane.WARNING_MESSAGE);
        else
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}