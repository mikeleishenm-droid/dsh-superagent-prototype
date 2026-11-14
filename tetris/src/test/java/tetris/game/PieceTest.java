package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class PieceTest {

    @Test
    void spawnsAtTheTopOfTheBoard() {
        Piece piece = Piece.spawn(Tetromino.T);
        assertSame(Tetromino.T, piece.type());
        assertEquals(0, piece.rotation());
        assertEquals(GameEngine.SPAWN_X, piece.x());
        assertEquals(0, piece.y());
    }

    @Test
    void moveAndRotateReturnNewPieces() {
        Piece piece = Piece.spawn(Tetromino.L);
        Piece moved = piece.moved(2, 3);
        Piece rotated = piece.rotated(true);

        assertEquals(5, moved.x());
        assertEquals(3, moved.y());
        assertEquals(1, rotated.rotation());
        assertEquals(GameEngine.SPAWN_X, piece.x());
        assertEquals(0, piece.rotation());
    }

    @Test
    void rotationWrapsAround() {
        Piece piece = new Piece(Tetromino.S, 3, 0, 0);
        assertEquals(0, piece.rotated(true).rotation());
        assertEquals(2, piece.rotated(false).rotation());
        assertEquals(1, new Piece(Tetromino.S, 5, 0, 0).rotation());
    }

    @Test
    void cellsAreTranslatedToBoardCoordinates() {
        Piece piece = new Piece(Tetromino.O, 0, 4, 5);
        assertEquals(
                new HashSet<>(java.util.List.of(
                        new Cell(5, 5), new Cell(6, 5), new Cell(5, 6), new Cell(6, 6))),
                new HashSet<>(piece.cells()));
    }
}
