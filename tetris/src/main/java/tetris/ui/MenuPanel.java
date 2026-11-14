package tetris.ui;

import tetris.game.GameMode;
import tetris.game.HighScoreStore;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * The start screen: pick Marathon, Sprint 40 or Ultra 2:00 and see the personal
 * best for each mode.
 */
public final class MenuPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int WIDTH = 688;
    private static final int HEIGHT = 684;
    private static final int CARD_X = 44;
    private static final int CARD_WIDTH = 600;
    private static final int CARD_HEIGHT = 96;
    private static final int CARD_GAP = 16;
    private static final int FIRST_CARD_Y = 200;

    private final HighScoreStore scores;
    private int selected;

    public MenuPanel(HighScoreStore scores) {
        this.scores = scores;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setOpaque(false);
    }

    public GameMode selectedMode() {
        return GameMode.values()[selected];
    }

    public void select(GameMode mode) {
        selected = mode.ordinal();
        repaint();
    }

    public void moveSelection(int delta) {
        int count = GameMode.values().length;
        selected = Math.floorMod(selected + delta, count);
        repaint();
    }

    public void refresh() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Theme.paintWindowBackground(g2, getWidth(), getHeight());

        drawHeader(g2);
        for (int index = 0; index < GameMode.values().length; index++) {
            drawModeCard(g2, GameMode.values()[index], index);
        }
        drawFooter(g2);
        g2.dispose();
    }

    private void drawHeader(Graphics2D g2) {
        g2.setFont(Theme.font(Font.BOLD, 44));
        g2.setColor(Theme.TEXT);
        g2.drawString("俄罗斯方块", 44, 112);

        g2.setFont(Theme.font(Font.BOLD, 12));
        g2.setColor(Theme.ACCENT);
        g2.drawString("T E T R I S   ·   J A V A", 46, 140);

        g2.setFont(Theme.font(Font.PLAIN, 13));
        g2.setColor(Theme.MUTED);
        g2.drawString("选择模式开始游戏", 46, 178);
    }

    private void drawModeCard(Graphics2D g2, GameMode mode, int index) {
        int y = FIRST_CARD_Y + index * (CARD_HEIGHT + CARD_GAP);
        boolean active = index == selected;

        Theme.drawCard(g2, CARD_X, y, CARD_WIDTH, CARD_HEIGHT);
        if (active) {
            g2.setColor(Theme.alpha(Theme.ACCENT, 20));
            g2.fill(new RoundRectangle2D.Double(CARD_X, y, CARD_WIDTH, CARD_HEIGHT, 18, 18));
            g2.setColor(Theme.alpha(Theme.ACCENT, 210));
            g2.fill(new RoundRectangle2D.Double(CARD_X, y + 20, 4, CARD_HEIGHT - 40, 4, 4));
            g2.setColor(Theme.alpha(Theme.ACCENT, 200));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new RoundRectangle2D.Double(
                    CARD_X + 0.5, y + 0.5, CARD_WIDTH - 1, CARD_HEIGHT - 1, 18, 18));
        }

        Theme.drawKeyCap(g2, CARD_X + 24, y + 24, 26, 24, String.valueOf(index + 1), active);

        g2.setFont(Theme.font(Font.BOLD, 22));
        g2.setColor(active ? Theme.TEXT : Theme.alpha(Theme.TEXT, 220));
        g2.drawString(mode.displayName(), CARD_X + 66, y + 46);

        g2.setFont(Theme.font(Font.BOLD, 9));
        g2.setColor(active ? Theme.ACCENT : Theme.alpha(Theme.ACCENT, 150));
        g2.drawString(mode.englishName(), CARD_X + 66, y + 65);

        g2.setFont(Theme.font(Font.PLAIN, 11));
        g2.setColor(Theme.MUTED);
        g2.drawString(mode.description(), CARD_X + 66, y + 84);

        String record = scores.bestText(mode);
        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.alpha(Theme.MUTED, 170));
        FontMetrics small = g2.getFontMetrics();
        g2.drawString("最佳", CARD_X + CARD_WIDTH - 24 - small.stringWidth("最佳"), y + 32);

        g2.setFont(Theme.font(Font.BOLD, 18));
        g2.setColor(record.equals("暂无") ? Theme.MUTED : Theme.GOLD);
        FontMetrics metrics = g2.getFontMetrics();
        String suffix = record.equals("暂无") || mode == GameMode.SPRINT ? "" : " 分";
        String value = record + suffix;
        g2.drawString(value, CARD_X + CARD_WIDTH - 24 - metrics.stringWidth(value), y + 58);
    }

    private void drawFooter(Graphics2D g2) {
        g2.setFont(Theme.font(Font.PLAIN, 11));
        g2.setColor(Theme.alpha(Theme.TEXT, 195));
        g2.drawString("↑ ↓ 选择        Enter / Space 开始        1 / 2 / 3 快速开始", 44, 610);

        g2.setFont(Theme.font(Font.PLAIN, 9));
        g2.setColor(Theme.alpha(Theme.MUTED, 170));
        g2.drawString("个人最佳保存在本机 · 用 R 直接开始当前选择", 44, 638);
    }
}
