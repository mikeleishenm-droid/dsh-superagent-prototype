package tetris.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighScoreStoreTest {

    @TempDir
    Path directory;

    private HighScoreStore store() {
        return new HighScoreStore(directory.resolve("highscores.properties"));
    }

    private static GameResult result(GameMode mode, int score, int lines, long elapsedMs) {
        return new GameResult(mode, score, lines, 1, elapsedMs, 10, 0, 0);
    }

    @Test
    void freshStoreHasNoRecords() {
        HighScoreStore store = store();
        assertEquals(0, store.bestScore(GameMode.MARATHON));
        assertEquals(0, store.bestTimeMs(GameMode.SPRINT));
        assertEquals("暂无", store.bestText(GameMode.MARATHON));
        assertEquals("暂无", store.bestText(GameMode.SPRINT));
    }

    @Test
    void marathonKeepsTheHighestScoreAndPersistsIt() {
        HighScoreStore store = store();
        assertTrue(store.submit(result(GameMode.MARATHON, 1_200, 10, 60_000)));
        assertEquals(1_200, store.bestScore(GameMode.MARATHON));
        assertFalse(store.submit(result(GameMode.MARATHON, 1_199, 10, 60_000)));
        assertTrue(store.submit(result(GameMode.MARATHON, 9_876, 40, 120_000)));
        assertEquals(9_876, store.bestScore(GameMode.MARATHON));

        HighScoreStore reloaded = store();
        assertEquals(9_876, reloaded.bestScore(GameMode.MARATHON));
        assertEquals("9,876", reloaded.bestText(GameMode.MARATHON));
    }

    @Test
    void ultraUsesScoreAsItsRecord() {
        HighScoreStore store = store();
        assertTrue(store.submit(result(GameMode.ULTRA, 500, 3, 10_000)));
        assertTrue(store.submit(result(GameMode.ULTRA, 700, 5, 20_000)));
        assertFalse(store.submit(result(GameMode.ULTRA, 700, 5, 20_000)));
        assertEquals(700, store.bestScore(GameMode.ULTRA));
        assertEquals(0, store.bestTimeMs(GameMode.ULTRA));
    }

    @Test
    void sprintOnlyRecordsCompletedRunsAndKeepsTheFastestTime() {
        HighScoreStore store = store();
        assertFalse(store.submit(result(GameMode.SPRINT, 5_000, 39, 30_000)),
                "a top-out before 40 lines is not a Sprint record");
        assertEquals(0, store.bestTimeMs(GameMode.SPRINT));

        assertTrue(store.submit(result(GameMode.SPRINT, 5_000, 40, 95_000)));
        assertEquals(95_000, store.bestTimeMs(GameMode.SPRINT));
        assertFalse(store.submit(result(GameMode.SPRINT, 9_999, 40, 95_000)));
        assertTrue(store.submit(result(GameMode.SPRINT, 1, 40, 83_000)));
        assertEquals(83_000, store.bestTimeMs(GameMode.SPRINT));
        assertEquals("01:23", store.bestText(GameMode.SPRINT));

        HighScoreStore reloaded = store();
        assertEquals(83_000, reloaded.bestTimeMs(GameMode.SPRINT));
    }

    @Test
    void zeroScoreIsNeverARecord() {
        HighScoreStore store = store();
        assertFalse(store.submit(result(GameMode.MARATHON, 0, 0, 10_000)));
        assertFalse(store.submit(result(GameMode.ULTRA, 0, 0, 10_000)));
    }

    @Test
    void aBrokenFileIsTreatedAsEmpty() throws IOException {
        Path file = directory.resolve("highscores.properties");
        Files.writeString(file, "this is not a properties file\n\u0000");
        HighScoreStore store = new HighScoreStore(file);
        assertEquals(0, store.bestScore(GameMode.MARATHON));
        assertEquals(0, store.bestTimeMs(GameMode.SPRINT));
    }

    @Test
    void formatTimeUsesMinutesAndSeconds() {
        assertEquals("00:00", HighScoreStore.formatTime(0));
        assertEquals("00:09", HighScoreStore.formatTime(9_999));
        assertEquals("01:23", HighScoreStore.formatTime(83_000));
        assertEquals("10:05", HighScoreStore.formatTime(605_000));
    }
}
