package tetris.ui;

import tetris.game.FinishReason;
import tetris.game.GameMode;
import tetris.game.GameResult;
import tetris.game.HighScoreStore;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * The end-of-run screen: the primary metric, the result of the run and the
 * personal best, plus the keys to replay or return to the menu.
 */
public final class SummaryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 688;
    private static final int HEIGHT = 684;

    private final HighScoreStore scores;
    private FinishReason reason = FinishReason.GAME_OVER;
    private GameResult result;
    private boolean newRecord;

    public SummaryPanel(HighScoreStore scores) {
        this.scores = scores;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setOpaque(false);
    }

    public void show(FinishReason reason, GameResult result, boolean newRecord) {
        this.reason = reason;
        this.result = result;
        this.newRecord = newRecord;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Theme.paintWindowBackground(g2, getWidth(), getHeight());

        if (result == null) {
            g2.dispose();
            return;
        }

        drawHeader(g2, result.mode());
        drawPrimaryCard(g2, result.mode());
        drawStats(g2);
        drawBest(g2, result.mode());
        drawFooter(g2);

        g2.dispose();
    }

    private void drawHeader(Graphics2D g2, GameMode mode) {
        String title = switch (reason) {
            case TARGET_REACHED -> "完成！";
            case TIME_UP -> "时间到";
            case GAME_OVER -> "游戏结束";
        };
        g2.setFont(Theme.font(Font.BOLD, 38));
        g2.setColor(Theme.TEXT);
        g2.drawString(title, 44, 92);

        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(Theme.MUTED);
        g2.drawString(mode.displayName() + " · " + mode.englishName()
                + " · 消除 " + result.lines() + " 行", 46, 120);
    }

    private void drawPrimaryCard(Graphics2D g2, GameMode mode) {
        Theme.drawCard(g2, 44, 148, 600, 126);

        boolean sprint = mode == GameMode.SPRINT;
        String label = sprint ? "用时" : "得分";
        String primary = sprint ? UiFormat.clock(result.elapsedMs()) : UiFormat.number(result.score());
        String secondaryLabel = sprint ? "得分" : "用时";
        String secondary = sprint ? UiFormat.number(result.score()) : UiFormat.clock(result.elapsedMs());

        g2.setFont(Theme.font(Font.BOLD, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString(label, 68, 178);

        g2.setFont(Theme.font(Font.BOLD, 44));
        g2.setColor(Theme.TEXT);
        g2.drawString(primary, 68, 228);

        g2.setFont(Theme.font(Font.PLAIN, 10));
        g2.setColor(Theme.MUTED);
        g2.drawString(secondaryLabel, 440, 196);

        g2.setFont(Theme.font(Font.BOLD, 24));
        g2.setColor(Theme.alpha(Theme.TEXT, 220));
        g2.drawString(secondary, 440, 230);

        if (newRecord) {
            int badgeWidth = 94;
            int badgeX = 44 + 600 - 24 - badgeWidth;
            int badgeY = 166;
            g2.setColor(Theme.alpha(Theme.GOLD, 34));
            g2.fill(new RoundRectangle2D.Double(badgeX, badgeY, badgeWidth, 26, 13, 13));
            g2.setColor(Theme.GOLD);
            g2.setFont(Theme.font(Font.BOLD, 12));
            FontMetrics metrics = g2.getFontMetrics();
            g2.drawString("新纪录!", badgeX + (badgeWidth - metrics.stringWidth("新纪录!")) / 2, badgeY + 18);
        }
    }

    private void drawStats(Graphics2D g2) {
        String[][] stats = {
                {"等级", String.valueOf(result.level())},
                {"消除行", String.valueOf(result.lines())},
                {"TETRIS", String.valueOf(result.tetrises())},
                {"最大连击", "×" + result.maxCombo()},
                {"方块数", String.valueOf(result.piecesPlaced())},
                {"方块 / 秒", UiFormat.decimal(result.piecesPerSecond())},
        };
        int cardWidth = 190;
        int cardHeight = 84;
        int gap = 15;
        for (int index = 0; index < stats.length; index++) {
            int column = index % 3;
            int row = index / 3;
            int x = 44 + column * (cardWidth + gap);
            int y = 296 + row * (cardHeight + 16);
            Theme.drawCard(g2, x, y, cardWidth, cardHeight);

            g2.setFont(Theme.font(Font.BOLD, 10));
            g2.setColor(Theme.MUTED);
            g2.drawString(stats[index][0], x + 16, y + 24);

            g2.setFont(Theme.font(Font.BOLD, 24));
            g2.setColor(index < 3 ? Theme.TEXT : Theme.alpha(Theme.TEXT, 225));
            g2.drawString(stats[index][1], x + 16, y + 58);
        }
    }

    private void drawBest(Graphics2D g2, GameMode mode) {
        g2.setFont(Theme.font(Font.PLAIN, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString("个人最佳", 46, 520);

        String best = scores.bestText(mode);
        g2.setFont(Theme.font(Font.BOLD, 18));
        g2.setColor(best.equals("暂无") ? Theme.MUTED : Theme.GOLD);
        String suffix = best.equals("暂无") || mode == GameMode.SPRINT ? "" : " 分";
        g2.drawString(best + suffix, 46, 546);
    }

    private void drawFooter(Graphics2D g2) {
        g2.setColor(Theme.alpha(Theme.TEXT, 20));
        g2.drawLine(44, 582, 644, 582);

        g2.setFont(Theme.font(Font.PLAIN, 12));
        g2.setColor(Theme.alpha(Theme.TEXT, 210));
        g2.drawString("R 再来一局        Enter / Q 返回菜单", 44, 616);

        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.alpha(Theme.MUTED, 170));
        g2.drawString(newRecord ? "新纪录已保存到本机" : "个人最佳可在菜单查看", 44, 642);
    }
}
