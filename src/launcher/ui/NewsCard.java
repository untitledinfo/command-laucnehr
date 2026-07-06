package launcher.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/** Small gradient info/news card used in the Home page's "Latest News" row. */
public class NewsCard extends JPanel {

    private final Color colorA;
    private final Color colorB;
    private float hover = 0f;
    private final Timer timer;

    public NewsCard(String icon, String title, String subtitle, Color colorA, Color colorB) {
        this.colorA = colorA;
        this.colorB = colorB;
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(14, 16, 14, 16));
        setPreferredSize(new Dimension(220, 120));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        add(iconLabel, BorderLayout.NORTH);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(Theme.FONT_BOLD);
        JLabel subtitleLabel = new JLabel("<html><div style='width:170px'>" + subtitle + "</div></html>");
        subtitleLabel.setForeground(new Color(255, 255, 255, 210));
        subtitleLabel.setFont(Theme.FONT_SUBTITLE);
        textBox.add(titleLabel);
        textBox.add(Box.createVerticalStrut(4));
        textBox.add(subtitleLabel);
        add(textBox, BorderLayout.SOUTH);

        timer = new Timer(15, e -> {
            float target = isMouseOver() ? 1f : 0f;
            hover += (target - hover) * 0.2f;
            repaint();
        });
        timer.start();

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                mouseOver = true;
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                mouseOver = false;
            }
        });
    }

    private boolean mouseOver = false;

    private boolean isMouseOver() {
        return mouseOver;
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
        float lift = hover * 3f;

        RoundRectangle2D shape = new RoundRectangle2D.Float(0, lift, w, h - lift, 16, 16);
        GradientPaint gp = new GradientPaint(0, 0, colorA, w, h, colorB);
        g2.setPaint(gp);
        g2.fill(shape);

        if (hover > 0.02f) {
            g2.setColor(new Color(255, 255, 255, (int) (hover * 40)));
            g2.fill(shape);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
