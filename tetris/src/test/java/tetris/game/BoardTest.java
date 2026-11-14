package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    @Test
    void emptyBoardAcceptsAFreshPiece() {
        Board board = new Board();
        assertFalse(board.collides(Piece.spawn(Tetromino.T)));
        assertEquals(0, board.filledCellCount());
    }

    @Test
    void wallsAndFloorCollide() {
        Board board = new Board();
        assertTrue(board.collides(new Piece(Tetromino.T, 0, -1, 0)));
        assertTrue(board.collides(new Piece(Tetromino.T, 0, 8, 0)));
        assertTrue(board.collides(new Piece(Tetromino.T, 0, 3, Board.HEIGHT - 1)));
        assertFalse(board.collides(new Piece(Tetromino.T, 0, 3, Board.HEIGHT - 2)));
    }

    @Test
    void cellsAboveTheBoardAreAllowed() {
        Board board = new Board();
        assertFalse(board.collides(new Piece(Tetromino.I, 0, 3, -1)));
    }

    @Test
    void lockedCellsBlockPieces() {
        Board board = new Board();
        board.lock(new Piece(Tetromino.O, 0, 0, 0));
        assertTrue(board.collides(new Piece(Tetromino.I, 0, 0, 0)));
        assertTrue(board.collides(new Piece(Tetromino.O, 0, 0, 0)));
        assertFalse(board.collides(new Piece(Tetromino.I, 0, 4, 0)));
    }

    @Test
    void lockIgnoresCellsAboveTheBoard() {
        Board board = new Board();
        board.lock(new Piece(Tetromino.I, 0, 3, -2));
        assertEquals(0, board.filledCellCount());
    }

    @Test
    void clearingAFullRowRemovesIt() {
        Board board = new Board();
        TestBoards.fillRow(board, Board.HEIGHT - 1);
        assertEquals(Board.WIDTH, board.filledCellCount());
        assertTrue(board.isRowFull(Board.HEIGHT - 1));

        List<Integer> cleared = board.clearFullLines();

        assertEquals(List.of(Board.HEIGHT - 1), cleared);
        assertEquals(0, board.filledCellCount());
    }

    @Test
    void clearingRowsDropsEverythingAboveThem() {
        Board board = new Board();
        // Marker at row 17, columns 0-3.
        board.lock(new Piece(Tetromino.I, 0, 0, 16));
        TestBoards.fillRow(board, Board.HEIGHT - 2);
        TestBoards.fillRow(board, Board.HEIGHT - 1);

        List<Integer> cleared = board.clearFullLines();

        assertEquals(2, cleared.size());
        assertEquals(4, board.filledCellCount());
        for (int x = 0; x < 4; x++) {
            assertEquals(Tetromino.I, board.cellAt(x, Board.HEIGHT - 1));
        }
        assertNull(board.cellAt(0, Board.HEIGHT - 2));
    }

    @Test
    void nonAdjacentFullRowsCollapseCorrectly() {
        Board board = new Board();
        // Marker at row 18, columns 0-3.
        board.lock(new Piece(Tetromino.I, 0, 0, 17));
        TestBoards.fillRow(board, 17);
        TestBoards.fillRow(board, 19);

        List<Integer> cleared = board.clearFullLines();

        assertEquals(2, cleared.size());
        for (int x = 0; x < 4; x++) {
            assertEquals(Tetromino.I, board.cellAt(x, 19));
        }
        assertNull(board.cellAt(0, 17));
        assertNull(board.cellAt(0, 18));
    }

    @Test
    void noFullRowsMeansNothingIsRemoved() {
        Board board = new Board();
        board.lock(new Piece(Tetromino.O, 0, 0, 0));
        List<Integer> cleared = board.clearFullLines();
        assertTrue(cleared.isEmpty());
        assertEquals(4, board.filledCellCount());
    }

    @Test
    void resetEmptiesTheBoard() {
        Board board = new Board();
        board.lock(new Piece(Tetromino.O, 0, 0, 0));
        board.reset();
        assertEquals(0, board.filledCellCount());
        assertNull(board.cellAt(0, 0));
    }

    @Test
    void cellAtReturnsTheLockedType() {
        Board board = new Board();
        board.lock(new Piece(Tetromino.T, 0, 3, 0));
        assertEquals(Tetromino.T, board.cellAt(4, 0));
        assertEquals(Tetromino.T, board.cellAt(3, 1));
        assertNull(board.cellAt(0, 0));
    }

    @Test
    void filledRowIsDetected() {
        Board board = new Board();
        TestBoards.fillRow(board, 0);
        HashSet<Tetromino> types = new HashSet<>();
        for (int x = 0; x < Board.WIDTH; x++) {
            types.add(board.cellAt(x, 0));
        }
        assertEquals(1, types.size());
    }
}
