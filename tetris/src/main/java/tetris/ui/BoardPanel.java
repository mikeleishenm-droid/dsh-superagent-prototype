package tetris.ui;

import tetris.game.Board;
import tetris.game.Cell;
import tetris.game.GameEngine;
import tetris.game.GameState;
import tetris.game.Piece;
import tetris.game.Tetromino;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Draws the playfield. The first version of the game uses flat blocks and a
 * plain dark background; the screenshot harness captures this revision as the
 * "before" baseline.
 */
public final class BoardPanel extends JPanel {

    public static final int BLOCK = 32;
    public static final int BOARD_WIDTH = Board.WIDTH * BLOCK;
    public static final int BOARD_HEIGHT = Board.HEIGHT * BLOCK;

    private static final Color BACKGROUND = new Color(12, 13, 20);
    private static final Color GRID = new Color(255, 255, 255, 16);

    private final GameEngine engine;

    public BoardPanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.setColor(GRID);
        for (int column = 1; column < Board.WIDTH; column++) {
            int x = column * BLOCK;
            g2.drawLine(x, 0, x, BOARD_HEIGHT);
        }
        for (int row = 1; row < Board.HEIGHT; row++) {
            int y = row * BLOCK;
            g2.drawLine(0, y, BOARD_WIDTH, y);
        }

        for (int row = 0; row < Board.HEIGHT; row++) {
            for (int column = 0; column < Board.WIDTH; column++) {
                Tetromino type = engine.board().cellAt(column, row);
                if (type != null) {
                    drawCell(g2, column, row, type);
                }
            }
        }

        Piece current = engine.current();
        if (current != null) {
            for (Cell cell : current.cells()) {
                if (cell.y() >= 0 && cell.y() < Board.HEIGHT) {
                    drawCell(g2, cell.x(), cell.y(), current.type());
                }
            }
        }

        if (engine.state() != GameState.RUNNING) {
            drawOverlay(g2);
        }
        g2.dispose();
    }

    private void drawCell(Graphics2D g2, int column, int row, Tetromino type) {
        int x = column * BLOCK;
        int y = row * BLOCK;
        g2.setColor(colorOf(type));
        g2.fillRect(x + 1, y + 1, BLOCK - 2, BLOCK - 2);
        g2.setColor(new Color(255, 255, 255, 55));
        g2.drawLine(x + 3, y + 3, x + BLOCK - 4, y + 3);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.drawRect(x + 1, y + 1, BLOCK - 2, BLOCK - 2);
    }

    private void drawOverlay(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        boolean paused = engine.state() == GameState.PAUSED;
        String title = paused ? "PAUSED" : "GAME OVER";
        String hint = paused ? "Press P to continue" : "Press R to restart";

        Font titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 30);
        Font hintFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        g2.setFont(titleFont);
        FontMetrics titleMetrics = g2.getFontMetrics();
        int titleY = getHeight() / 2 - 10;
        g2.setColor(Color.WHITE);
        g2.drawString(title, (getWidth() - titleMetrics.stringWidth(title)) / 2, titleY);

        g2.setFont(hintFont);
        FontMetrics hintMetrics = g2.getFontMetrics();
        g2.setColor(new Color(190, 196, 214));
        g2.drawString(hint, (getWidth() - hintMetrics.stringWidth(hint)) / 2, titleY + 30);

        if (!paused) {
            String score = "Score: " + engine.score();
            g2.drawString(score, (getWidth() - hintMetrics.stringWidth(score)) / 2, titleY + 54);
        }
    }

    static Color colorOf(Tetromino type) {
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
}
