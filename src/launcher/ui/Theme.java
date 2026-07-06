package launcher.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

public final class Theme {
    private Theme() {
    }

    public static final Color BG_DARKEST = new Color(0x0b, 0x0b, 0x12);
    public static final Color BG_DARK = new Color(0x12, 0x12, 0x18);
    public static final Color BG_PANEL = new Color(0x18, 0x18, 0x22);
    public static final Color BG_PANEL_LIGHT = new Color(0x20, 0x20, 0x2c);
    public static final Color BORDER = new Color(0x2b, 0x2b, 0x3a);
    public static final Color ACCENT = new Color(0x8b, 0x5c, 0xf6);
    public static final Color ACCENT_2 = new Color(0x22, 0xd3, 0xee);
    public static final Color ACCENT_SOFT = new Color(0xa7, 0x8b, 0xfa);
    public static final Color TEXT = new Color(0xea, 0xea, 0xf5);
    public static final Color TEXT_DIM = new Color(0x94, 0x94, 0xa8);
    public static final Color SUCCESS = new Color(0x34, 0xd3, 0x99);
    public static final Color ERROR = new Color(0xf8, 0x71, 0x71);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BRAND = new Font("Segoe UI", Font.BOLD, 18);

    public static void antialias(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static void paintRoundedPanel(Graphics2D g2, int w, int h, int arc, Color bg, Color border) {
        antialias(g2);
        RoundRectangle2D shape = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc);
        g2.setColor(bg);
        g2.fill(shape);
        if (border != null) {
            g2.setColor(border);
            g2.draw(shape);
        }
    }

    public static GradientPaint accentGradient(int w, int h) {
        return new GradientPaint(0, 0, ACCENT, w, h, ACCENT_2);
    }
}
