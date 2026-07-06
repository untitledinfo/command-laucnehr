package launcher.ui;

import javax.swing.JToggleButton;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class SidebarButton extends JToggleButton {

    private float barWidth = 0f;
    private final Timer timer;

    public SidebarButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setHorizontalAlignment(LEFT);
        setForeground(Theme.TEXT_DIM);
        setFont(Theme.FONT_BODY);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new java.awt.Insets(10, 20, 10, 10));

        timer = new Timer(15, e -> {
            float target = isSelected() ? 4f : 0f;
            barWidth += (target - barWidth) * 0.3f;
            setForeground(isSelected() ? Theme.TEXT : Theme.TEXT_DIM);
            repaint();
        });
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!timer.isRunning()) timer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (isSelected()) {
            g2.setColor(Theme.BG_PANEL_LIGHT);
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(0x16, 0x16, 0x1f));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        if (barWidth > 0.2f) {
            g2.setColor(Theme.ACCENT);
            g2.fillRect(0, 0, (int) barWidth, getHeight());
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
