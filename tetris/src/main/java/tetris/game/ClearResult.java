package tetris.game;

/**
 * The outcome of one line clear, including the combo and back-to-back bonuses
 * that were applied.
 *
 * @param lines      number of rows removed
 * @param points     total points awarded for this clear
 * @param combo      number of consecutive line-clearing placements (1 = first)
 * @param backToBack whether this tetris followed another tetris
 */
public record ClearResult(int lines, int points, int combo, boolean backToBack) {

    public boolean tetris() {
        return lines >= 4;
    }
}
