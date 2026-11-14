package tetris.ui;

import tetris.game.HighScoreStore;

import java.util.Locale;

/** Small formatting helpers shared by the menu, HUD and summary. */
public final class UiFormat {

    private UiFormat() {
    }

    public static String number(int value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    public static String clock(long milliseconds) {
        return HighScoreStore.formatTime(milliseconds);
    }

    public static String decimal(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
