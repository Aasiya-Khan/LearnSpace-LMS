package gui;

import model.Course;
import model.Lesson;
import model.Student;
import model.User;
import service.LessonService;
import service.ProgressService;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;

// F8 – opens on top of the dashboard; Back closes it
public class CourseDetailsFrame extends JDialog {
    private final LessonService lessonService = new LessonService();
    private final ProgressService progressService = new ProgressService();
    private final User user;
    private final Course course;

    private final DefaultListModel<Lesson> listModel = new DefaultListModel<>();
    private final JList<Lesson> lessonList = new JList<>(listModel);
    private final JLabel lessonTitle = new JLabel("Select a lesson");
    private final JButton playBtn = Theme.primary(new JButton("Play video"));
    private final JButton pdfBtn = new JButton("Open PDF");
    private final JButton completeBtn = Theme.approve(new JButton("Mark complete"));
    private final JProgressBar progressBar = new JProgressBar(0, 100);
    private Set<Integer> completed = Set.of();

    public CourseDetailsFrame(JFrame owner, User user, Course course) {
        super(owner, course.getTitle(), true);
        this.user = user;
        this.course = course;

        // Header
        JButton back = new JButton("← Back");
        back.addActionListener(e -> dispose());
        JLabel title = new JLabel(course.getTitle());
        Theme.heading(title, 30f);
        JLabel meta = new JLabel("by " + course.getInstructorName() + "  ·  "
                + ("PUBLIC".equals(course.getVisibility()) ? "Public" : "Institution"));
        Theme.quiet(meta);
        String d = course.getDescription() == null ? "" : course.getDescription();
        JLabel desc = new JLabel("<html><body style='width:700px'>" + d + "</body></html>");
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{back, title, meta, desc}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            header.add(c);
            header.add(Box.createVerticalStrut(6));
        }

        // Left: lessons, with ✓ when completed
        lessonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lessonList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, focus);
                Lesson l = (Lesson) value;
                String tick = completed.contains(l.getLessonId()) ? "   ✓" : "";
                setText(l.getLessonOrder() + ". " + l.getTitle() + tick);
                setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                return this;
            }
        });
        lessonList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showLesson(lessonList.getSelectedValue());
        });
        JScrollPane left = new JScrollPane(lessonList);
        left.setPreferredSize(new Dimension(300, 400));
        left.setBorder(BorderFactory.createTitledBorder("Lessons"));

        // Right: selected lesson, buttons, progress
        Theme.heading(lessonTitle, 22f);
        playBtn.addActionListener(e -> openFile(lessonList.getSelectedValue().getVideoPath()));
        pdfBtn.addActionListener(e -> openFile(lessonList.getSelectedValue().getPdfPath()));
        completeBtn.addActionListener(e -> markComplete());
        completeBtn.setVisible(user instanceof Student);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.add(playBtn);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(pdfBtn);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(completeBtn);

        JLabel note = new JLabel("Video and PDF open in your computer's default player and viewer.");
        Theme.quiet(note);
        progressBar.setStringPainted(true);
        progressBar.setVisible(user instanceof Student);
        progressBar.setMaximumSize(new Dimension(500, 24));

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 8));
        for (JComponent c : new JComponent[]{lessonTitle, buttons, note, progressBar}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            right.add(c);
            right.add(Box.createVerticalStrut(16));
        }

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        content.add(header, BorderLayout.NORTH);
        content.add(left, BorderLayout.WEST);
        content.add(right, BorderLayout.CENTER);
        setContentPane(content);

        loadLessons();
        setSize(1000, 650);
        setLocationRelativeTo(owner);
    }

    private void loadLessons() {
        try {
            List<Lesson> lessons = lessonService.lessons(course.getCourseId());
            for (Lesson l : lessons) listModel.addElement(l);
            refreshProgress();
            if (lessons.isEmpty()) {
                lessonTitle.setText("This course has no lessons yet");
                playBtn.setEnabled(false);
                pdfBtn.setEnabled(false);
                completeBtn.setEnabled(false);
            } else {
                lessonList.setSelectedIndex(0);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void refreshProgress() throws Exception {
        if (!(user instanceof Student)) return;
        completed = progressService.completedLessonIds(user, course.getCourseId());
        int total = listModel.size();
        int pct = ProgressService.percent(completed.size(), total);
        progressBar.setValue(pct);
        progressBar.setString(completed.size() + " of " + total + " lessons – " + pct + "%");
        lessonList.repaint();
    }

    private void showLesson(Lesson l) {
        if (l == null) return;
        lessonTitle.setText(l.getLessonOrder() + ". " + l.getTitle());
        playBtn.setEnabled(l.getVideoPath() != null);
        pdfBtn.setEnabled(l.getPdfPath() != null);
        boolean done = completed.contains(l.getLessonId());
        completeBtn.setText(done ? "✓ Completed" : "Mark complete");
        completeBtn.setEnabled(!done);
    }

    private void markComplete() {
        Lesson l = lessonList.getSelectedValue();
        if (l == null) return;
        try {
            progressService.markComplete(user, course, l);
            refreshProgress();
            showLesson(l);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not save progress",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openFile(String path) {
        try {
            File f = database.AppPaths.resolve(path);
            if (!f.isFile())
                throw new IllegalArgumentException("File not found: " + path);
            if (!Desktop.isDesktopSupported())
                throw new IllegalStateException("Opening files isn't supported on this computer");
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not open file",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}