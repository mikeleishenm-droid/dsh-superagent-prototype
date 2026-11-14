package tetris.ui;

import tetris.game.Cell;
import tetris.game.GameEngine;
import tetris.game.GameMode;
import tetris.game.HighScoreStore;
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
 * The right-hand information column: score, mode progress, level, lines, hold,
 * next pieces and the controls legend.
 */
public final class SidePanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int HEIGHT = 640;
    private static final String[][] CONTROLS_LEFT = {
            {"← →", "移动"},
            {"↑ / X", "顺时针"},
            {"空格", "硬降"},
            {"P", "暂停"},
            {"R", "重开"},
    };
    private static final String[][] CONTROLS_RIGHT = {
            {"↓", "软降"},
            {"Z", "逆时针"},
            {"C", "暂存"},
            {"Q", "菜单"},
            {"Esc", "暂停"},
    };

    private final GameEngine engine;
    private final HighScoreStore scores;

    public SidePanel(GameEngine engine, HighScoreStore scores) {
        this.engine = engine;
        this.scores = scores;
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
        drawTitle(g2, width);
        drawScoreCard(g2, width);
        drawStats(g2, width);
        drawModeCard(g2, width);
        drawHoldCard(g2, width);
        drawNextCard(g2, width);
        drawControlsCard(g2, width);
        g2.dispose();
    }

    private void drawTitle(Graphics2D g2, int width) {
        g2.setFont(Theme.font(Font.BOLD, 24));
        g2.setColor(Theme.TEXT);
        g2.drawString("俄罗斯方块", 2, 36);

        g2.setFont(Theme.font(Font.BOLD, 9));
        g2.setColor(Theme.ACCENT);
        g2.drawString("T E T R I S   ·   J A V A", 3, 54);

        GameMode mode = engine.mode();
        if (mode != null) {
            g2.setFont(Theme.font(Font.BOLD, 9));
            g2.setColor(Theme.alpha(Theme.MUTED, 210));
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString(mode.englishName(), width - 2 - metrics.stringWidth(mode.englishName()), 54);
        }
    }

    private void drawScoreCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 62, width, 92);

        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("得分", 18, 86);

        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.alpha(Theme.MUTED, 170));
        g2.drawString("最佳", width - 18 - g2.getFontMetrics().stringWidth("最佳"), 86);

        GameMode mode = engine.mode();
        String best = mode == null ? "—" : scores.bestText(mode);
        g2.setFont(Theme.font(Font.BOLD, 13));
        g2.setColor(best.equals("暂无") || best.equals("—") ? Theme.MUTED : Theme.GOLD);
        FontMetrics bestMetrics = g2.getFontMetrics();
        g2.drawString(best, width - 18 - bestMetrics.stringWidth(best), 105);

        g2.setFont(Theme.font(Font.BOLD, 32));
        g2.setColor(Theme.TEXT);
        g2.drawString(UiFormat.number(engine.score()), 18, 134);
    }

    private void drawStats(Graphics2D g2, int width) {
        int gap = 16;
        int cardWidth = (width - gap) / 2;
        drawStatCard(g2, 0, 162, cardWidth, 58, "等级", String.valueOf(engine.level()), Theme.GOLD);
        drawStatCard(g2, cardWidth + gap, 162, cardWidth, 58,
                "消除行", String.valueOf(engine.lines()), Theme.ACCENT);
    }

    private void drawStatCard(Graphics2D g2, double x, double y, double width, double height,
                              String label, String value, Color accent) {
        Theme.drawCard(g2, x, y, width, height);
        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString(label, (int) x + 16, (int) y + 21);

        g2.setFont(Theme.font(Font.BOLD, 20));
        g2.setColor(accent);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(value, (int) (x + width) - 16 - metrics.stringWidth(value), (int) y + 45);
    }

    private void drawModeCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 228, width, 66);
        GameMode mode = engine.mode();

        if (mode == null) {
            g2.setFont(Theme.font(Font.BOLD, 11));
            g2.setColor(Theme.MUTED);
            g2.drawString("模式", 18, 252);
            return;
        }

        int barX = 18;
        int barY = 278;
        int barWidth = width - 36;

        switch (mode) {
            case MARATHON -> {
                int intoLevel = engine.lines() % 10;
                drawModeHeader(g2, width, "等级进度", intoLevel + " / 10");
                drawBar(g2, barX, barY, barWidth, intoLevel / 10f, Theme.ACCENT);
            }
            case SPRINT -> {
                drawModeHeader(g2, width, "冲刺进度", UiFormat.clock(engine.elapsedMs()));
                g2.setFont(Theme.font(Font.BOLD, 12));
                g2.setColor(Theme.alpha(Theme.TEXT, 210));
                g2.drawString(engine.lines() + " / " + mode.targetLines() + " 行", 18, 272);
                drawBar(g2, barX, barY, barWidth,
                        Math.min(1f, engine.lines() / (float) mode.targetLines()), Theme.ACCENT);
            }
            case ULTRA -> {
                drawModeHeader(g2, width, "剩余时间", UiFormat.clock(engine.remainingMs()));
                float remaining = mode.timeLimitMs() == 0 ? 0
                        : engine.remainingMs() / (float) mode.timeLimitMs();
                drawBar(g2, barX, barY, barWidth, remaining, Theme.GOLD);
            }
        }
    }

    private void drawModeHeader(Graphics2D g2, int width, String label, String value) {
        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString(label, 18, 252);

        g2.setFont(Theme.font(Font.BOLD, 15));
        g2.setColor(Theme.ACCENT);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(value, width - 18 - metrics.stringWidth(value), 252);
    }

    private void drawBar(Graphics2D g2, int x, int y, int width, float progress, Color color) {
        g2.setColor(Theme.alpha(Theme.TEXT, 22));
        g2.fillRoundRect(x, y, width, 5, 5, 5);
        int filled = Math.round(width * Math.max(0f, Math.min(1f, progress)));
        if (filled > 0) {
            g2.setColor(color);
            g2.fillRoundRect(x, y, filled, 5, 5, 5);
        }
    }

    private void drawHoldCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 302, width, 76);

        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString("暂存", 18, 326);
        Theme.drawKeyCap(g2, width - 38, 310, 22, 17, "C", engine.held() != null);

        if (engine.held() == null) {
            g2.setFont(Theme.font(Font.PLAIN, 18));
            g2.setColor(Theme.alpha(Theme.MUTED, 110));
            g2.drawString("—", width - 84, 354);
            return;
        }

        float opacity = engine.holdUsed() ? 0.36f : 1f;
        drawPreview(g2, engine.held(), width - 70.0, 350.0, 13, opacity);
        if (engine.holdUsed()) {
            g2.setFont(Theme.font(Font.PLAIN, 9));
            g2.setColor(Theme.alpha(Theme.MUTED, 150));
            g2.drawString("已使用", 52, 326);
        }
    }

    private void drawNextCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 386, width, 86);
        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString("下一个", 18, 410);

        List<Tetromino> next = engine.nextPieces();
        for (int index = 0; index < next.size(); index++) {
            double centerX = width / 2.0 + (index - 1) * 96.0;
            drawPreview(g2, next.get(index), centerX, 436.0, 12, 1f);
        }
    }

    private void drawControlsCard(Graphics2D g2, int width) {
        Theme.drawCard(g2, 0, 480, width, 152);

        g2.setFont(Theme.font(Font.BOLD, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString("操作", 18, 504);
        g2.setFont(Theme.font(Font.PLAIN, 8));
        g2.setColor(Theme.alpha(Theme.MUTED, 140));
        g2.drawString("CONTROLS", width - 70, 504);

        for (int row = 0; row < 5; row++) {
            int baseline = 524 + row * 20;
            drawControl(g2, 14, baseline, CONTROLS_LEFT[row]);
            drawControl(g2, 154, baseline, CONTROLS_RIGHT[row]);
        }
    }

    private void drawControl(Graphics2D g2, int x, int baseline, String[] control) {
        Theme.drawKeyCap(g2, x, baseline - 12, 50, 16, control[0], false);
        g2.setFont(Theme.font(Font.PLAIN, 10));
        g2.setColor(Theme.alpha(Theme.TEXT, 205));
        g2.drawString(control[1], x + 56, baseline);
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
