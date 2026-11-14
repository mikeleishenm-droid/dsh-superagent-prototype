package tetris.ui;

import tetris.game.Tetromino;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared palette and painting helpers for the game. Keeping these in one place
 * makes the board, the side panel and the window consistent.
 */
public final class Theme {

    public static final Color WINDOW_TOP = new Color(12, 15, 30);
    public static final Color WINDOW_BOTTOM = new Color(27, 32, 58);
    public static final Color BOARD_TOP = new Color(11, 14, 29);
    public static final Color BOARD_BOTTOM = new Color(6, 8, 18);
    public static final Color BOARD_GRID = new Color(255, 255, 255, 12);
    public static final Color CARD = new Color(25, 30, 54, 236);
    public static final Color CARD_BORDER = new Color(255, 255, 255, 22);
    public static final Color TEXT = new Color(238, 242, 255);
    public static final Color MUTED = new Color(143, 153, 189);
    public static final Color ACCENT = new Color(56, 189, 248);
    public static final Color GOLD = new Color(250, 204, 21);
    public static final Color DANGER = new Color(248, 113, 113);
    public static final Color KEYCAP = new Color(255, 255, 255, 15);
    public static final Color KEYCAP_TEXT = new Color(214, 222, 245);

    private static final String FONT_FAMILY = "SansSerif";

    private Theme() {
    }

    public static Font font(int style, float size) {
        return new Font(FONT_FAMILY, style, Math.round(size));
    }

    public static Color pieceColor(Tetromino type) {
        return switch (type) {
            case I -> new Color(34, 211, 238);
            case O -> new Color(250, 204, 21);
            case T -> new Color(168, 85, 247);
            case S -> new Color(74, 222, 128);
            case Z -> new Color(248, 113, 113);
            case J -> new Color(96, 165, 250);
            case L -> new Color(251, 146, 60);
        };
    }

    public static Color alpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    /** Paints a glossy rounded block. {@code opacity} is 0..1. */
    public static void drawBlock(Graphics2D g2, double x, double y, double size, Color color, float opacity) {
        float clamped = Math.max(0f, Math.min(1f, opacity));
        int fillAlpha = Math.round(255 * clamped);
        double inset = Math.max(1.0, size * 0.055);
        double arc = size * 0.24;
        Shape shape = new RoundRectangle2D.Double(
                x + inset, y + inset, size - 2 * inset, size - 2 * inset, arc, arc);

        Color top = brighten(color, 0.30f);
        Color bottom = darken(color, 0.24f);
        g2.setPaint(new GradientPaint(
                (float) x, (float) y, alpha(top, fillAlpha),
                (float) x, (float) (y + size), alpha(bottom, fillAlpha)));
        g2.fill(shape);

        if (clamped > 0.2f) {
            g2.setColor(alpha(Color.WHITE, Math.round(58 * clamped)));
            g2.setStroke(new BasicStroke(
                    (float) Math.max(1.0, size * 0.045), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(new Line2D.Double(
                    x + inset + size * 0.14, y + inset + size * 0.12,
                    x + size - inset - size * 0.14, y + inset + size * 0.12));
        }

        g2.setColor(alpha(brighten(color, 0.45f), Math.round(95 * clamped)));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(shape);
    }

    /** Paints the translucent landing preview. */
    public static void drawGhostBlock(Graphics2D g2, double x, double y, double size, Color color) {
        double inset = Math.max(1.0, size * 0.055);
        double arc = size * 0.24;
        Shape shape = new RoundRectangle2D.Double(
                x + inset, y + inset, size - 2 * inset, size - 2 * inset, arc, arc);
        g2.setColor(alpha(color, 22));
        g2.fill(shape);
        g2.setColor(alpha(color, 150));
        g2.setStroke(new BasicStroke((float) Math.max(1.5, size * 0.06)));
        g2.draw(shape);
    }

    public static void drawCard(Graphics2D g2, double x, double y, double width, double height) {
        RoundRectangle2D card = new RoundRectangle2D.Double(x, y, width, height, 18, 18);
        g2.setColor(CARD);
        g2.fill(card);
        g2.setColor(CARD_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(card);
    }

    /** Paints the window backdrop with two soft colour glows. */
    public static void paintWindowBackground(Graphics2D g2, int width, int height) {
        g2.setPaint(new GradientPaint(0, 0, WINDOW_TOP, 0, height, WINDOW_BOTTOM));
        g2.fillRect(0, 0, width, height);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(width * 0.82f, height * 0.06f),
                Math.max(width, height) * 0.55f,
                new float[]{0f, 1f},
                new Color[]{alpha(ACCENT, 34), alpha(ACCENT, 0)},
                RadialGradientPaint.CycleMethod.NO_CYCLE));
        g2.fillRect(0, 0, width, height);

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(width * 0.10f, height * 0.92f),
                Math.max(width, height) * 0.5f,
                new float[]{0f, 1f},
                new Color[]{alpha(new Color(168, 85, 247), 30), alpha(new Color(168, 85, 247), 0)},
                RadialGradientPaint.CycleMethod.NO_CYCLE));
        g2.fillRect(0, 0, width, height);
    }

    private static Color brighten(Color color, float amount) {
        return mix(color, Color.WHITE, amount);
    }

    private static Color darken(Color color, float amount) {
        return mix(color, Color.BLACK, amount);
    }

    private static Color mix(Color color, Color other, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        int red = Math.round(color.getRed() + (other.getRed() - color.getRed()) * clamped);
        int green = Math.round(color.getGreen() + (other.getGreen() - color.getGreen()) * clamped);
        int blue = Math.round(color.getBlue() + (other.getBlue() - color.getBlue()) * clamped);
        return new Color(red, green, blue);
    }

    /** A small rounded key cap used by the controls legend. */
    public static void drawKeyCap(Graphics2D g2, int x, int y, int width, int height, String label, boolean highlighted) {
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, width, height, 6, 6);
        g2.setColor(highlighted ? alpha(ACCENT, 34) : KEYCAP);
        g2.fill(shape);
        g2.setColor(highlighted ? alpha(ACCENT, 130) : CARD_BORDER);
        g2.setStroke(new BasicStroke(1f));
        g2.draw(shape);

        g2.setFont(font(Font.BOLD, 10));
        g2.setColor(highlighted ? ACCENT : KEYCAP_TEXT);
        var metrics = g2.getFontMetrics();
        int baseline = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g2.drawString(label, x + (width - metrics.stringWidth(label)) / 2, baseline);
    }
}
