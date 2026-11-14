package tetris.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A falling tetromino: its shape, rotation and position on the board. Pieces
 * are immutable; moving or rotating returns a new instance.
 */
public final class Piece {

    private final Tetromino type;
    private final int rotation;
    private final int x;
    private final int y;

    public Piece(Tetromino type, int rotation, int x, int y) {
        this.type = Objects.requireNonNull(type, "type");
        this.rotation = Math.floorMod(rotation, 4);
        this.x = x;
        this.y = y;
    }

    /** A new piece at the standard spawn position. */
    public static Piece spawn(Tetromino type) {
        return new Piece(type, 0, GameEngine.SPAWN_X, 0);
    }

    public Tetromino type() {
        return type;
    }

    public int rotation() {
        return rotation;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Piece moved(int dx, int dy) {
        return new Piece(type, rotation, x + dx, y + dy);
    }

    public Piece rotated(boolean clockwise) {
        return new Piece(type, clockwise ? rotation + 1 : rotation + 3, x, y);
    }

    /** The four board cells occupied by this piece. */
    public List<Cell> cells() {
        List<Cell> shape = type.cells(rotation);
        List<Cell> cells = new ArrayList<>(shape.size());
        for (Cell cell : shape) {
            cells.add(new Cell(x + cell.x(), y + cell.y()));
        }
        return List.copyOf(cells);
    }
}
