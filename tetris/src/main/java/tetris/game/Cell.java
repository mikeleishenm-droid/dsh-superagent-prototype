package tetris.game;

/**
 * An integer cell coordinate. Used both for absolute board cells and for
 * offsets inside a tetromino's rotation matrix.
 */
public record Cell(int x, int y) {
}
