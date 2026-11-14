package tetris.game;

/** A final snapshot of a run, used by the summary screen and score storage. */
public record GameResult(GameMode mode, int score, int lines, int level, long elapsedMs,
                         int piecesPlaced, int tetrises, int maxCombo) {

    public double piecesPerSecond() {
        if (elapsedMs <= 0) {
            return 0;
        }
        return piecesPlaced * 1000.0 / elapsedMs;
    }
}
