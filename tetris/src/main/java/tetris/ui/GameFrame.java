package tetris.ui;

import tetris.game.ClearResult;
import tetris.game.FinishReason;
import tetris.game.GameEngine;
import tetris.game.GameListener;
import tetris.game.GameMode;
import tetris.game.GameResult;
import tetris.game.GameState;
import tetris.game.HighScoreStore;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * The application window. It owns the frame timer, the menu/game/summary cards
 * and the key bindings that translate input into engine actions.
 */
public final class GameFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String CARD_MENU = "menu";
    private static final String CARD_GAME = "game";
    private static final String CARD_SUMMARY = "summary";

    private final GameEngine engine;
    private final HighScoreStore scores;
    private final BoardPanel boardPanel;
    private final SidePanel sidePanel;
    private final MenuPanel menuPanel;
    private final SummaryPanel summaryPanel;
    private final JPanel cards;
    private final CardLayout cardLayout;
    private final Timer timer;
    private String currentCard = CARD_MENU;
    private long lastTickNanos;

    public GameFrame(GameEngine engine, HighScoreStore scores) {
        super("俄罗斯方块 · Java Tetris");
        this.engine = engine;
        this.scores = scores;
        this.boardPanel = new BoardPanel(engine);
        this.sidePanel = new SidePanel(engine, scores);
        this.menuPanel = new MenuPanel(scores);
        this.summaryPanel = new SummaryPanel(scores);

        engine.setListener(new GameListener() {
            @Override
            public void onLinesCleared(ClearResult result) {
                boardPanel.showLineClear(result);
            }

            @Override
            public void onRunFinished(FinishReason reason) {
                finishRun(reason);
            }
        });

        JPanel gameView = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.paintWindowBackground(g2, getWidth(), getHeight());
                g2.dispose();
            }
        };
        gameView.setBackground(Theme.WINDOW_TOP);
        gameView.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        gameView.add(boardPanel, BorderLayout.WEST);

        JPanel sideWrap = new JPanel(new BorderLayout());
        sideWrap.setOpaque(false);
        sideWrap.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        sideWrap.add(sidePanel, BorderLayout.NORTH);
        gameView.add(sideWrap, BorderLayout.CENTER);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);
        cards.add(menuPanel, CARD_MENU);
        cards.add(gameView, CARD_GAME);
        cards.add(summaryPanel, CARD_SUMMARY);
        setContentPane(cards);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setIconImage(createWindowIcon());
        installKeyBindings();
        pack();
        setLocationRelativeTo(null);

        timer = new Timer(16, event -> tick());
        timer.setCoalesce(true);
        showCard(CARD_MENU);
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
        if (CARD_GAME.equals(currentCard)) {
            sidePanel.repaint();
            boardPanel.repaint();
        }
    }

    private void finishRun(FinishReason reason) {
        GameResult result = engine.result();
        boolean newRecord = scores.submit(result);
        summaryPanel.show(reason, result, newRecord);
        menuPanel.refresh();
        showCard(CARD_SUMMARY);
    }

    private void startMode(GameMode mode) {
        engine.startGame(mode);
        boardPanel.clearToast();
        showCard(CARD_GAME);
        boardPanel.repaint();
        sidePanel.repaint();
    }

    private void restartCurrentMode() {
        GameMode mode = engine.mode();
        startMode(mode == null ? menuPanel.selectedMode() : mode);
    }

    private void backToMenu() {
        engine.returnToMenu();
        menuPanel.refresh();
        showCard(CARD_MENU);
    }

    private void showCard(String card) {
        currentCard = card;
        cardLayout.show(cards, card);
        cards.revalidate();
        cards.repaint();
        requestFocus();
    }

    private void installKeyBindings() {
        bind("left", KeyEvent.VK_LEFT, this::onLeft);
        bind("left-a", KeyEvent.VK_A, this::onLeft);
        bind("right", KeyEvent.VK_RIGHT, this::onRight);
        bind("right-d", KeyEvent.VK_D, this::onRight);
        bind("up", KeyEvent.VK_UP, this::onUp);
        bind("up-w", KeyEvent.VK_W, this::onUp);
        bind("down", KeyEvent.VK_DOWN, this::onDown);
        bind("down-s", KeyEvent.VK_S, this::onDown);
        bind("rotate-x", KeyEvent.VK_X, this::onRotateClockwise);
        bind("rotate-ccw", KeyEvent.VK_Z, this::onRotateCounterClockwise);
        bind("hard-drop", KeyEvent.VK_SPACE, this::onSpace);
        bind("hold", KeyEvent.VK_C, this::onHold);
        bind("pause", KeyEvent.VK_P, this::onPause);
        bind("pause-esc", KeyEvent.VK_ESCAPE, this::onPause);
        bind("restart", KeyEvent.VK_R, this::onRestart);
        bind("menu", KeyEvent.VK_Q, this::onMenu);
        bind("confirm", KeyEvent.VK_ENTER, this::onEnter);
        bind("mode-1", KeyEvent.VK_1, () -> onModeNumber(GameMode.MARATHON));
        bind("mode-2", KeyEvent.VK_2, () -> onModeNumber(GameMode.SPRINT));
        bind("mode-3", KeyEvent.VK_3, () -> onModeNumber(GameMode.ULTRA));
    }

    private void onLeft() {
        if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.moveLeft();
        }
    }

    private void onRight() {
        if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.moveRight();
        }
    }

    private void onUp() {
        if (CARD_MENU.equals(currentCard)) {
            menuPanel.moveSelection(-1);
        } else if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.rotateClockwise();
        }
    }

    private void onDown() {
        if (CARD_MENU.equals(currentCard)) {
            menuPanel.moveSelection(1);
        } else if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.softDrop();
        }
    }

    private void onRotateClockwise() {
        if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.rotateClockwise();
        }
    }

    private void onRotateCounterClockwise() {
        if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.rotateCounterClockwise();
        }
    }

    private void onSpace() {
        if (CARD_MENU.equals(currentCard)) {
            startMode(menuPanel.selectedMode());
        } else if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.hardDrop();
        }
    }

    private void onEnter() {
        if (CARD_MENU.equals(currentCard)) {
            startMode(menuPanel.selectedMode());
        } else if (CARD_SUMMARY.equals(currentCard)) {
            backToMenu();
        }
    }

    private void onHold() {
        if (CARD_GAME.equals(currentCard) && engine.state() == GameState.RUNNING) {
            engine.hold();
        }
    }

    private void onPause() {
        if (CARD_GAME.equals(currentCard)) {
            engine.togglePause();
            boardPanel.repaint();
            sidePanel.repaint();
        }
    }

    private void onRestart() {
        if (CARD_MENU.equals(currentCard)) {
            startMode(menuPanel.selectedMode());
        } else {
            restartCurrentMode();
        }
    }

    private void onMenu() {
        if (CARD_SUMMARY.equals(currentCard)) {
            backToMenu();
        } else if (CARD_GAME.equals(currentCard)) {
            if (engine.state() == GameState.RUNNING) {
                engine.togglePause();
                boardPanel.repaint();
                sidePanel.repaint();
            } else if (engine.state() == GameState.PAUSED) {
                backToMenu();
            }
        }
    }

    private void onModeNumber(GameMode mode) {
        if (CARD_MENU.equals(currentCard)) {
            menuPanel.select(mode);
            startMode(mode);
        }
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
