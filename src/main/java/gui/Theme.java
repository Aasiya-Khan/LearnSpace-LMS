package gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatLineBorder;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Map;

public final class Theme {
    public static final Color RUST   = new Color(0xB5541C);   // main buttons, active tab, logo
    public static final Color PEACH  = new Color(0xFCE6D4);   // selected rows, tags
    public static final Color SAND   = new Color(0xFBF3E6);   // table headers
    public static final Color IVORY  = new Color(0xFFFBF5);   // background
    public static final Color COCOA  = new Color(0x3A2E25);   // text
    public static final Color QUIET  = new Color(0x7A6A5C);   // hints
    public static final Color BORDER = new Color(0xEADFD0);
    public static final Color OLIVE  = new Color(0x4E7A3A);   // approve, complete
    public static final Color BRICK  = new Color(0x9A3B2E);   // reject

    private static Font body = new Font("SansSerif", Font.PLAIN, 14);
    private static Font bodyBold = body.deriveFont(Font.BOLD);
    private static Font heading = new Font("SansSerif", Font.BOLD, 20);

    private Theme() {}

    public static void install() {
        Font karla = load("/fonts/Karla-Regular.ttf");
        Font karlaBold = load("/fonts/Karla-Bold.ttf");
        Font archivo = load("/fonts/Archivo-Bold.ttf");
        if (karla != null) body = karla.deriveFont(Font.PLAIN, 14f);
        bodyBold = karlaBold != null ? karlaBold.deriveFont(Font.PLAIN, 14f) : body.deriveFont(Font.BOLD);
        if (archivo != null) heading = archivo.deriveFont(Font.PLAIN, 20f);

        FlatLaf.setGlobalExtraDefaults(Map.of(
                "@accentColor", "#B5541C",
                "@background", "#FFFBF5",
                "@foreground", "#3A2E25"));
        FlatLightLaf.setup();

        UIManager.put("defaultFont", body);
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Component.borderColor", new Color(0xD9C7B2));
        UIManager.put("TableHeader.background", SAND);
        UIManager.put("TableHeader.foreground", QUIET);
        UIManager.put("Table.selectionBackground", PEACH);
        UIManager.put("Table.selectionForeground", COCOA);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("List.selectionBackground", PEACH);
        UIManager.put("List.selectionForeground", COCOA);
        UIManager.put("TabbedPane.underlineColor", RUST);
        UIManager.put("TabbedPane.selectedForeground", RUST);
        UIManager.put("ProgressBar.foreground", RUST);
        UIManager.put("ProgressBar.background", new Color(0xF3E7D8));
        UIManager.put("ProgressBar.arc", 999);
    }

    private static Font load(String path) {
        try (InputStream in = Theme.class.getResourceAsStream(path)) {
            if (in == null) return null;
            Font f = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f;
        } catch (Exception e) {
            return null;
        }
    }

    // ---- helpers used by the screens ----
    public static JButton primary(JButton b) {
        b.setBackground(RUST); b.setForeground(Color.WHITE); b.setFont(bodyBold); return b;
    }
    public static JButton approve(JButton b) {
        b.setBackground(OLIVE); b.setForeground(Color.WHITE); b.setFont(bodyBold); return b;
    }
    public static JButton reject(JButton b) {
        b.setForeground(BRICK); b.setFont(bodyBold); return b;
    }
    public static JLabel wordmark(JLabel l, float size) {
        l.setFont(heading.deriveFont(size)); l.setForeground(RUST); return l;
    }
    public static JLabel heading(JLabel l, float size) {
        l.setFont(heading.deriveFont(size)); l.setForeground(COCOA); return l;
    }
    public static JLabel quiet(JLabel l) {
        l.setForeground(QUIET); return l;
    }
    public static <T extends JComponent> T card(T c) {
        c.setOpaque(true);
        c.setBackground(Color.WHITE);
        c.setBorder(new FlatLineBorder(new Insets(16, 16, 16, 16), BORDER, 1, 12));
        return c;
    }
}