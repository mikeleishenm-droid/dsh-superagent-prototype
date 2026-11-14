package tetris.game;

import java.util.List;

/**
 * Receives notable events from {@link GameEngine}. All callbacks run on the
 * thread that drives the engine.
 */
public interface GameListener {

    /** Called after one or more rows were cleared. {@code rows} are board row indices. */
    default void onLinesCleared(int count, List<Integer> rows) {
    }

    /** Called once when the board tops out. */
    default void onGameOver() {
    }
}
