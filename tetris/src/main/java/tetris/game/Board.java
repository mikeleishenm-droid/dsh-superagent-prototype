package tetris.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The playfield: a grid of locked cells. The board is deliberately independent
 * of timing and input so its rules can be tested in isolation.
 */
public final class Board {

    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;

    private final Tetromino[][] cells = new Tetromino[HEIGHT][WIDTH];

    /** The locked cell at (x, y), or {@code null} when empty. */
    public Tetromino cellAt(int x, int y) {
        return cells[y][x];
    }

    public boolean inside(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    /** True when any cell of {@code piece} is outside the board or occupied. */
    public boolean collides(Piece piece) {
        for (Cell cell : piece.cells()) {
            if (cell.x() < 0 || cell.x() >= WIDTH || cell.y() >= HEIGHT) {
                return true;
            }
            // Cells above the visible board are allowed; pieces can be kicked up.
            if (cell.y() >= 0 && cells[cell.y()][cell.x()] != null) {
                return true;
            }
        }
        return false;
    }

    /** Locks a piece into the board. Cells above the visible board are dropped. */
    public void lock(Piece piece) {
        for (Cell cell : piece.cells()) {
            if (inside(cell.x(), cell.y())) {
                cells[cell.y()][cell.x()] = piece.type();
            }
        }
    }

    /** Removes every full row, drops the rows above it and returns the cleared rows. */
    public List<Integer> clearFullLines() {
        List<Integer> cleared = new ArrayList<>(4);
        int writeRow = HEIGHT - 1;
        for (int readRow = HEIGHT - 1; readRow >= 0; readRow--) {
            if (isRowFull(readRow)) {
                cleared.add(readRow);
            } else {
                if (writeRow != readRow) {
                    System.arraycopy(cells[readRow], 0, cells[writeRow], 0, WIDTH);
                }
                writeRow--;
            }
        }
        for (int row = writeRow; row >= 0; row--) {
            Arrays.fill(cells[row], null);
        }
        return List.copyOf(cleared);
    }

    public boolean isRowFull(int row) {
        for (int column = 0; column < WIDTH; column++) {
            if (cells[row][column] == null) {
                return false;
            }
        }
        return true;
    }

    public void reset() {
        for (Tetromino[] row : cells) {
            Arrays.fill(row, null);
        }
    }

    /** Number of occupied cells; handy for tests and for debugging. */
    public int filledCellCount() {
        int count = 0;
        for (Tetromino[] row : cells) {
            for (Tetromino cell : row) {
                if (cell != null) {
                    count++;
                }
            }
        }
        return count;
    }
}
