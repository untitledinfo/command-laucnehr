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
        JLabel label = new JLabel("Command Launcher", SwingConstants.CENTER);
        label.setForeground(Theme.ACCENT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel sub = new JLabel("Starting up...", SwingConstants.CENTER);
        sub.setForeground(Theme.TEXT_DIM);
        panel.add(label, BorderLayout.CENTER);
        panel.add(sub, BorderLayout.SOUTH);
        splash.setContentPane(panel);
        splash.setSize(420, 220);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        Timer timer = new Timer(1200, e -> {
            splash.dispose();
            MainFrame frame = new MainFrame();
            frame.setOpacity(0f);
            frame.setVisible(true);
            fadeIn(frame);
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static void fadeIn(Window window) {
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
