package launcher;

import launcher.ui.MainFrame;
import launcher.ui.Theme;

import javax.swing.*;
import java.awt.*;

public class LauncherApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LauncherApp::start);
    }

    private static void start() {
        applyGlobalDefaults();

        JWindow splash = new JWindow();
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_DARKEST);
        panel.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 1));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20));

        JLabel label = new JLabel("\u26A1 Command Launcher", SwingConstants.CENTER);
        label.setForeground(Theme.ACCENT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(label);
        center.add(Box.createVerticalStrut(18));

        SpinnerPanel spinner = new SpinnerPanel();
        spinner.setAlignmentX(Component.CENTER_ALIGNMENT);
        spinner.setPreferredSize(new Dimension(36, 36));
        spinner.setMaximumSize(new Dimension(36, 36));
        center.add(spinner);
        center.add(Box.createVerticalStrut(14));

        JLabel sub = new JLabel("Starting up", SwingConstants.CENTER);
        sub.setForeground(Theme.TEXT_DIM);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(sub);

        panel.add(center, BorderLayout.CENTER);
        splash.setContentPane(panel);
        splash.setSize(420, 240);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        // animate "Starting up" -> "Starting up." -> ".." -> "..." on a loop
        Timer dots = new Timer(400, null);
        int[] dotCount = {0};
        dots.addActionListener(e -> {
            dotCount[0] = (dotCount[0] + 1) % 4;
            StringBuilder sb = new StringBuilder("Starting up");
            for (int i = 0; i < dotCount[0]; i++) sb.append('.');
            sub.setText(sb.toString());
        });
        dots.start();

        Timer timer = new Timer(1200, e -> {
            dots.stop();
            spinner.stop();
            splash.dispose();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            fadeIn(frame);
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** Small rotating arc spinner shown on the splash screen while the app boots. */
    private static final class SpinnerPanel extends JPanel {
        private float angle = 0f;
        private final Timer timer;

        SpinnerPanel() {
            setOpaque(false);
            timer = new Timer(16, e -> {
                angle = (angle + 8f) % 360f;
                repaint();
            });
            timer.start();
        }

        void stop() {
            timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int size = Math.min(getWidth(), getHeight()) - 6;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;
            g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Theme.BG_PANEL_LIGHT);
            g2.drawOval(x, y, size, size);
            g2.setColor(Theme.ACCENT);
            g2.drawArc(x, y, size, size, (int) -angle, 100);
            g2.dispose();
        }
    }

    /**
     * Animates a window's opacity from 0 -> 1, but only if the window
     * actually supports per-pixel translucency in its current state.
     * Decorated AWT windows (e.g. a normal JFrame with a title bar, like
     * MainFrame) CANNOT have their opacity set below 1.0f -
     * Window.setOpacity() throws IllegalComponentStateException in that
     * case. Calling this on a decorated frame used to crash the app right
     * after the splash screen closed, so the main window never appeared.
     * We now detect that case and simply skip the fade instead of crashing.
     */
    private static void fadeIn(Window window) {
        boolean decorated = (window instanceof Frame) && !((Frame) window).isUndecorated();
        GraphicsConfiguration gc = window.getGraphicsConfiguration();
        boolean translucencySupported = gc != null
                && gc.getDevice().isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);

        if (decorated || !translucencySupported) {
            window.setOpacity(1f);
            return;
        }

        window.setOpacity(0f);
        final float[] opacity = {0f};
        Timer timer = new Timer(15, null);
        timer.addActionListener(e -> {
            opacity[0] += 0.06f;
            if (opacity[0] >= 1f) {
                opacity[0] = 1f;
                window.setOpacity(1f);
                ((Timer) e.getSource()).stop();
            } else {
                window.setOpacity(opacity[0]);
            }
        });
        timer.start();
    }

    private static void applyGlobalDefaults() {
        UIManager.put("Panel.background", Theme.BG_DARKEST);
        UIManager.put("Label.foreground", Theme.TEXT);
        UIManager.put("OptionPane.background", Theme.BG_PANEL);
        UIManager.put("OptionPane.messageForeground", Theme.TEXT);
        UIManager.put("Panel[OptionPane].background", Theme.BG_PANEL);
        UIManager.put("TextField.background", Theme.BG_PANEL_LIGHT);
        UIManager.put("TextField.foreground", Theme.TEXT);
        UIManager.put("TextField.caretForeground", Theme.TEXT);
        UIManager.put("ComboBox.background", Theme.BG_PANEL_LIGHT);
        UIManager.put("ComboBox.foreground", Theme.TEXT);
        UIManager.put("List.background", Theme.BG_PANEL_LIGHT);
        UIManager.put("List.foreground", Theme.TEXT);
        UIManager.put("ScrollPane.background", Theme.BG_DARK);
        UIManager.put("Viewport.background", Theme.BG_DARK);
        UIManager.put("Button.background", Theme.BG_PANEL_LIGHT);
        UIManager.put("Button.foreground", Theme.TEXT);
        UIManager.put("CheckBox.background", Theme.BG_DARKEST);
        UIManager.put("CheckBox.foreground", Theme.TEXT);
        UIManager.put("SpinnerNumber.background", Theme.BG_PANEL_LIGHT);
    }
}
