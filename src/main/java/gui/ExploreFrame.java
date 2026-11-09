package gui;

import model.Course;
import model.User;
import service.CourseService;
import service.EnrollmentService;
import javax.swing.*;
import java.awt.*;
import java.util.List;

// F7 – opens as a window on top of the student dashboard; Back closes it
public class ExploreFrame extends JDialog {
    private final CourseService courseService = new CourseService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final User user;
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 16, 16));

    public ExploreFrame(JFrame owner, User user) {
        super(owner, "Explore courses", true);
        this.user = user;

        JButton back = new JButton("← Back");
        back.addActionListener(e -> dispose());
        JLabel title = new JLabel("Explore courses");
        Theme.heading(title, 26f);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        top.add(back);
        top.add(title);

        JPanel gridWrap = new JPanel(new BorderLayout());
        gridWrap.add(grid, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(gridWrap);
        scroll.setBorder(null);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        content.add(top, BorderLayout.NORTH);
        content.add(scroll, BorderLayout.CENTER);
        setContentPane(content);

        reload();
        setSize(1000, 650);
        setLocationRelativeTo(owner);
    }

    private void reload() {
        grid.removeAll();
        try {
            List<Course> courses = courseService.publicCourses();
            if (courses.isEmpty()) grid.add(new JLabel("No courses found"));
            for (Course c : courses) grid.add(card(c));
        } catch (Exception ex) {
            grid.add(new JLabel("Error: " + ex.getMessage()));
        }
        grid.revalidate();
        grid.repaint();
    }

    private JPanel card(Course c) throws Exception {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        Theme.card(card);

        JLabel name = new JLabel(c.getTitle());
        name.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel meta = new JLabel("by " + c.getInstructorName() + " · " + c.getLessonCount() + " lessons");
        Theme.quiet(meta);
        String d = c.getDescription() == null ? "" : c.getDescription();
        JLabel desc = new JLabel("<html><body style='width:220px'>" + d + "</body></html>");

        JButton action = new JButton();
        if (enrollmentService.isEnrolled(user, c.getCourseId())) {
            action.setText("✓ Enrolled");
            action.setEnabled(false);
        } else {
            action.setText("Enrol");
            Theme.primary(action);
            action.addActionListener(e -> {
                try {
                    enrollmentService.enrol(user, c.getCourseId());
                    JOptionPane.showMessageDialog(this, "Enrolled in " + c.getTitle());
                    reload();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not enrol",
                            JOptionPane.WARNING_MESSAGE);
                }
            });
        }

        for (JComponent comp : new JComponent[]{name, meta, desc, action}) {
            comp.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(comp);
            card.add(Box.createVerticalStrut(6));
        }
        return card;
    }
}