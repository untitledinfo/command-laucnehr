package launcher.ui;

import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public class ShimmerProgressBar extends JProgressBar {

    private float phase = 0f;
    private final Timer timer;

    public ShimmerProgressBar() {
        super();
        setStringPainted(true);
        setForeground(Theme.ACCENT);
        setBackground(Theme.BG_PANEL_LIGHT);
        setBorderPainted(false);
        timer = new Timer(40, e -> {
            phase += 0.01f;
            if (phase > 1f) phase = 0f;
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
        int w = getWidth();
        int h = getHeight();

        RoundRectangle2D track = new RoundRectangle2D.Float(0, 0, w, h, h, h);
        g2.setColor(Theme.BG_PANEL_LIGHT);
        g2.fill(track);

        double pct = getMaximum() > getMinimum()
                ? (getValue() - getMinimum()) / (double) (getMaximum() - getMinimum())
                : 0;
        int fillW = (int) (w * pct);
        if (fillW > 0) {
            float shift = phase * w;
            LinearGradientPaint lgp = new LinearGradientPaint(
                    -w + shift, 0, shift, 0,
                    new float[]{0f, 0.5f, 1f},
                    new java.awt.Color[]{Theme.ACCENT, Theme.ACCENT_2, Theme.ACCENT});
            g2.setPaint(lgp);
            g2.fill(new RoundRectangle2D.Float(0, 0, fillW, h, h, h));
        }

        if (isStringPainted()) {
            g2.setColor(Theme.TEXT);
            g2.setFont(Theme.FONT_SUBTITLE);
            String text = getString() != null ? getString() : (int) (pct * 100) + "%";
            java.awt.FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(text)) / 2;
            int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);
        }
        g2.dispose();
    }
}
