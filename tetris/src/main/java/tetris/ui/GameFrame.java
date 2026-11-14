package tetris.ui;

import tetris.game.GameEngine;
import tetris.game.GameListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The application window. It owns the frame timer that drives gravity and the
 * key bindings that translate input into engine actions.
 */
public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private final GameEngine engine;
    private final BoardPanel boardPanel;
    private final SidePanel sidePanel;
    private final Timer timer;
    private long lastTickNanos;

    public GameFrame(GameEngine engine) {
        super("俄罗斯方块 · Java Tetris");
        this.engine = engine;
        this.boardPanel = new BoardPanel(engine);
        this.sidePanel = new SidePanel(engine);

        engine.setListener(new GameListener() {
            @Override
            public void onLinesCleared(int count, List<Integer> rows) {
                boardPanel.showLineClear(count);
            }
        });

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintWindowBackground(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBackground(Theme.WINDOW_TOP);
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
        setIconImage(createWindowIcon());
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
        sidePanel.repaint();
        boardPanel.repaint();
    }

    private void installKeyBindings() {
        bind("left", KeyEvent.VK_LEFT, engine::moveLeft);
        bind("left-a", KeyEvent.VK_A, engine::moveLeft);
        bind("right", KeyEvent.VK_RIGHT, engine::moveRight);
        bind("right-d", KeyEvent.VK_D, engine::moveRight);
        bind("soft-drop", KeyEvent.VK_DOWN, engine::softDrop);
        bind("soft-drop-s", KeyEvent.VK_S, engine::softDrop);
        bind("rotate-cw", KeyEvent.VK_UP, engine::rotateClockwise);
        bind("rotate-cw-x", KeyEvent.VK_X, engine::rotateClockwise);
        bind("rotate-cw-w", KeyEvent.VK_W, engine::rotateClockwise);
        bind("rotate-ccw", KeyEvent.VK_Z, engine::rotateCounterClockwise);
        bind("hard-drop", KeyEvent.VK_SPACE, engine::hardDrop);
        bind("hold", KeyEvent.VK_C, engine::hold);
        bind("pause", KeyEvent.VK_P, engine::togglePause);
        bind("pause-escape", KeyEvent.VK_ESCAPE, engine::togglePause);
        bind("restart", KeyEvent.VK_R, () -> {
            engine.restart();
            boardPanel.clearToast();
        });
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

    private static Image createWindowIcon() {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.WINDOW_TOP);
        g2.fillRoundRect(0, 0, 64, 64, 16, 16);
        Color color = Theme.pieceColor(tetris.game.Tetromino.T);
        Theme.drawBlock(g2, 9, 8, 15, color, 1f);
        Theme.drawBlock(g2, 25, 8, 15, color, 1f);
        Theme.drawBlock(g2, 41, 8, 15, color, 1f);
        Theme.drawBlock(g2, 25, 24, 15, color, 1f);
        g2.dispose();
        return image;
    }
}
