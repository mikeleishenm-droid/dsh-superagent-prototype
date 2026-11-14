package tetris.ui;

import org.junit.jupiter.api.Test;
import tetris.game.GameEngine;
import tetris.game.GameState;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless smoke tests for the painted panels. They catch layout and painting
 * exceptions without needing a window, and verify that game state actually
 * changes what is rendered.
 */
class UiRenderTest {

    @Test
    void boardPanelPaintsRunningPausedAndGameOverStates() {
        GameEngine engine = new GameEngine(7);
        BoardPanel panel = new BoardPanel(engine);
        panel.setSize(panel.getPreferredSize());

        int running = renderHash(panel);

        engine.togglePause();
        int paused = renderHash(panel);
        assertNotEquals(running, paused, "pause overlay must change the board");

        engine.togglePause();
        int drops = 0;
        while (engine.state() != GameState.GAME_OVER && drops++ < 100) {
            engine.hardDrop();
        }
        int gameOver = renderHash(panel);
        assertNotEquals(running, gameOver, "game-over overlay must change the board");

        panel.showLineClear(4);
        renderHash(panel); // must not throw while a toast is showing
    }

    @Test
    void sidePanelPaintsScoreStatsAndPreviews() {
        GameEngine engine = new GameEngine(11);
        SidePanel panel = new SidePanel(engine);
        panel.setSize(panel.getPreferredSize());

        int initial = renderHash(panel);

        assertTrue(engine.hold());
        engine.hardDrop();
        int afterHold = renderHash(panel);
        assertNotEquals(initial, afterHold, "hold and score changes must be rendered");
    }

    private static int renderHash(JComponent panel) {
        BufferedImage image = new BufferedImage(
                Math.max(1, panel.getWidth()), Math.max(1, panel.getHeight()),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            panel.paint(g2);
        } finally {
            g2.dispose();
        }
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        return Arrays.hashCode(pixels);
    }
}
