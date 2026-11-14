package tetris.game;

/**
 * Receives notable events from {@link GameEngine}. All callbacks run on the
 * thread that drives the engine.
 */
public interface GameListener {

    /** Called after rows were cleared, with the combo/back-to-back outcome. */
    default void onLinesCleared(ClearResult result) {
    }

    /** Called once when the run ends (top-out, target reached or time is up). */
    default void onRunFinished(FinishReason reason) {
    }
}
