package tetris.ui;

import tetris.game.GameEngine;
import tetris.game.Tetromino;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The information column: score, stats, next pieces, hold and controls. The
 * first version is intentionally plain; it is the baseline for the visual
 * comparison in the pull request.
 */
public final class SidePanel extends JPanel {

    private final GameEngine engine;
    private final JLabel scoreValue = valueLabel();
    private final JLabel levelValue = valueLabel();
    private final JLabel linesValue = valueLabel();
    private final JLabel nextValue = valueLabel();
    private final JLabel holdValue = valueLabel();
    private final JLabel statusValue = valueLabel();

    public SidePanel(GameEngine engine) {
        this.engine = engine;
        setPreferredSize(new Dimension(300, BoardPanel.BOARD_HEIGHT));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(24, 26, 36));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("TETRIS");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(LEFT_ALIGNMENT);
        add(title);

        JLabel subtitle = new JLabel("Java Swing edition");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(new Color(150, 156, 176));
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        add(subtitle);
        add(Box.createVerticalStrut(16));

        add(section("SCORE", scoreValue));
        add(Box.createVerticalStrut(6));
        add(section("LEVEL", levelValue));
        add(Box.createVerticalStrut(6));
        add(section("LINES", linesValue));
        add(Box.createVerticalStrut(6));
        add(section("NEXT", nextValue));
        add(Box.createVerticalStrut(6));
        add(section("HOLD", holdValue));
        add(Box.createVerticalStrut(6));
        add(section("STATUS", statusValue));
        add(Box.createVerticalStrut(12));

        JTextArea controls = new JTextArea("""
                LEFT / RIGHT   Move
                DOWN           Soft drop
                UP / X         Rotate
                Z              Rotate CCW
                SPACE          Hard drop
                C              Hold
                P              Pause
                R              Restart
                """);
        controls.setEditable(false);
        controls.setOpaque(false);
        controls.setForeground(new Color(186, 192, 210));
        controls.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        controls.setAlignmentX(LEFT_ALIGNMENT);
        controls.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        add(controls);

        refresh();
    }

    private static JLabel valueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JPanel section(String name, JLabel value) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        JLabel key = new JLabel(name);
        key.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        key.setForeground(new Color(150, 156, 176));
        panel.add(key, BorderLayout.WEST);
        panel.add(value, BorderLayout.EAST);
        return panel;
    }

    /** Refreshes all dynamic values; cheap enough to call on every frame. */
    public void refresh() {
        scoreValue.setText(String.valueOf(engine.score()));
        levelValue.setText(String.valueOf(engine.level()));
        linesValue.setText(String.valueOf(engine.lines()));
        holdValue.setText(engine.held() == null ? "-" : engine.held().name());
        statusValue.setText(engine.state().name().replace('_', ' '));
        nextValue.setText(formatNext(engine.nextPieces()));
    }

    private static String formatNext(List<Tetromino> pieces) {
        return pieces.stream().map(Enum::name).collect(Collectors.joining(" "));
    }
}
