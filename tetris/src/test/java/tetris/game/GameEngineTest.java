package tetris.game;

import org.junit.jupiter.api.Test;

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

    @Test
    void newGameIsReadyToPlay() {
        GameEngine engine = new GameEngine(SEED);
        assertNotNull(engine.current());
        assertEquals(GameState.RUNNING, engine.state());
        assertEquals(0, engine.score());
        assertEquals(0, engine.lines());
        assertEquals(1, engine.level());
        assertTrue(engine.board().filledCellCount() == 0);
        assertEquals(3, engine.nextPieces().size());
        assertFalse(engine.board().collides(engine.current()));
        assertFalse(engine.isGrounded());
        assertNull(engine.held());
    }

    @Test
    void gravityMovesThePieceDown() {
        GameEngine engine = new GameEngine(SEED);
        int startY = engine.current().y();
        engine.update(engine.dropIntervalMs() + 1);
        assertEquals(startY + 1, engine.current().y());
        assertEquals(0, engine.score());
    }

    @Test
    void softDropMovesDownAndScores() {
        GameEngine engine = new GameEngine(SEED);
        int startY = engine.current().y();
        assertTrue(engine.softDrop());
        assertEquals(startY + 1, engine.current().y());
        assertEquals(1, engine.score());
    }

    @Test
    void hardDropLocksFourCellsAndSpawnsTheNextPiece() {
        GameEngine engine = new GameEngine(SEED);
        GameState state = engine.state();
        assertTrue(engine.hardDrop());
        assertEquals(4, engine.board().filledCellCount());
        assertNotNull(engine.current());
        assertNotEquals(GameState.GAME_OVER, engine.state());
        assertEquals(state, engine.state());
        assertTrue(engine.score() >= 2);
        assertFalse(engine.holdUsed());
    }

    @Test
    void movementStopsAtBothWalls() {
        GameEngine engine = new GameEngine(SEED);
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
        GameEngine engine = new GameEngine(SEED);
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
        GameEngine engine = new GameEngine(SEED);
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
        GameEngine engine = new GameEngine(SEED);
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
        GameEngine engine = new GameEngine(SEED);
        TestBoards.fillRow(engine.board(), Board.HEIGHT - 2);
        TestBoards.fillRow(engine.board(), Board.HEIGHT - 1);

        assertTrue(engine.hardDrop());

        assertEquals(2, engine.lines());
        assertEquals(1, engine.level());
        assertTrue(engine.score() >= GameEngine.lineClearScore(2, 1));
        assertEquals(4, engine.board().filledCellCount(), "only the locked piece remains");
    }

    @Test
    void tenClearedLinesRaiseTheLevel() {
        GameEngine engine = new GameEngine(SEED);
        for (int i = 0; i < 10; i++) {
            TestBoards.fillRow(engine.board(), Board.HEIGHT - 1);
            assertTrue(engine.hardDrop(), "iteration " + i);
        }
        assertEquals(10, engine.lines());
        assertEquals(2, engine.level());
        assertTrue(engine.dropIntervalMs() < 800);
    }

    @Test
    void stackingWithoutMovingEventuallyEndsTheGame() {
        GameEngine engine = new GameEngine(SEED);
        int drops = 0;
        while (engine.state() != GameState.GAME_OVER && drops < 100) {
            engine.hardDrop();
            drops++;
        }
        assertEquals(GameState.GAME_OVER, engine.state());
        assertTrue(drops < 100, "game over should happen after " + drops + " drops");
        assertFalse(engine.moveLeft());
        assertFalse(engine.hardDrop());
        assertFalse(engine.rotateClockwise());
    }

    @Test
    void pauseStopsTimeAndInput() {
        GameEngine engine = new GameEngine(SEED);
        assertTrue(engine.togglePause());
        assertEquals(GameState.PAUSED, engine.state());
        assertFalse(engine.moveLeft());
        assertFalse(engine.hardDrop());
        engine.update(10_000);
        assertEquals(0, engine.board().filledCellCount());
        assertTrue(engine.togglePause());
        assertEquals(GameState.RUNNING, engine.state());
        assertTrue(engine.softDrop());
    }

    @Test
    void restartAfterGameOverStartsFresh() {
        GameEngine engine = new GameEngine(SEED);
        int drops = 0;
        while (engine.state() != GameState.GAME_OVER && drops++ < 100) {
            engine.hardDrop();
        }
        assertEquals(GameState.GAME_OVER, engine.state());
        engine.restart();
        assertEquals(GameState.RUNNING, engine.state());
        assertEquals(0, engine.score());
        assertEquals(0, engine.lines());
        assertEquals(1, engine.level());
        assertEquals(0, engine.board().filledCellCount());
        assertNotNull(engine.current());
    }

    @Test
    void ghostLandsWhereHardDropWouldPutThePiece() {
        GameEngine engine = new GameEngine(SEED);
        int ghostY = engine.ghostY();
        assertTrue(ghostY >= engine.current().y());
        Piece landing = engine.current().moved(0, ghostY - engine.current().y());
        assertFalse(engine.board().collides(landing));
        assertTrue(engine.board().collides(landing.moved(0, 1)));
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
    void nextPiecesAreShownInSpawnOrder() {
        GameEngine engine = new GameEngine(SEED);
        List<Tetromino> preview = engine.nextPieces();
        assertEquals(3, preview.size());
        assertSame(preview.get(0), engine.nextPieces().get(0));
    }
}
