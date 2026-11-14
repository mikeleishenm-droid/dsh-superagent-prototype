package tetris.game;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The Tetris rules: gravity, locking, line clears, scoring, hold and level
 * progression. The engine has no dependency on Swing so it can be driven by a
 * test or by any UI.
 */
public final class GameEngine {

    /** Spawn column (left edge of the piece's bounding box). */
    public static final int SPAWN_X = 3;

    public static final long LOCK_DELAY_MS = 500;
    public static final long SOFT_DROP_INTERVAL_MS = 40;

    private static final int MAX_LOCK_RESETS = 15;
    private static final long BASE_DROP_INTERVAL_MS = 800;
    private static final long MIN_DROP_INTERVAL_MS = 80;
    private static final long LEVEL_SPEEDUP_MS = 70;
    private static final int QUEUE_SIZE = 5;
    private static final int PREVIEW_SIZE = 3;
    private static final int[] LINE_SCORES = {0, 100, 300, 500, 800};
    private static final int SOFT_DROP_POINTS = 1;
    private static final int HARD_DROP_POINTS = 2;

    private final Board board = new Board();
    private final BagRandomizer bag;
    private final Deque<Tetromino> nextQueue = new ArrayDeque<>();

    private GameListener listener = new GameListener() {
    };
    private GameState state = GameState.RUNNING;
    private Piece current;
    private Tetromino held;
    private boolean holdUsed;
    private int score;
    private int lines;
    private int level;
    private long gravityElapsedMs;
    private long lockElapsedMs;
    private int lockResets;

    public GameEngine() {
        this(new Random());
    }

    public GameEngine(long seed) {
        this(new Random(seed));
    }

    public GameEngine(Random random) {
        this.bag = new BagRandomizer(Objects.requireNonNull(random, "random"));
        restart();
    }

    public void setListener(GameListener listener) {
        this.listener = listener == null ? new GameListener() {
        } : listener;
    }

    public Board board() {
        return board;
    }

    public Piece current() {
        return current;
    }

    public Tetromino held() {
        return held;
    }

    public boolean holdUsed() {
        return holdUsed;
    }

    public GameState state() {
        return state;
    }

    public int score() {
        return score;
    }

    public int lines() {
        return lines;
    }

    public int level() {
        return level;
    }

    /** Rows cleared by the most recent lock, for UI feedback. */
    public int lastClearCount() {
        return lastClearCount;
    }

    private int lastClearCount;

    /** The next pieces in spawn order, for the preview panel. */
    public List<Tetromino> nextPieces() {
        return nextPieces(PREVIEW_SIZE);
    }

    public List<Tetromino> nextPieces(int count) {
        List<Tetromino> preview = new ArrayList<>(Math.min(count, nextQueue.size()));
        int index = 0;
        for (Tetromino type : nextQueue) {
            if (index++ >= count) {
                break;
            }
            preview.add(type);
        }
        return List.copyOf(preview);
    }

    /** Current gravity interval in milliseconds; smaller means faster. */
    public long dropIntervalMs() {
        long interval = BASE_DROP_INTERVAL_MS - (long) (level - 1) * LEVEL_SPEEDUP_MS;
        return Math.max(MIN_DROP_INTERVAL_MS, interval);
    }

    /** The row where the current piece would land if hard dropped. */
    public int ghostY() {
        if (current == null) {
            return 0;
        }
        Piece ghost = current;
        while (!board.collides(ghost.moved(0, 1))) {
            ghost = ghost.moved(0, 1);
        }
        return ghost.y();
    }

    public boolean isGrounded() {
        return current != null && board.collides(current.moved(0, 1));
    }

    /** Starts a brand-new game. */
    public void restart() {
        board.reset();
        nextQueue.clear();
        fillQueue();
        current = null;
        held = null;
        holdUsed = false;
        score = 0;
        lines = 0;
        level = 1;
        lastClearCount = 0;
        state = GameState.RUNNING;
        resetDropTimers();
        spawnNext();
    }

    /** Advances time. Handles gravity and the lock delay. */
    public void update(long deltaMs) {
        if (state != GameState.RUNNING || current == null || deltaMs <= 0) {
            return;
        }
        if (isGrounded()) {
            lockElapsedMs += deltaMs;
            if (lockElapsedMs >= LOCK_DELAY_MS) {
                lockCurrent();
            }
            return;
        }
        lockElapsedMs = 0;
        gravityElapsedMs += deltaMs;
        long interval = dropIntervalMs();
        while (gravityElapsedMs >= interval) {
            gravityElapsedMs -= interval;
            if (!moveDown(false)) {
                break;
            }
        }
    }

    public boolean moveLeft() {
        return moveHorizontally(-1);
    }

    public boolean moveRight() {
        return moveHorizontally(1);
    }

    private boolean moveHorizontally(int dx) {
        if (!canAct()) {
            return false;
        }
        Piece candidate = current.moved(dx, 0);
        if (board.collides(candidate)) {
            return false;
        }
        current = candidate;
        resetLockAfterAction();
        return true;
    }

    /** Moves down one row; awards soft-drop points when the player asks for it. */
    public boolean softDrop() {
        if (!canAct() || !moveDown(true)) {
            return false;
        }
        return true;
    }

    private boolean moveDown(boolean byPlayer) {
        Piece candidate = current.moved(0, 1);
        if (board.collides(candidate)) {
            return false;
        }
        current = candidate;
        if (byPlayer) {
            score += SOFT_DROP_POINTS;
        }
        return true;
    }

    public boolean rotateClockwise() {
        return rotate(true);
    }

    public boolean rotateCounterClockwise() {
        return rotate(false);
    }

    private boolean rotate(boolean clockwise) {
        if (!canAct()) {
            return false;
        }
        Piece rotated = current.rotated(clockwise);
        for (Cell kick : current.type().kicks(current.rotation(), rotated.rotation())) {
            Piece candidate = rotated.moved(kick.x(), kick.y());
            if (!board.collides(candidate)) {
                current = candidate;
                resetLockAfterAction();
                return true;
            }
        }
        return false;
    }

    /** Drops the piece instantly and locks it. */
    public boolean hardDrop() {
        if (!canAct()) {
            return false;
        }
        int distance = 0;
        while (!board.collides(current.moved(0, 1))) {
            current = current.moved(0, 1);
            distance++;
        }
        score += distance * HARD_DROP_POINTS;
        lockCurrent();
        return true;
    }

    /** Stores the current piece, or swaps it with the stored one. */
    public boolean hold() {
        if (!canAct() || holdUsed) {
            return false;
        }
        Tetromino outgoing = current.type();
        if (held == null) {
            held = outgoing;
            spawnNext();
        } else {
            Tetromino incoming = held;
            held = outgoing;
            spawnPiece(incoming);
        }
        holdUsed = true;
        return true;
    }

    /** Pauses a running game or resumes a paused one. Returns false on game over. */
    public boolean togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
            return true;
        }
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
            return true;
        }
        return false;
    }

    public static int lineClearScore(int count, int level) {
        if (count <= 0) {
            return 0;
        }
        int base;
        if (count < LINE_SCORES.length) {
            base = LINE_SCORES[count];
        } else {
            base = LINE_SCORES[LINE_SCORES.length - 1] + (count - LINE_SCORES.length + 1) * 100;
        }
        return base * Math.max(1, level);
    }

    private void lockCurrent() {
        board.lock(current);
        List<Integer> cleared = board.clearFullLines();
        if (!cleared.isEmpty()) {
            int count = cleared.size();
            lines += count;
            score += lineClearScore(count, level);
            level = lines / 10 + 1;
            lastClearCount = count;
            listener.onLinesCleared(count, cleared);
        } else {
            lastClearCount = 0;
        }
        spawnNext();
    }

    private void spawnNext() {
        Tetromino type = nextQueue.pollFirst();
        if (type == null) {
            fillQueue();
            type = nextQueue.pollFirst();
        }
        fillQueue();
        spawnPiece(type);
        holdUsed = false;
    }

    private void spawnPiece(Tetromino type) {
        current = Piece.spawn(type);
        resetDropTimers();
        if (board.collides(current)) {
            state = GameState.GAME_OVER;
            listener.onGameOver();
        }
    }

    private void resetDropTimers() {
        gravityElapsedMs = 0;
        lockElapsedMs = 0;
        lockResets = 0;
    }

    private void resetLockAfterAction() {
        if (isGrounded()) {
            if (lockResets < MAX_LOCK_RESETS) {
                lockElapsedMs = 0;
                lockResets++;
            }
        } else {
            lockElapsedMs = 0;
        }
    }

    private void fillQueue() {
        while (nextQueue.size() < QUEUE_SIZE) {
            nextQueue.addAll(bag.nextBag());
        }
    }

    private boolean canAct() {
        return state == GameState.RUNNING && current != null;
    }
}
