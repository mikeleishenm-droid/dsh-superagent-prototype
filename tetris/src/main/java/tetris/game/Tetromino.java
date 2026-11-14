package tetris.game;

import java.util.ArrayList;
import java.util.List;

/**
 * The seven tetromino shapes and their four rotation states.
 *
 * <p>Rotation follows the Super Rotation System (SRS): {@code J}, {@code L},
 * {@code S}, {@code T} and {@code Z} rotate inside a 3x3 box, {@code I} rotates
 * inside a 4x4 box, and {@code O} does not move when rotated. The wall kick
 * offsets in {@link #kicks(int, int)} are the standard SRS tables, converted to
 * screen coordinates (positive y points down).</p>
 */
public enum Tetromino {

    I(KickSet.I, new String[]{
            "....",
            "XXXX",
            "....",
            "...."
    }),
    O(KickSet.NONE, new String[]{
            ".XX.",
            ".XX.",
            "....",
            "...."
    }),
    T(KickSet.JLSTZ, new String[]{
            ".X.",
            "XXX",
            "..."
    }),
    S(KickSet.JLSTZ, new String[]{
            ".XX",
            "XX.",
            "..."
    }),
    Z(KickSet.JLSTZ, new String[]{
            "XX.",
            ".XX",
            "..."
    }),
    J(KickSet.JLSTZ, new String[]{
            "X..",
            "XXX",
            "..."
    }),
    L(KickSet.JLSTZ, new String[]{
            "..X",
            "XXX",
            "..."
    });

    private enum KickSet { JLSTZ, I, NONE }

    private static final List<Cell> NO_KICK = List.of(new Cell(0, 0));

    // Standard SRS wall kicks, y already converted from "up" to "down".
    private static final List<Cell> JLSTZ_0_R = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(-1, -1), new Cell(0, 2), new Cell(-1, 2));
    private static final List<Cell> JLSTZ_R_0 = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(1, 1), new Cell(0, -2), new Cell(1, -2));
    private static final List<Cell> JLSTZ_R_2 = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(1, 1), new Cell(0, -2), new Cell(1, -2));
    private static final List<Cell> JLSTZ_2_R = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(-1, -1), new Cell(0, 2), new Cell(-1, 2));
    private static final List<Cell> JLSTZ_2_L = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(1, -1), new Cell(0, 2), new Cell(1, 2));
    private static final List<Cell> JLSTZ_L_2 = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(-1, 1), new Cell(0, -2), new Cell(-1, -2));
    private static final List<Cell> JLSTZ_L_0 = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(-1, 1), new Cell(0, -2), new Cell(-1, -2));
    private static final List<Cell> JLSTZ_0_L = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(1, -1), new Cell(0, 2), new Cell(1, 2));

    private static final List<Cell> I_0_R = List.of(
            new Cell(0, 0), new Cell(-2, 0), new Cell(1, 0), new Cell(-2, 1), new Cell(1, -2));
    private static final List<Cell> I_R_0 = List.of(
            new Cell(0, 0), new Cell(2, 0), new Cell(-1, 0), new Cell(2, -1), new Cell(-1, 2));
    private static final List<Cell> I_R_2 = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(2, 0), new Cell(-1, -2), new Cell(2, 1));
    private static final List<Cell> I_2_R = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(-2, 0), new Cell(1, 2), new Cell(-2, -1));
    private static final List<Cell> I_2_L = List.of(
            new Cell(0, 0), new Cell(2, 0), new Cell(-1, 0), new Cell(2, -1), new Cell(-1, 2));
    private static final List<Cell> I_L_2 = List.of(
            new Cell(0, 0), new Cell(-2, 0), new Cell(1, 0), new Cell(-2, 1), new Cell(1, -2));
    private static final List<Cell> I_L_0 = List.of(
            new Cell(0, 0), new Cell(1, 0), new Cell(-2, 0), new Cell(1, 2), new Cell(-2, -1));
    private static final List<Cell> I_0_L = List.of(
            new Cell(0, 0), new Cell(-1, 0), new Cell(2, 0), new Cell(-1, -2), new Cell(2, 1));

    private final KickSet kickSet;
    private final boolean[][][] states;

    Tetromino(KickSet kickSet, String[] rows) {
        this.kickSet = kickSet;
        int size = rows.length;
        boolean[][] spawn = new boolean[size][size];
        for (int row = 0; row < size; row++) {
            if (rows[row].length() != size) {
                throw new IllegalArgumentException("Non-square shape for " + name());
            }
            for (int column = 0; column < size; column++) {
                spawn[row][column] = rows[row].charAt(column) == 'X';
            }
        }
        states = new boolean[4][][];
        states[0] = spawn;
        if (kickSet == KickSet.NONE) {
            // O never moves when rotated: every state is identical.
            states[1] = spawn;
            states[2] = spawn;
            states[3] = spawn;
        } else {
            for (int rotation = 1; rotation < 4; rotation++) {
                states[rotation] = rotateClockwise(states[rotation - 1]);
            }
        }
    }

    /**
     * The four occupied cells of this shape in the given rotation, relative to
     * the top-left corner of its bounding box.
     */
    public List<Cell> cells(int rotation) {
        boolean[][] matrix = states[rotation & 3];
        List<Cell> cells = new ArrayList<>(4);
        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix[row].length; column++) {
                if (matrix[row][column]) {
                    cells.add(new Cell(column, row));
                }
            }
        }
        return List.copyOf(cells);
    }

    /** The SRS wall kicks to try when rotating from {@code from} to {@code to}. */
    public List<Cell> kicks(int from, int to) {
        int key = (from & 3) * 4 + (to & 3);
        if (kickSet == KickSet.NONE) {
            return NO_KICK;
        }
        if (kickSet == KickSet.I) {
            return switch (key) {
                case 1 -> I_0_R;
                case 4 -> I_R_0;
                case 6 -> I_R_2;
                case 9 -> I_2_R;
                case 11 -> I_2_L;
                case 14 -> I_L_2;
                case 12 -> I_L_0;
                case 3 -> I_0_L;
                default -> NO_KICK;
            };
        }
        return switch (key) {
            case 1 -> JLSTZ_0_R;
            case 4 -> JLSTZ_R_0;
            case 6 -> JLSTZ_R_2;
            case 9 -> JLSTZ_2_R;
            case 11 -> JLSTZ_2_L;
            case 14 -> JLSTZ_L_2;
            case 12 -> JLSTZ_L_0;
            case 3 -> JLSTZ_0_L;
            default -> NO_KICK;
        };
    }

    private static boolean[][] rotateClockwise(boolean[][] matrix) {
        int size = matrix.length;
        boolean[][] rotated = new boolean[size][size];
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                rotated[column][size - 1 - row] = matrix[row][column];
            }
        }
        return rotated;
    }
}
