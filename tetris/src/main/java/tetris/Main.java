package tetris;

import tetris.game.GameEngine;
import tetris.game.HighScoreStore;
import tetris.ui.GameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point for the Tetris game. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        long seed = parseSeed(args);
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // The custom painted UI does not depend on the look and feel.
            }
            HighScoreStore scores = HighScoreStore.defaultStore();
            GameFrame frame = new GameFrame(new GameEngine(seed), scores);
            frame.start();
        });
    }

    /**
     * Supports {@code --seed <number>} so a game can be replayed for debugging or
     * for reproducible screenshots.
     */
    private static long parseSeed(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("--seed".equals(args[i])) {
                try {
                    return Long.parseLong(args[i + 1]);
                } catch (NumberFormatException ignored) {
                    // fall through to a time-based seed
                }
            }
        }
        return System.nanoTime();
    }
}
