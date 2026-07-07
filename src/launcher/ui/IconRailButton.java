package launcher.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Compact icon-only button used in the left navigation rail (Home /
 * Instances / Accounts / Settings / Console). Shows a soft rounded
 * highlight when selected/hovered and eases its glow in/out.
 */
public class IconRailButton extends JToggleButton {

    private final String icon;
    private float glow = 0f;
    private final Timer timer;

    public IconRailButton(String icon, String tooltip) {
        this.icon = icon;
        setToolTipText(tooltip);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(46, 46));
        setMaximumSize(new Dimension(46, 46));
        setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));

        timer = new Timer(15, e -> {
            float target = (isSelected() || getModel().isRollover()) ? 1f : 0f;
            glow += (target - glow) * 0.25f;
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
        Theme.antialias(g2);
        int w = getWidth();
        int h = getHeight();

        if (glow > 0.02f) {
            RoundRectangle2D shape = new RoundRectangle2D.Float(3, 3, w - 6, h - 6, 14, 14);
            int alpha = (int) (glow * (isSelected() ? 60 : 30));
            g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(), alpha));
            g2.fill(shape);
            if (isSelected()) {
                g2.setColor(new Color(Theme.ACCENT.getRed(), Theme.ACCENT.getGreen(), Theme.ACCENT.getBlue(),
                        (int) (glow * 180)));
                g2.setStroke(new BasicStroke(1.4f));
                g2.draw(shape);
            }
        }

        g2.setFont(getFont());
        g2.setColor(isSelected() ? Theme.TEXT : Theme.TEXT_DIM);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(icon)) / 2;
        int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(icon, tx, ty);

        g2.dispose();
    }
}
