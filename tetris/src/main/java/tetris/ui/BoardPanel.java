package tetris.ui;

import tetris.game.Board;
import tetris.game.Cell;
import tetris.game.GameEngine;
import tetris.game.GameState;
import tetris.game.Piece;
import tetris.game.Tetromino;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * The playfield: a glossy board with a grid, a landing preview, the active
 * piece, line-clear toasts and the pause/game-over overlay.
 */
public final class BoardPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    public static final int BLOCK = 32;
    public static final int BOARD_WIDTH = Board.WIDTH * BLOCK;
    public static final int BOARD_HEIGHT = Board.HEIGHT * BLOCK;
    private static final int CORNER = 16;
    private static final long TOAST_NANOS = 950_000_000L;

    private final GameEngine engine;
    private String toastText;
    private long toastStartedAt;

    public BoardPanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setOpaque(false);
    }

    /** Shows a short message after a line clear. */
    public void showLineClear(int count) {
        toastText = count >= 4 ? "TETRIS!" : "消除 " + count + " 行";
        toastStartedAt = System.nanoTime();
    }

    public void clearToast() {
        toastText = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        RoundRectangle2D boardShape = new RoundRectangle2D.Double(
                0, 0, BOARD_WIDTH, BOARD_HEIGHT, CORNER, CORNER);
        g2.setPaint(new GradientPaint(0, 0, Theme.BOARD_TOP, 0, BOARD_HEIGHT, Theme.BOARD_BOTTOM));
        g2.fill(boardShape);
        g2.clip(boardShape);

        drawGrid(g2);
        drawLockedCells(g2);
        drawGhost(g2);
        drawCurrentPiece(g2);

        g2.setClip(null);
        drawToast(g2);
        if (engine.state() != GameState.RUNNING) {
            drawOverlay(g2);
        }

        g2.setColor(Theme.alpha(Theme.ACCENT, 42));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Double(0.6, 0.6, BOARD_WIDTH - 1.2, BOARD_HEIGHT - 1.2, CORNER, CORNER));
        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(Theme.BOARD_GRID);
        g2.setStroke(new BasicStroke(1f));
        for (int column = 1; column < Board.WIDTH; column++) {
            int x = column * BLOCK;
            g2.drawLine(x, 0, x, BOARD_HEIGHT);
        }
        for (int row = 1; row < Board.HEIGHT; row++) {
            int y = row * BLOCK;
            g2.drawLine(0, y, BOARD_WIDTH, y);
        }
    }

    private void drawLockedCells(Graphics2D g2) {
        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int column = 0; column < Board.WIDTH; column++) {
                Tetromino type = engine.board().cellAt(column, row);
                if (type != null) {
                    Theme.drawBlock(g2, column * BLOCK, row * BLOCK, BLOCK, Theme.pieceColor(type), 1f);
                }
            }
        }
    }

    private void drawGhost(Graphics2D g2) {
        Piece current = engine.current();
        if (current == null) {
            return;
        }
        int ghostY = engine.ghostY();
        int delta = ghostY - current.y();
        if (delta <= 0) {
            return;
        }
        for (Cell cell : current.cells()) {
            int y = cell.y() + delta;
            if (y >= 0 && y < Board.HEIGHT) {
                Theme.drawGhostBlock(g2, cell.x() * BLOCK, y * BLOCK, BLOCK,
                        Theme.pieceColor(current.type()));
            }
        }
    }

    private void drawCurrentPiece(Graphics2D g2) {
        Piece current = engine.current();
        if (current == null) {
            return;
        }
        for (Cell cell : current.cells()) {
            if (cell.y() >= 0 && cell.y() < Board.HEIGHT) {
                Theme.drawBlock(g2, cell.x() * BLOCK, cell.y() * BLOCK, BLOCK,
                        Theme.pieceColor(current.type()), 1f);
            }
        }
    }

    private void drawToast(Graphics2D g2) {
        if (toastText == null) {
            return;
        }
        long elapsed = System.nanoTime() - toastStartedAt;
        if (elapsed >= TOAST_NANOS) {
            toastText = null;
            return;
        }
        float progress = elapsed / (float) TOAST_NANOS;
        int alpha = Math.round(255 * Math.min(1f, (1f - progress) * 1.6f));
        g2.setFont(Theme.font(Font.BOLD, 26));
        FontMetrics metrics = g2.getFontMetrics();
        int x = (BOARD_WIDTH - metrics.stringWidth(toastText)) / 2;
        int y = 92;

        g2.setColor(Theme.alpha(Color.BLACK, alpha / 2));
        g2.drawString(toastText, x + 2, y + 2);
        g2.setColor(Theme.alpha(Theme.GOLD, alpha));
        g2.drawString(toastText, x, y);
    }

    private void drawOverlay(Graphics2D g2) {
        g2.setColor(new Color(3, 5, 13, 176));
        g2.fill(new RoundRectangle2D.Double(0, 0, BOARD_WIDTH, BOARD_HEIGHT, CORNER, CORNER));

        boolean paused = engine.state() == GameState.PAUSED;
        int cardWidth = 284;
        int cardHeight = 156;
        double cardX = (BOARD_WIDTH - cardWidth) / 2.0;
        double cardY = (BOARD_HEIGHT - cardHeight) / 2.0;
        Theme.drawCard(g2, cardX, cardY, cardWidth, cardHeight);

        g2.setColor(Theme.alpha(paused ? Theme.ACCENT : Theme.DANGER, 190));
        g2.fill(new RoundRectangle2D.Double(cardX + 24, cardY + 18, 40, 3, 3, 3));

        String title = paused ? "已暂停" : "游戏结束";
        String hint = paused ? "按 P 继续游戏" : "按 R 重新开始";
        int centerX = BOARD_WIDTH / 2;

        g2.setFont(Theme.font(Font.BOLD, 28));
        FontMetrics titleMetrics = g2.getFontMetrics();
        g2.setColor(Theme.TEXT);
        g2.drawString(title, centerX - titleMetrics.stringWidth(title) / 2, (int) cardY + 72);

        g2.setFont(Theme.font(Font.PLAIN, 14));
        FontMetrics hintMetrics = g2.getFontMetrics();
        g2.setColor(Theme.MUTED);
        g2.drawString(hint, centerX - hintMetrics.stringWidth(hint) / 2, (int) cardY + 102);

        if (!paused) {
            String score = "得分  " + engine.score();
            g2.setFont(Theme.font(Font.BOLD, 17));
            FontMetrics scoreMetrics = g2.getFontMetrics();
            g2.setColor(Theme.GOLD);
            g2.drawString(score, centerX - scoreMetrics.stringWidth(score) / 2, (int) cardY + 134);
        } else {
            String stats = "等级 " + engine.level() + "   ·   消除 " + engine.lines() + " 行";
            g2.setFont(Theme.font(Font.PLAIN, 13));
            FontMetrics statsMetrics = g2.getFontMetrics();
            g2.setColor(Theme.MUTED);
            g2.drawString(stats, centerX - statsMetrics.stringWidth(stats) / 2, (int) cardY + 132);
        }
    }
}
