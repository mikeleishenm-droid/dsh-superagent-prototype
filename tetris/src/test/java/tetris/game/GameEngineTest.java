package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineTest {

    private static final long SEED = 20261114L;

    private static GameEngine newGame() {
        GameEngine engine = new GameEngine(SEED);
        engine.startGame(GameMode.MARATHON);
        return engine;
    }

    @Test
    void startsInTheMenuWithoutAPiece() {
        GameEngine engine = new GameEngine(SEED);
        assertEquals(GameState.MENU, engine.state());
        assertNull(engine.mode());
        assertNull(engine.current());
        assertEquals(0, engine.board().filledCellCount());
        engine.update(10_000);
        assertEquals(GameState.MENU, engine.state());
    }

    @Test
    void startingAModeSpawnsAPieceAndResetsTheHud() {
        for (GameMode mode : GameMode.values()) {
            GameEngine engine = new GameEngine(SEED);
            engine.startGame(mode);
            assertEquals(GameState.RUNNING, engine.state());
            assertEquals(mode, engine.mode());
            assertNotNull(engine.current());
            assertEquals(0, engine.score());
            assertEquals(0, engine.lines());
            assertEquals(1, engine.level());
            assertEquals(0, engine.combo());
            assertEquals(0, engine.piecesPlaced());
            assertFalse(engine.board().collides(engine.current()));
            assertEquals(3, engine.nextPieces().size());
        }
    }

    @Test
    void gravityMovesThePieceDown() {
        GameEngine engine = newGame();
        int startY = engine.current().y();
        engine.update(engine.dropIntervalMs() + 1);
        assertEquals(startY + 1, engine.current().y());
        assertEquals(0, engine.score());
    }

    @Test
    void softDropMovesDownAndScores() {
        GameEngine engine = newGame();
        int startY = engine.current().y();
        assertTrue(engine.softDrop());
        assertEquals(startY + 1, engine.current().y());
        assertEquals(1, engine.score());
    }

    @Test
    void hardDropLocksFourCellsAndSpawnsTheNextPiece() {
        GameEngine engine = newGame();
        assertTrue(engine.hardDrop());
        assertEquals(4, engine.board().filledCellCount());
        assertNotNull(engine.current());
        assertEquals(1, engine.piecesPlaced());
        assertTrue(engine.score() >= 2);
        assertFalse(engine.holdUsed());
        assertEquals(GameState.RUNNING, engine.state());
    }

    @Test
    void movementStopsAtBothWalls() {
        GameEngine engine = newGame();
        int guard = 0;
        while (engine.moveLeft() && guard++ < 20) {
            // keep moving
        }
        assertTrue(engine.current().cells().stream().anyMatch(cell -> cell.x() == 0));
        assertTrue(engine.current().cells().stream().allMatch(cell -> cell.x() >= 0));

        guard = 0;
        while (engine.moveRight() && guard++ < 20) {
            // keep moving
        }
        assertTrue(engine.current().cells().stream().anyMatch(cell -> cell.x() == 9));
        assertTrue(engine.current().cells().stream().allMatch(cell -> cell.x() < Board.WIDTH));
    }

    @Test
    void rotationNearAWallKeepsThePieceOnTheBoard() {
        GameEngine engine = newGame();
        while (engine.moveLeft()) {
            // push against the wall
        }
        engine.rotateClockwise();
        assertFalse(engine.board().collides(engine.current()));
        assertTrue(engine.current().cells().stream().allMatch(cell -> cell.x() >= 0));

        while (engine.moveRight()) {
            // push against the other wall
        }
        engine.rotateCounterClockwise();
        assertFalse(engine.board().collides(engine.current()));
        assertTrue(engine.current().cells().stream().allMatch(cell -> cell.x() < Board.WIDTH));
    }

    @Test
    void holdSwapsPiecesOnlyOncePerDrop() {
        GameEngine engine = newGame();
        Tetromino first = engine.current().type();
        Tetromino queued = engine.nextPieces().get(0);

        assertTrue(engine.hold());
        assertEquals(first, engine.held());
        assertEquals(queued, engine.current().type());
        assertTrue(engine.holdUsed());
        assertFalse(engine.hold());

        assertTrue(engine.hardDrop());
        assertFalse(engine.holdUsed());
        Tetromino second = engine.current().type();
        assertTrue(engine.hold());
        assertEquals(second, engine.held());
        assertEquals(first, engine.current().type());
    }

    @Test
    void lockDelayGivesThePlayerTimeToSlide() {
        GameEngine engine = newGame();
        int guard = 0;
        while (engine.softDrop() && guard++ < 40) {
            // drop to the floor
        }
        assertTrue(engine.isGrounded());

        assertTrue(engine.moveLeft() || engine.moveRight());
        engine.update(GameEngine.LOCK_DELAY_MS - 1);
        assertEquals(0, engine.board().filledCellCount(), "piece must not lock before the delay");

        engine.update(2);
        assertEquals(4, engine.board().filledCellCount(), "piece must lock after the delay");
    }

    @Test
    void clearingLinesScoresAndCounts() {
        GameEngine engine = newGame();
        TestBoards.fillRow(engine.board(), Board.HEIGHT - 2);
        TestBoards.fillRow(engine.board(), Board.HEIGHT - 1);

        assertTrue(engine.hardDrop());

        assertEquals(2, engine.lines());
        assertEquals(1, engine.level());
        assertTrue(engine.score() >= GameEngine.lineClearScore(2, 1));
        assertEquals(4, engine.board().filledCellCount(), "only the locked piece remains");
        assertEquals(1, engine.combo());
        assertEquals(1, engine.maxCombo());
    }

    @Test
    void tenClearedLinesRaiseTheLevel() {
        GameEngine engine = newGame();
        for (int i = 0; i < 10; i++) {
            TestBoards.fillRow(engine.board(), Board.HEIGHT - 1);
            assertTrue(engine.hardDrop(), "iteration " + i);
        }
        assertEquals(10, engine.lines());
        assertEquals(2, engine.level());
        assertTrue(engine.dropIntervalMs() < 800);
    }

    @Test
    void comboGrowsOnConsecutiveClearsAndResetsAfterAGap() {
        GameEngine engine = newGame();
        for (int i = 0; i < 3; i++) {
            TestBoards.fillRow(engine.board(), Board.HEIGHT - 1);
            assertTrue(engine.hardDrop());
            assertEquals(i + 1, engine.combo());
        }
        assertEquals(3, engine.maxCombo());

        assertTrue(engine.hardDrop()); // no filled row this time
        assertEquals(0, engine.combo());
        assertEquals(3, engine.maxCombo());
    }

    @Test
    void backToBackTetrisesAreScoredAndReported() {
        List<ClearResult> results = new ArrayList<>();
        GameEngine engine = newGame();
        engine.setListener(new GameListener() {
            @Override
            public void onLinesCleared(ClearResult result) {
                results.add(result);
            }
        });

        for (int i = 0; i < 4; i++) {
            TestBoards.fillRow(engine.board(), Board.HEIGHT - 4 + i);
        }
        assertTrue(engine.hardDrop());
        for (int i = 0; i < 4; i++) {
            TestBoards.fillRow(engine.board(), Board.HEIGHT - 4 + i);
        }
        assertTrue(engine.hardDrop());

        assertEquals(2, results.size());
        assertTrue(results.get(0).tetris());
        assertFalse(results.get(0).backToBack());
        assertEquals(800, results.get(0).points());
        assertTrue(results.get(1).tetris());
        assertTrue(results.get(1).backToBack());
        assertEquals(1250, results.get(1).points(), "1.5x tetris plus the combo bonus");
        assertEquals(2, engine.tetrises());
        assertEquals(8, engine.lines());
    }

    @Test
    void sprintFinishesWhenFortyLinesAreCleared() {
        GameEngine engine = new GameEngine(SEED);
        engine.startGame(GameMode.SPRINT);
        for (int round = 0; round < 10; round++) {
            for (int row = 0; row < 4; row++) {
                TestBoards.fillRow(engine.board(), Board.HEIGHT - 4 + row);
            }
            assertTrue(engine.hardDrop(), "round " + round);
        }
        assertEquals(GameState.FINISHED, engine.state());
        assertEquals(FinishReason.TARGET_REACHED, engine.finishReason());
        assertEquals(GameMode.SPRINT, engine.mode());
        assertEquals(40, engine.lines());
        assertTrue(engine.elapsedMs() >= 0);
    }

    @Test
    void ultraFinishesWhenTheClockRunsOut() {
        GameEngine engine = new GameEngine(SEED);
        engine.startGame(GameMode.ULTRA);
        engine.update(30_000);
        assertEquals(90_000, engine.remainingMs());
        assertEquals(GameState.RUNNING, engine.state());

        engine.update(90_000);
        assertEquals(GameState.FINISHED, engine.state());
        assertEquals(FinishReason.TIME_UP, engine.finishReason());
        assertEquals(0, engine.remainingMs());
        assertEquals(120_000, engine.elapsedMs());
    }

    @Test
    void pauseStopsTimeAndInput() {
        GameEngine engine = newGame();
        assertTrue(engine.togglePause());
        assertEquals(GameState.PAUSED, engine.state());
        assertFalse(engine.moveLeft());
        assertFalse(engine.hardDrop());
        long elapsed = engine.elapsedMs();
        engine.update(10_000);
        assertEquals(elapsed, engine.elapsedMs());
        assertEquals(0, engine.board().filledCellCount());
        assertTrue(engine.togglePause());
        assertEquals(GameState.RUNNING, engine.state());
        assertTrue(engine.softDrop());
    }

    @Test
    void stackedPiecesEventuallyEndTheRun() {
        GameEngine engine = newGame();
        int drops = 0;
        while (engine.state() != GameState.FINISHED && drops < 100) {
            engine.hardDrop();
            drops++;
        }
        assertEquals(GameState.FINISHED, engine.state());
        assertEquals(FinishReason.GAME_OVER, engine.finishReason());
        assertTrue(drops < 100, "game over should happen after " + drops + " drops");
        assertFalse(engine.moveLeft());
        assertFalse(engine.hardDrop());
        assertFalse(engine.rotateClockwise());
    }

    @Test
    void restartKeepsTheModeAndStartsFresh() {
        GameEngine engine = new GameEngine(SEED);
        engine.startGame(GameMode.SPRINT);
        int drops = 0;
        while (engine.state() != GameState.FINISHED && drops++ < 100) {
            engine.hardDrop();
        }
        assertEquals(GameState.FINISHED, engine.state());
        engine.restart();
        assertEquals(GameState.RUNNING, engine.state());
        assertEquals(GameMode.SPRINT, engine.mode());
        assertEquals(0, engine.score());
        assertEquals(0, engine.lines());
        assertEquals(1, engine.level());
        assertEquals(0, engine.board().filledCellCount());
        assertNotNull(engine.current());
    }

    @Test
    void returnToMenuClearsTheRun() {
        GameEngine engine = newGame();
        assertTrue(engine.hardDrop());
        engine.returnToMenu();
        assertEquals(GameState.MENU, engine.state());
        assertNull(engine.mode());
        assertNull(engine.current());
        assertEquals(0, engine.board().filledCellCount());
        assertEquals(0, engine.score());
        engine.restart();
        assertEquals(GameState.MENU, engine.state(), "restart without a mode is a no-op");
    }

    @Test
    void ghostLandsWhereHardDropWouldPutThePiece() {
        GameEngine engine = newGame();
        int ghostY = engine.ghostY();
        assertTrue(ghostY >= engine.current().y());
        Piece landing = engine.current().moved(0, ghostY - engine.current().y());
        assertFalse(engine.board().collides(landing));
        assertTrue(engine.board().collides(landing.moved(0, 1)));
    }

    @Test
    void resultSnapshotContainsRunStats() {
        GameEngine engine = newGame();
        engine.update(1_000);
        engine.hardDrop();
        GameResult result = engine.result();
        assertEquals(GameMode.MARATHON, result.mode());
        assertEquals(engine.score(), result.score());
        assertEquals(1, result.piecesPlaced());
        assertEquals(0, result.lines());
        assertEquals(1, result.level());
        assertTrue(result.piecesPerSecond() > 0);
    }

    @Test
    void lineClearScoreFollowsTheClassicTable() {
        assertEquals(0, GameEngine.lineClearScore(0, 1));
        assertEquals(100, GameEngine.lineClearScore(1, 1));
        assertEquals(300, GameEngine.lineClearScore(2, 1));
        assertEquals(500, GameEngine.lineClearScore(3, 1));
        assertEquals(800, GameEngine.lineClearScore(4, 1));
        assertEquals(1600, GameEngine.lineClearScore(4, 2));
        assertTrue(GameEngine.lineClearScore(5, 1) > 800);
    }

    @Test
    void nextPiecesAreShownInSpawnOrderAndStayFilled() {
        GameEngine engine = newGame();
        List<Tetromino> preview = engine.nextPieces();
        assertEquals(3, preview.size());
        assertSame(preview.get(0), engine.nextPieces().get(0));

        for (int i = 0; i < 6 && engine.state() != GameState.FINISHED; i++) {
            assertEquals(3, engine.nextPieces().size());
            engine.hardDrop();
        }
    }

    @Test
    void modeDescriptorsMatchTheRules() {
        assertEquals(0, GameMode.MARATHON.targetLines());
        assertFalse(GameMode.MARATHON.isTimed());
        assertEquals(40, GameMode.SPRINT.targetLines());
        assertEquals(120_000, GameMode.ULTRA.timeLimitMs());
        assertTrue(GameMode.ULTRA.isTimed());
        assertNotEquals(GameMode.SPRINT, GameMode.ULTRA);
    }
}
