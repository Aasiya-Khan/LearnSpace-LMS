package gui;

import model.Course;
import model.User;
import service.CourseService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InstructorDashboard extends BaseDashboard {
    private final CourseService courseService = new CourseService();
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultTableModel courseModel = new DefaultTableModel(
            new String[]{"Title", "Visibility", "Category", "Lessons"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable courseTable = new JTable(courseModel);
    private List<Course> courses = List.of();

    private final JTextField titleField = new JTextField(30);
    private final JTextArea descArea = new JTextArea(4, 30);
    private final JRadioButton publicRadio = new JRadioButton("Public – anyone can enrol", true);
    private final JRadioButton institutionRadio = new JRadioButton("My institution only");
    private final JLabel hint = new JLabel(" ");

    public InstructorDashboard(User user) {
        super(user, "Instructor");
        JPanel body = new JPanel(new BorderLayout());
        body.add(new InstitutionPanel(user, this), BorderLayout.NORTH);
        tabs.addTab("My courses", buildMyCourses());
        tabs.addTab("Create course", buildCreateCourse());
        tabs.addChangeListener(e -> refresh());
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildMyCourses() {
        courseTable.setRowHeight(28);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addLessons = Theme.primary(new JButton("Add lessons"));
        addLessons.addActionListener(e -> {
            Course c = selectedCourse();
            if (c == null) {
                JOptionPane.showMessageDialog(this, "Select a course first");
                return;
            }
            new AddLessonsDialog(this, user, c).setVisible(true);
            refresh();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(addLessons);

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        p.add(new JScrollPane(courseTable), BorderLayout.CENTER);
        p.add(buttons, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCreateCourse() {
        ButtonGroup group = new ButtonGroup();
        group.add(publicRadio);
        group.add(institutionRadio);

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setMaximumSize(new Dimension(500, 100));
        titleField.setMaximumSize(new Dimension(500, 32));
        Theme.quiet(hint);

        JButton create = Theme.primary(new JButton("Create course"));
        create.addActionListener(e -> doCreate());

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JComponent[] parts = {new JLabel("Title"), titleField, new JLabel("Description"), descScroll,
                new JLabel("Visibility"), publicRadio, institutionRadio, hint, create};
        for (JComponent c : parts) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            form.add(c);
            form.add(Box.createVerticalStrut(6));
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(form, BorderLayout.NORTH);
        return wrapper;
    }

    private void doCreate() {
        try {
            courseService.create(user, titleField.getText(), descArea.getText(),
                    institutionRadio.isSelected());
            JOptionPane.showMessageDialog(this, "Course created");
            titleField.setText("");
            descArea.setText("");
            publicRadio.setSelected(true);
            tabs.setSelectedIndex(0);
            refresh();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private Course selectedCourse() {
        int row = courseTable.getSelectedRow();
        return row < 0 ? null : courses.get(row);
    }

    private void refresh() {
        try {
            courses = courseService.myCourses(user);
            courseModel.setRowCount(0);
            for (Course c : courses) {
                courseModel.addRow(new Object[]{
                        c.getTitle(),
                        "PUBLIC".equals(c.getVisibility()) ? "Public" : "Institution",
                        c.getCategoryName() == null ? "—" : c.getCategoryName(),
                        c.getLessonCount()});
            }
            boolean canInstitution = courseService.canPublishToInstitution(user);
            institutionRadio.setEnabled(canInstitution);
            if (!canInstitution) publicRadio.setSelected(true);
            hint.setText(canInstitution ? " "
                    : "Join an institution (and be approved) to publish institution-only courses.");
        } catch (Exception ex) {
            showError(ex);
        }
    }
}