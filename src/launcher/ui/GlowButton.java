package launcher.ui;

import javax.swing.JButton;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class GlowButton extends JButton {

    private final Color color;
    private float glowRadius = 14f;
    private final Timer hoverTimer;
    private Timer pulseTimer;
    private float pulsePhase = 0f;
    private final boolean pulse;
    private final boolean solidFill;

    public GlowButton(String text, Color color, boolean pulse, boolean solidFill) {
        super(text);
        this.color = color;
        this.pulse = pulse;
        this.solidFill = solidFill;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Theme.TEXT);
        setFont(Theme.FONT_BOLD);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        hoverTimer = new Timer(15, e -> repaint());
        hoverTimer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                targetRadius = 34f;
                if (pulseTimer != null) pulseTimer.stop();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                targetRadius = 14f;
                if (pulseTimer != null) pulseTimer.start();
            }
        });

        if (pulse) {
            pulseTimer = new Timer(30, e -> {
                pulsePhase += 0.04f;
                float osc = (float) (Math.sin(pulsePhase) + 1) / 2f; // 0..1
                targetRadius = 14f + osc * 16f;
            });
            pulseTimer.start();
        }
    }

    public GlowButton(String text, Color color) {
        this(text, color, false, false);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (!hoverTimer.isRunning()) hoverTimer.start();
        if (pulseTimer != null && !pulseTimer.isRunning()) pulseTimer.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        hoverTimer.stop();
        if (pulseTimer != null) pulseTimer.stop();
    }

    private float targetRadius = 14f;

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // ease toward target glow radius
        glowRadius += (targetRadius - glowRadius) * 0.15f;

        // soft glow behind the button
        int cx = w / 2;
        int cy = h / 2;
        float radius = Math.max(1f, Math.max(w, h) / 2f + glowRadius);
        Color transparent = new Color(color.getRed(), color.getGreen(), color.getBlue(), 0);
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 140);
        RadialGradientPaint rgp = new RadialGradientPaint(cx, cy, radius, new float[]{0f, 1f},
                new Color[]{glowColor, transparent});
        g2.setPaint(rgp);
        g2.fillOval((int) (cx - radius), (int) (cy - radius), (int) (radius * 2), (int) (radius * 2));

        // button body
        RoundRectangle2D shape = new RoundRectangle2D.Float(0, 0, w, h, 14, 14);
        if (solidFill) {
            GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT, w, h, Theme.ACCENT_2);
            g2.setPaint(gp);
        } else {
            g2.setColor(getModel().isRollover() ? new Color(0x29, 0x29, 0x38) : Theme.BG_PANEL_LIGHT);
        }
        g2.fill(shape);
        g2.setColor(Theme.ACCENT_SOFT.darker());
        g2.draw(shape);

        g2.dispose();
        super.paintComponent(g);
    }
}
