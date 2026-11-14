package tetris.ui;

import tetris.game.GameEngine;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * The application window. It owns the frame timer that drives gravity and the
 * key bindings that translate input into engine actions.
 */
public final class GameFrame extends JFrame {

    private final GameEngine engine;
    private final BoardPanel boardPanel;
    private final SidePanel sidePanel;
    private final Timer timer;
    private long lastTickNanos;

    public GameFrame(GameEngine engine) {
        super("Tetris");
        this.engine = engine;
        this.boardPanel = new BoardPanel(engine);
        this.sidePanel = new SidePanel(engine);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(20, 22, 30));
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        root.add(boardPanel, BorderLayout.WEST);

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setOpaque(false);
        sideWrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        sideWrap.add(sidePanel, BorderLayout.NORTH);
        root.add(sideWrap, BorderLayout.CENTER);

        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        installKeyBindings();
        pack();
        setLocationRelativeTo(null);

        timer = new Timer(16, event -> tick());
        timer.setCoalesce(true);
    }

    /** Shows the window and starts the game loop. Call on the Swing EDT. */
    public void start() {
        lastTickNanos = System.nanoTime();
        setVisible(true);
        timer.start();
        toFront();
        requestFocus();
    }

    private void tick() {
        long now = System.nanoTime();
        long deltaMs = lastTickNanos == 0 ? 0 : (now - lastTickNanos) / 1_000_000L;
        lastTickNanos = now;
        engine.update(Math.min(Math.max(deltaMs, 0), 100));
        sidePanel.refresh();
        boardPanel.repaint();
    }

    private void installKeyBindings() {
        bind("left", KeyEvent.VK_LEFT, engine::moveLeft);
        bind("right", KeyEvent.VK_RIGHT, engine::moveRight);
        bind("soft-drop", KeyEvent.VK_DOWN, engine::softDrop);
        bind("rotate-cw", KeyEvent.VK_UP, engine::rotateClockwise);
        bind("rotate-cw-x", KeyEvent.VK_X, engine::rotateClockwise);
        bind("rotate-ccw", KeyEvent.VK_Z, engine::rotateCounterClockwise);
        bind("hard-drop", KeyEvent.VK_SPACE, engine::hardDrop);
        bind("hold", KeyEvent.VK_C, engine::hold);
        bind("pause", KeyEvent.VK_P, engine::togglePause);
        bind("pause-escape", KeyEvent.VK_ESCAPE, engine::togglePause);
        bind("restart", KeyEvent.VK_R, engine::restart);
    }

    private void bind(String id, int keyCode, Runnable action) {
        JComponent root = getRootPane();
        KeyStroke pressed = KeyStroke.getKeyStroke(keyCode, 0, false);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pressed, id);
        root.getActionMap().put(id, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }
}
