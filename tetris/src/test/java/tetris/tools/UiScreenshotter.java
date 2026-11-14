package tetris.tools;

import tetris.game.GameEngine;
import tetris.game.GameState;
import tetris.ui.GameFrame;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Development tool: runs the real Swing UI on the current display, plays a
 * scripted game with {@link Robot} and writes PNG screenshots. It is used under
 * Xvfb to capture the before/after evidence for the pull request.
 *
 * <p>Run from the {@code tetris} directory:
 * {@code java -cp target/classes:target/test-classes tetris.tools.UiScreenshotter docs/screenshots}</p>
 */
public final class UiScreenshotter {

    private static final long SEED = 20261114L;
    private static final int WINDOW_X = 20;
    private static final int WINDOW_Y = 20;
    private static final int MID_GAME_PIECES = 12;
    private static final int[] HORIZONTAL_SHIFT = {3, -1, 0, 2, -3, 1, 4, -2, 0, 3, -1, 2};

    private UiScreenshotter() {
    }

    public static void main(String[] args) throws Exception {
        Path outputDir = Path.of(args.length > 0 ? args[0] : "docs/screenshots");
        Files.createDirectories(outputDir);

        AtomicReference<GameFrame> frameRef = new AtomicReference<>();
        AtomicReference<GameEngine> engineRef = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GameEngine engine = new GameEngine(SEED);
            GameFrame frame = new GameFrame(engine);
            frame.setLocation(WINDOW_X, WINDOW_Y);
            frame.start();
            frameRef.set(frame);
            engineRef.set(engine);
        });

        GameFrame frame = frameRef.get();
        GameEngine engine = engineRef.get();
        Robot robot = new Robot();
        robot.setAutoDelay(8);
        Thread.sleep(800);
        clickInside(robot, frame);

        save(capture(frame), outputDir.resolve("start.png"));

        for (int i = 0; i < MID_GAME_PIECES; i++) {
            if (i == 3) {
                tap(robot, KeyEvent.VK_C);
            }
            int rotations = (i * 3 + 1) % 4;
            for (int rotation = 0; rotation < rotations; rotation++) {
                tap(robot, KeyEvent.VK_UP);
            }
            int shift = HORIZONTAL_SHIFT[i % HORIZONTAL_SHIFT.length];
            int key = shift < 0 ? KeyEvent.VK_LEFT : KeyEvent.VK_RIGHT;
            for (int step = 0; step < Math.abs(shift); step++) {
                tap(robot, key);
            }
            tap(robot, KeyEvent.VK_SPACE);
            Thread.sleep(180);
        }
        Thread.sleep(400);
        save(capture(frame), outputDir.resolve("midgame.png"));

        tap(robot, KeyEvent.VK_P);
        Thread.sleep(300);
        requireState(engine, GameState.PAUSED);
        save(capture(frame), outputDir.resolve("paused.png"));

        tap(robot, KeyEvent.VK_P);
        Thread.sleep(250);

        for (int i = 0; i < 120 && state(engine) != GameState.GAME_OVER; i++) {
            tap(robot, KeyEvent.VK_SPACE);
            Thread.sleep(90);
        }
        Thread.sleep(400);
        requireState(engine, GameState.GAME_OVER);
        save(capture(frame), outputDir.resolve("gameover.png"));

        System.out.println("Screenshots written to " + outputDir.toAbsolutePath());
        System.out.println("Final state: " + state(engine) + ", score=" + engine.score());
        System.exit(0);
    }

    private static void clickInside(Robot robot, GameFrame frame) {
        Point location = frame.getLocationOnScreen();
        robot.mouseMove(location.x + 80, location.y + 80);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(80);
    }

    private static void tap(Robot robot, int keyCode) {
        robot.keyPress(keyCode);
        robot.keyRelease(keyCode);
        robot.delay(18);
    }

    /**
     * Captures what the live window renders. Painting the real content pane is
     * independent of the window server's placement of the window, so the
     * evidence is reproducible under Xvfb.
     */
    private static BufferedImage capture(GameFrame frame) throws Exception {
        AtomicReference<BufferedImage> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            JComponent content = (JComponent) frame.getContentPane();
            BufferedImage image = new BufferedImage(
                    content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            try {
                content.paint(graphics);
            } finally {
                graphics.dispose();
            }
            result.set(image);
        });
        return result.get();
    }

    private static void save(BufferedImage image, Path path) throws IOException {
        ImageIO.write(image, "png", path.toFile());
        System.out.println("Wrote " + path + " (" + image.getWidth() + "x" + image.getHeight() + ")");
    }

    private static GameState state(GameEngine engine) throws Exception {
        AtomicReference<GameState> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(engine.state()));
        return ref.get();
    }

    private static void requireState(GameEngine engine, GameState expected) throws Exception {
        GameState actual = state(engine);
        if (actual != expected) {
            throw new IllegalStateException("Expected " + expected + " but was " + actual);
        }
    }
}
