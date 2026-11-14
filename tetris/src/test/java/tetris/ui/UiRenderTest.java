package tetris.ui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tetris.game.ClearResult;
import tetris.game.FinishReason;
import tetris.game.GameEngine;
import tetris.game.GameMode;
import tetris.game.GameResult;
import tetris.game.GameState;
import tetris.game.HighScoreStore;

import javax.swing.JComponent;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Headless smoke tests for the painted panels. They catch layout and painting
 * exceptions without needing a window, and verify that game state actually
 * changes what is rendered.
 */
class UiRenderTest {

    @TempDir
    Path directory;

    private HighScoreStore scores() {
        return new HighScoreStore(directory.resolve("highscores.properties"));
    }

    @Test
    void boardPanelPaintsRunningPausedAndFinishedStates() {
        GameEngine engine = new GameEngine(7);
        engine.startGame(GameMode.MARATHON);
        BoardPanel panel = new BoardPanel(engine);
        panel.setSize(panel.getPreferredSize());

        int running = renderHash(panel);

        engine.togglePause();
        int paused = renderHash(panel);
        assertNotEquals(running, paused, "pause overlay must change the board");

        engine.togglePause();
        panel.showLineClear(new ClearResult(4, 1_200, 3, true));
        renderHash(panel); // must not throw while a toast is showing

        int drops = 0;
        while (engine.state() != GameState.FINISHED && drops++ < 100) {
            engine.hardDrop();
        }
        renderHash(panel);
    }

    @Test
    void sidePanelPaintsScoreStatsAndPreviews() {
        GameEngine engine = new GameEngine(11);
        engine.startGame(GameMode.SPRINT);
        SidePanel panel = new SidePanel(engine, scores());
        panel.setSize(panel.getPreferredSize());

        int initial = renderHash(panel);

        assertTrue(engine.hold());
        engine.hardDrop();
        int afterHold = renderHash(panel);
        assertNotEquals(initial, afterHold, "hold and score changes must be rendered");
    }

    @Test
    void menuPaintsTheSelectedMode() {
        MenuPanel panel = new MenuPanel(scores());
        panel.setSize(panel.getPreferredSize());

        int marathon = renderHash(panel);
        panel.moveSelection(1);
        int sprint = renderHash(panel);
        assertNotEquals(marathon, sprint, "selection highlight must be rendered");
    }

    @Test
    void summaryPaintsResultAndNewRecordBadge() {
        SummaryPanel panel = new SummaryPanel(scores());
        panel.setSize(panel.getPreferredSize());
        GameResult result = new GameResult(GameMode.SPRINT, 5_400, 40, 5,
                83_000, 120, 3, 7);

        panel.show(FinishReason.TARGET_REACHED, result, false);
        int normal = renderHash(panel);
        panel.show(FinishReason.TARGET_REACHED, result, true);
        int record = renderHash(panel);
        assertNotEquals(normal, record, "new-record badge must be rendered");
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
