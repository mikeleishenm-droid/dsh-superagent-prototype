package tetris.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TetrominoTest {

    @Test
    void everyRotationHasFourCells() {
        for (Tetromino type : Tetromino.values()) {
            for (int rotation = 0; rotation < 4; rotation++) {
                assertEquals(4, type.cells(rotation).size(), type + " rotation " + rotation);
            }
        }
    }

    @Test
    void rotationsStayInsideTheBoundingBox() {
        for (Tetromino type : Tetromino.values()) {
            for (int rotation = 0; rotation < 4; rotation++) {
                for (Cell cell : type.cells(rotation)) {
                    assertTrue(cell.x() >= 0 && cell.x() < 4, type + " x=" + cell.x());
                    assertTrue(cell.y() >= 0 && cell.y() < 4, type + " y=" + cell.y());
                }
            }
        }
    }

    @Test
    void rotatingFourTimesReturnsToSpawnShape() {
        for (Tetromino type : Tetromino.values()) {
            Piece piece = Piece.spawn(type);
            Set<Cell> spawn = new HashSet<>(piece.cells());
            for (int i = 0; i < 4; i++) {
                piece = piece.rotated(true);
            }
            assertEquals(spawn, new HashSet<>(piece.cells()));
        }
    }

    @Test
    void oPieceKeepsItsShapeAndPositionWhenRotated() {
        List<Cell> spawn = Tetromino.O.cells(0);
        for (int rotation = 1; rotation < 4; rotation++) {
            assertEquals(spawn, Tetromino.O.cells(rotation));
        }
    }

    @Test
    void shapesMatchTheSuperRotationSystem() {
        assertEquals(
                Set.of(new Cell(0, 1), new Cell(1, 1), new Cell(2, 1), new Cell(3, 1)),
                new HashSet<>(Tetromino.I.cells(0)));
        assertEquals(
                Set.of(new Cell(2, 0), new Cell(2, 1), new Cell(2, 2), new Cell(2, 3)),
                new HashSet<>(Tetromino.I.cells(1)));
        assertEquals(
                Set.of(new Cell(0, 2), new Cell(1, 2), new Cell(2, 2), new Cell(3, 2)),
                new HashSet<>(Tetromino.I.cells(2)));
        assertEquals(
                Set.of(new Cell(1, 0), new Cell(1, 1), new Cell(1, 2), new Cell(1, 3)),
                new HashSet<>(Tetromino.I.cells(3)));

        // T pointing right: a vertical bar plus one cell on the right.
        assertEquals(
                Set.of(new Cell(1, 0), new Cell(1, 1), new Cell(1, 2), new Cell(2, 1)),
                new HashSet<>(Tetromino.T.cells(1)));
    }

    @Test
    void wallKickTablesStartAtTheUnchangedPosition() {
        for (Tetromino type : Tetromino.values()) {
            for (int from = 0; from < 4; from++) {
                for (int to = 0; to < 4; to++) {
                    boolean quarterTurn = to == (from + 1) % 4 || to == (from + 3) % 4;
                    if (!quarterTurn) {
                        continue;
                    }
                    List<Cell> kicks = type.kicks(from, to);
                    assertFalse(kicks.isEmpty(), type + " kick " + from + "->" + to);
                    assertEquals(new Cell(0, 0), kicks.get(0), type + " kick " + from + "->" + to);
                    if (type == Tetromino.O) {
                        assertEquals(1, kicks.size());
                    } else {
                        assertEquals(5, kicks.size());
                    }
                }
            }
        }
    }

    @Test
    void nonZeroRotationChangesTheShapeForNonSquarePieces() {
        assertNotEquals(Tetromino.I.cells(0), Tetromino.I.cells(1));
        assertNotEquals(Tetromino.T.cells(0), Tetromino.T.cells(1));
    }
}
