package gui;

import model.Institution;
import model.Instructor;
import model.User;
import service.InstitutionService;
import javax.swing.*;
import java.awt.*;

public class InstitutionPanel extends JPanel {
    private final InstitutionService service = new InstitutionService();
    private final User user;
    private final BaseDashboard owner;
    private final Runnable onJoined;

    public InstitutionPanel(User user, BaseDashboard owner) {
        this(user, owner, null);
    }

    public InstitutionPanel(User user, BaseDashboard owner, Runnable onJoined) {
        super(new BorderLayout());
        this.user = user;
        this.owner = owner;
        this.onJoined = onJoined;
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        refresh();
    }

    private void refresh() {
        removeAll();
        try {
            Institution active = service.forMember(user);
            Institution pending = service.forPending(user);
            if (active != null) {
                JLabel label = new JLabel("My institution: " + active.getName());
                Theme.heading(label, 18f);
                add(label, BorderLayout.NORTH);
            } else if (pending != null) {
                add(new JLabel("Request sent to " + pending.getName()
                        + " – waiting for the admin to approve."), BorderLayout.NORTH);
            } else {
                add(joinForm(), BorderLayout.NORTH);
            }
        } catch (Exception ex) {
            add(new JLabel("Database error: " + ex.getMessage()), BorderLayout.NORTH);
        }
        revalidate();
        repaint();
    }

    private JPanel joinForm() {
        JTextField codeField = new JTextField(12);
        JButton joinBtn = Theme.primary(new JButton("Join"));

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Join an institution – enter its code:"));
        p.add(codeField);
        p.add(joinBtn);

        joinBtn.addActionListener(e -> {
            try {
                Institution inst = service.join(user, codeField.getText());
                String msg = (user instanceof Instructor)
                        ? "Request sent to " + inst.getName() + ". Wait for the admin to approve."
                        : "You joined " + inst.getName();
                JOptionPane.showMessageDialog(this, msg);
                refresh();
                if (onJoined != null) onJoined.run();
            } catch (Exception ex) {
                owner.showError(ex);
            }
        });
        return p;
    }
}