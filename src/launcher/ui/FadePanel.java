package launcher.ui;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

/** Wrap any component in this and call playFadeIn() whenever it becomes visible. */
public class FadePanel extends JPanel {

    private float overlayAlpha = 0f;
    private Timer timer;

    public FadePanel(Component content) {
        super(new BorderLayout());
        setOpaque(false);
        add(content, BorderLayout.CENTER);
    }

    public void playFadeIn() {
        overlayAlpha = 1f;
        if (timer != null) timer.stop();
        timer = new Timer(15, e -> {
            overlayAlpha -= 0.08f;
            if (overlayAlpha <= 0f) {
                overlayAlpha = 0f;
                timer.stop();
            }
            repaint();
        });
        timer.start();
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (overlayAlpha > 0f) {
            Graphics2DHelper.fillOverlay(g, getWidth(), getHeight(), Theme.BG_DARKEST, overlayAlpha);
        }
    }

    private static final class Graphics2DHelper {
        static void fillOverlay(Graphics g, int w, int h, Color base, float alpha) {
            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            int a = Math.max(0, Math.min(255, (int) (alpha * 255)));
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), a));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }
}
