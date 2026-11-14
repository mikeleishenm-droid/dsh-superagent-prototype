package tetris.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;

/**
 * Persists personal bests per game mode. Marathon and Ultra keep the highest
 * score; Sprint keeps the fastest completed 40-line run. Storage failures are
 * ignored so a missing or read-only home directory never breaks the game.
 */
public final class HighScoreStore {

    private static final String SCORE_PREFIX = "best.score.";
    private static final String TIME_PREFIX = "best.time.";

    private final Path file;
    private final Properties data = new Properties();

    public HighScoreStore(Path file) {
        this.file = file;
        load();
    }

    public static HighScoreStore defaultStore() {
        String home = System.getProperty("user.home", ".");
        return new HighScoreStore(Path.of(home, ".tetris-java", "highscores.properties"));
    }

    public int bestScore(GameMode mode) {
        try {
            return Math.max(0, Integer.parseInt(data.getProperty(SCORE_PREFIX + mode.name(), "0")));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /** Fastest completed Sprint time, or 0 when there is no record. */
    public long bestTimeMs(GameMode mode) {
        try {
            return Math.max(0, Long.parseLong(data.getProperty(TIME_PREFIX + mode.name(), "0")));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Records a finished run when it beats the existing personal best.
     *
     * @return true when this is a new record
     */
    public boolean submit(GameResult result) {
        if (result.mode() == GameMode.SPRINT) {
            if (result.lines() < result.mode().targetLines() || result.elapsedMs() <= 0) {
                return false;
            }
            long previous = bestTimeMs(result.mode());
            if (previous > 0 && result.elapsedMs() >= previous) {
                return false;
            }
            data.setProperty(TIME_PREFIX + result.mode().name(),
                    Long.toString(result.elapsedMs()));
            save();
            return true;
        }

        if (result.score() <= bestScore(result.mode())) {
            return false;
        }
        data.setProperty(SCORE_PREFIX + result.mode().name(),
                Integer.toString(result.score()));
        save();
        return true;
    }

    /** The record to display for a mode, or "暂无" when there is none. */
    public String bestText(GameMode mode) {
        if (mode == GameMode.SPRINT) {
            long best = bestTimeMs(mode);
            return best > 0 ? formatTime(best) : "暂无";
        }
        int best = bestScore(mode);
        return best > 0 ? String.format(Locale.ROOT, "%,d", best) : "暂无";
    }

    public static String formatTime(long milliseconds) {
        long totalSeconds = Math.max(0, milliseconds) / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.ROOT, "%02d:%02d", minutes, seconds);
    }

    private void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (InputStream input = Files.newInputStream(file)) {
            data.load(input);
        } catch (IOException | IllegalArgumentException ignored) {
            // A broken records file must not prevent the game from starting.
        }
    }

    private void save() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream output = Files.newOutputStream(temporary)) {
                data.store(output, "Tetris personal bests");
            }
            try {
                Files.move(temporary, file,
                        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // Keep the in-memory record; the next successful save will persist it.
        }
    }
}
