package gui;

import model.Course;
import model.User;
import service.CourseService;
import service.EnrollmentService;
import service.ProgressService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class StudentDashboard extends BaseDashboard {
    private final CourseService courseService = new CourseService();
    private final EnrollmentService enrollmentService = new EnrollmentService();
    private final ProgressService progressService = new ProgressService();
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultTableModel instModel = readOnlyModel();
    private final JTable instTable = new JTable(instModel);
    private final JLabel instEmpty = new JLabel(" ");
    private List<Course> instCourses = List.of();

    private final DefaultTableModel learnModel = readOnlyModel();
    private final JTable learnTable = new JTable(learnModel);
    private final JLabel learnEmpty = new JLabel(" ");
    private List<Course> myCourses = List.of();

    public StudentDashboard(User user) {
        super(user, "Student");

        JButton explore = Theme.primary(new JButton("Explore marketplace"));
        explore.addActionListener(e -> {
            new ExploreFrame(this, user).setVisible(true);
            refresh();
            tabs.setSelectedIndex(1);
        });
        JPanel exploreBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 20));
        exploreBox.add(explore);

        JPanel top = new JPanel(new BorderLayout());
        top.add(new InstitutionPanel(user, this, this::refresh), BorderLayout.CENTER);
        top.add(exploreBox, BorderLayout.EAST);

        tabs.addTab("My institution courses",
                tablePanel(instTable, instEmpty, () -> selected(instTable, instCourses)));
        tabs.addTab("My learning",
                tablePanel(learnTable, learnEmpty, () -> selected(learnTable, myCourses)));
        tabs.addChangeListener(e -> refresh());

        JPanel body = new JPanel(new BorderLayout());
        body.add(top, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private static DefaultTableModel readOnlyModel() {
        return new DefaultTableModel(new String[]{"Title", "Instructor", "Lessons", "Progress"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // Draws the Progress column as a bar instead of a number
    private static class ProgressRenderer extends JProgressBar implements TableCellRenderer {
        ProgressRenderer() { super(0, 100); setStringPainted(true); }
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                       boolean focus, int row, int col) {
            int pct = value == null ? 0 : (Integer) value;
            setValue(pct);
            setString(pct + "%");
            return this;
        }
    }

    private Course selected(JTable table, List<Course> list) {
        int row = table.getSelectedRow();
        return row < 0 ? null : list.get(row);
    }

    private JPanel tablePanel(JTable table, JLabel empty, Supplier<Course> pick) {
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(3).setCellRenderer(new ProgressRenderer());
        Theme.quiet(empty);

        JButton open = Theme.primary(new JButton("Open"));
        open.addActionListener(e -> {
            Course c = pick.get();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course first");
                return;
            }
            try {
                if (!courseService.canView(user, c)) {
                    JOptionPane.showMessageDialog(this, "You don't have access to this course",
                            "Access denied", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                new CourseDetailsFrame(this, user, c).setVisible(true);
                refresh();
            } catch (Exception ex) {
                showError(ex);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        south.add(open);
        south.add(Box.createHorizontalStrut(16));
        south.add(empty);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void fill(DefaultTableModel model, List<Course> list) throws Exception {
        model.setRowCount(0);
        for (Course c : list) {
            model.addRow(new Object[]{c.getTitle(), c.getInstructorName(), c.getLessonCount(),
                    progressService.percent(user, c)});
        }
    }

    private void refresh() {
        try {
            instCourses = courseService.institutionCourses(user);
            fill(instModel, instCourses);
            instEmpty.setText(instCourses.isEmpty()
                    ? "Join an institution to see its courses (or it has none yet)." : " ");

            myCourses = enrollmentService.enrolledCourses(user);
            fill(learnModel, myCourses);
            learnEmpty.setText(myCourses.isEmpty()
                    ? "You haven't enrolled in any courses yet – explore the marketplace." : " ");
        } catch (Exception ex) {
            showError(ex);
        }
    }
}