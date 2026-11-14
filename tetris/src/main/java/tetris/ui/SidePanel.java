package tetris.ui;

import tetris.game.Cell;
import tetris.game.GameEngine;
import tetris.game.Tetromino;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

/**
 * The right-hand information column: score, level, lines, hold, next pieces and
 * the controls legend. Everything is custom painted so the panel matches the
 * board's visual language.
 */
public final class SidePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int HEIGHT = 640;
    private static final String[][] CONTROLS = {
            {"← →", "移动"},
            {"↓", "软降"},
            {"↑ / X", "顺时针旋转"},
            {"Z", "逆时针旋转"},
            {"空格", "硬降"},
            {"C", "暂存"},
            {"P", "暂停"},
            {"R", "重新开始"},
    };

    private final GameEngine engine;

    public SidePanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(300, HEIGHT));
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        drawTitle(g2);
        drawScoreCard(g2, width);
        drawStats(g2, width);
        drawHoldCard(g2, width);
        drawNextCard(g2, width);
        drawControlsCard(g2, width);
        g2.dispose();
    }

    private void drawTitle(Graphics2D g2) {
        g2.setFont(Theme.font(Font.BOLD, 26));
        g2.setColor(Theme.TEXT);
        g2.drawString("俄罗斯方块", 2, 38);

        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.ACCENT);
        g2.drawString("T E T R I S   ·   J A V A", 3, 58);
    }

    private void drawScoreCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 72, width, 82);

        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("得分", 18, 98);

        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.alpha(Theme.MUTED, 150));
        g2.drawString("SCORE", 18, 113);

        g2.setFont(Theme.font(Font.BOLD, 34));
        g2.setColor(Theme.TEXT);
        g2.drawString(String.valueOf(engine.score()), 18, 144);

        // Small progress bar: ten cleared lines fill it and raise the level.
        int barX = width - 86;
        int barY = 126;
        int barWidth = 68;
        int barHeight = 5;
        g2.setColor(Theme.alpha(Theme.TEXT, 22));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        int filled = Math.round(barWidth * (engine.lines() % 10) / 10f);
        if (filled > 0) {
            g2.setColor(Theme.ACCENT);
            g2.fillRoundRect(barX, barY, filled, barHeight, 5, 5);
        }
        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.MUTED);
        g2.drawString((engine.lines() % 10) + " / 10 升级", barX, 148);
    }

    private void drawStats(Graphics2D g2, int width) {
        int gap = 16;
        int cardWidth = (width - gap) / 2;
        drawStatCard(g2, 0, 162, cardWidth, 62, "等级", String.valueOf(engine.level()), Theme.GOLD);
        drawStatCard(g2, cardWidth + gap, 162, cardWidth, 62,
                "消除行", String.valueOf(engine.lines()), Theme.ACCENT);
    }

    private void drawStatCard(Graphics2D g2, double x, double y, double width, double height,
                              String label, String value, Color accent) {
        Theme.drawCard(g2, x, y, width, height);
        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString(label, (int) x + 16, (int) y + 23);

        g2.setFont(Theme.font(Font.BOLD, 22));
        g2.setColor(accent);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(value, (int) (x + width) - 16 - metrics.stringWidth(value), (int) y + 48);
    }

    private void drawHoldCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 232, width, 84);

        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("暂存", 18, 258);
        Theme.drawKeyCap(g2, width - 38, 242, 22, 17, "C", engine.held() != null);

        if (engine.held() == null) {
            g2.setFont(Theme.font(Font.PLAIN, 18));
            g2.setColor(Theme.alpha(Theme.MUTED, 110));
            g2.drawString("—", width - 84, 292);
            return;
        }

        float opacity = engine.holdUsed() ? 0.36f : 1f;
        drawPreview(g2, engine.held(), width - 70.0, 286.0, 14, opacity);
        if (engine.holdUsed()) {
            g2.setFont(Theme.font(Font.PLAIN, 9));
            g2.setColor(Theme.alpha(Theme.MUTED, 150));
            g2.drawString("已使用", 52, 258);
        }
    }

    private void drawNextCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 324, width, 94);
        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("下一个", 18, 348);

        List<Tetromino> next = engine.nextPieces();
        for (int index = 0; index < next.size(); index++) {
            double centerX = width / 2.0 + (index - 1) * 96.0;
            drawPreview(g2, next.get(index), centerX, 380.0, 13, 1f);
        }
    }

    private void drawControlsCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 426, width, 206);

        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("操作", 18, 452);
        g2.setFont(Theme.font(Font.PLAIN, 8));
        g2.setColor(Theme.alpha(Theme.MUTED, 140));
        g2.drawString("CONTROLS", width - 70, 452);

        int baseline = 474;
        int rowHeight = 19;
        for (int index = 0; index < CONTROLS.length; index++) {
            int y = baseline + index * rowHeight;
            Theme.drawKeyCap(g2, 16, y - 13, 58, 17, CONTROLS[index][0], false);
            g2.setFont(Theme.font(Font.PLAIN, 11));
            g2.setColor(Theme.alpha(Theme.TEXT, 205));
            g2.drawString(CONTROLS[index][1], 86, y);
        }
    }

    /**
     * Draws a piece centred on the given point. Uses the spawn rotation of the
     * shape and its actual bounding box so every piece looks balanced.
     */
    private void drawPreview(Graphics2D g2, Tetromino type, double centerX, double centerY,
                             int block, float opacity) {
        List<Cell> cells = type.cells(0);
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Cell cell : cells) {
            minX = Math.min(minX, cell.x());
            maxX = Math.max(maxX, cell.x());
            minY = Math.min(minY, cell.y());
            maxY = Math.max(maxY, cell.y());
        }
        double pieceWidth = (maxX - minX + 1) * block;
        double pieceHeight = (maxY - minY + 1) * block;
        double originX = centerX - pieceWidth / 2 - minX * block;
        double originY = centerY - pieceHeight / 2 - minY * block;
        for (Cell cell : cells) {
            Theme.drawBlock(g2, originX + cell.x() * block, originY + cell.y() * block,
                    block, Theme.pieceColor(type), opacity);
        }
    }
}
