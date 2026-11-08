package gui;

import model.Course;
import model.Lesson;
import model.User;
import service.LessonService;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;

public class AddLessonsDialog extends JDialog {
    private final LessonService service = new LessonService();
    private final User user;
    private final Course course;

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Order", "Title", "Video", "PDF"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTextField titleField = new JTextField(20);
    private final JSpinner orderSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JLabel videoLabel = new JLabel("No file chosen");
    private final JLabel pdfLabel = new JLabel("No file chosen");
    private File video;
    private File pdf;

    public AddLessonsDialog(JFrame owner, User user, Course course) {
        super(owner, "Lessons – " + course.getTitle(), true);
        this.user = user;
        this.course = course;

        JTable table = new JTable(model);
        table.setRowHeight(26);
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(560, 150));

        JButton chooseVideo = new JButton("Choose video…");
        chooseVideo.addActionListener(e -> {
            File f = choose("Videos", "mp4", "mov", "m4v");
            if (f != null) { video = f; videoLabel.setText(f.getName()); }
        });
        JButton choosePdf = new JButton("Choose PDF…");
        choosePdf.addActionListener(e -> {
            File f = choose("PDF documents", "pdf");
            if (f != null) { pdf = f; pdfLabel.setText(f.getName()); }
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        g.gridx = 0; g.gridy = 0; form.add(new JLabel("Lesson title"), g);
        g.gridx = 1; form.add(titleField, g);
        g.gridx = 2; form.add(new JLabel("Order"), g);
        g.gridx = 3; form.add(orderSpinner, g);
        g.gridx = 0; g.gridy = 1; form.add(chooseVideo, g);
        g.gridx = 1; g.gridwidth = 3; form.add(videoLabel, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 1; form.add(choosePdf, g);
        g.gridx = 1; g.gridwidth = 3; form.add(pdfLabel, g);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        JButton add = Theme.primary(new JButton("Add lesson"));
        add.addActionListener(e -> doAdd());
        getRootPane().setDefaultButton(add);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(close);
        footer.add(add);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        content.add(tableScroll, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        setContentPane(content);

        reload();
        pack();
        setLocationRelativeTo(owner);
    }

    private File choose(String label, String... extensions) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(label, extensions));
        return chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile() : null;
    }

    private void doAdd() {
        try {
            service.addLesson(user, course.getCourseId(), titleField.getText(),
                    (Integer) orderSpinner.getValue(), video, pdf);
            titleField.setText("");
            video = null;
            pdf = null;
            videoLabel.setText("No file chosen");
            pdfLabel.setText("No file chosen");
            reload();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check your input",
                    JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reload() {
        try {
            model.setRowCount(0);
            for (Lesson l : service.lessons(course.getCourseId())) {
                model.addRow(new Object[]{
                        l.getLessonOrder(), l.getTitle(),
                        l.getVideoPath() != null ? "✓" : "–",
                        l.getPdfPath() != null ? "✓" : "–"});
            }
            orderSpinner.setValue(service.nextOrder(course.getCourseId()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}