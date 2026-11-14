package tetris.game;

/**
 * A game mode defines the win/end condition and how the run is timed.
 */
public enum GameMode {

    MARATHON("马拉松", "MARATHON", "无尽模式，越玩越快"),
    SPRINT("冲刺 40 行", "SPRINT", "用最短时间清空 40 行", 40, 0),
    ULTRA("极速 2 分钟", "ULTRA", "120 秒内冲击最高分", 0, 120_000);

    private final String displayName;
    private final String englishName;
    private final String description;
    private final int targetLines;
    private final long timeLimitMs;

    GameMode(String displayName, String englishName, String description) {
        this(displayName, englishName, description, 0, 0);
    }

    GameMode(String displayName, String englishName, String description,
             int targetLines, long timeLimitMs) {
        this.displayName = displayName;
        this.englishName = englishName;
        this.description = description;
        this.targetLines = targetLines;
        this.timeLimitMs = timeLimitMs;
    }

    public String displayName() {
        return displayName;
    }

    public String englishName() {
        return englishName;
    }

    public String description() {
        return description;
    }

    /** Number of lines needed to finish; 0 means endless. */
    public int targetLines() {
        return targetLines;
    }

    /** Time limit in milliseconds; 0 means no limit. */
    public long timeLimitMs() {
        return timeLimitMs;
    }

    public boolean isTargetMode() {
        return targetLines > 0;
    }

    public boolean isTimed() {
        return timeLimitMs > 0;
    }
}
