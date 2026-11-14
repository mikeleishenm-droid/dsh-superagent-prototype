package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestBoards {

    private TestBoards() {
    }

    /** Fills one row with three horizontal I pieces (columns 0-3, 3-6, 6-9). */
    static void fillRow(Board board, int row) {
        for (int x : new int[]{0, 3, 6}) {
            board.lock(new Piece(Tetromino.I, 0, x, row - 1));
        }
    }
}
