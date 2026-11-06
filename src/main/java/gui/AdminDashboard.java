package gui;

import model.Institution;
import model.User;
import service.InstitutionService;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class AdminDashboard extends BaseDashboard {
    private final InstitutionService service = new InstitutionService();
    private final JPanel body = new JPanel(new BorderLayout());

    public AdminDashboard(User user) {
        super(user, "Institution Admin");
        body.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        body.removeAll();
        try {
            Institution inst = service.forAdmin(user);
            body.add(inst == null ? createForm() : infoPanel(inst), BorderLayout.NORTH);
        } catch (Exception ex) {
            body.add(new JLabel("Database error: " + ex.getMessage()), BorderLayout.NORTH);
        }
        body.revalidate();
        body.repaint();
    }

    private JPanel createForm() {
        JTextField nameField = new JTextField(25);
        JTextField descField = new JTextField(25);
        JButton createBtn = Theme.primary(new JButton("Create institution"));

        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Create your institution"));
        p.add(new JLabel("Name"));
        p.add(nameField);
        p.add(new JLabel("Description (optional)"));
        p.add(descField);
        p.add(createBtn);

        createBtn.addActionListener(e -> {
            try {
                service.create(user, nameField.getText(), descField.getText());
                refresh();
            } catch (Exception ex) {
                showError(ex);
            }
        });
        return p;
    }

    private JPanel infoPanel(Institution inst) throws Exception {
        JTextField codeField = new JTextField(inst.getJoinCode());
        codeField.setEditable(false);
        codeField.setFont(new Font("Monospaced", Font.BOLD, 20));
        codeField.setBackground(Theme.PEACH); codeField.setForeground(Theme.RUST);

        JButton copyBtn = new JButton("Copy code");
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(inst.getJoinCode()), null);
            JOptionPane.showMessageDialog(this, "Join code copied");
        });

        JPanel info = new JPanel(new GridLayout(0, 1, 6, 6));
        info.add(Theme.heading(new JLabel("Institution: " + inst.getName()), 20f));
        info.add(new JLabel("Share this join code with your instructors and students:"));
        info.add(codeField);
        info.add(copyBtn);

        JPanel wrapper = new JPanel(new BorderLayout(0, 20));
        wrapper.add(info, BorderLayout.NORTH);
        wrapper.add(requestsPanel(), BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel requestsPanel() throws Exception {
        JPanel p = new JPanel(new GridLayout(0, 1, 4, 4));
        p.setBorder(BorderFactory.createTitledBorder("Pending instructor requests"));

        List<User> pending = service.pendingInstructors(user);
        if (pending.isEmpty()) {
            p.add(new JLabel("No pending requests"));
        }
        for (User instructor : pending) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row.add(new JLabel(instructor.getName() + " (" + instructor.getEmail() + ")"));

            JButton approveBtn = Theme.approve(new JButton("Approve"));
            JButton rejectBtn = Theme.reject(new JButton("Reject"));
            approveBtn.addActionListener(e -> {
                try {
                    service.approve(user, instructor.getUserId());
                    refresh();
                } catch (Exception ex) {
                    showError(ex);
                }
            });
            rejectBtn.addActionListener(e -> {
                try {
                    service.reject(user, instructor.getUserId());
                    refresh();
                } catch (Exception ex) {
                    showError(ex);
                }
            });
            row.add(approveBtn);
            row.add(rejectBtn);
            p.add(row);
        }
        return p;
    }
}