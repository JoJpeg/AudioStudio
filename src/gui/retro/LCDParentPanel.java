package gui.retro;

import javax.swing.*;
import java.awt.*;
import static gui.retro.RetroTheme.*;

/**
 * LCD-style parent panel combining SongLCDPanel and RetroTimeline,
 * styled to resemble an old CD player LCD display.
 */
public class LCDParentPanel extends JPanel {
    private final SongLCDPanel songPanel;
    private final RetroTimeline timelinePanel;

    public LCDParentPanel(SongLCDPanel songPanel, RetroTimeline timelinePanel) {
        if (songPanel == null || timelinePanel == null) {
            throw new IllegalArgumentException("LCDParentPanel requires non-null SongLCDPanel and RetroTimeline");
        }
        this.songPanel = songPanel;
        this.timelinePanel = timelinePanel;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));
        // Panel padding for LCD border effect
        // reduce inner padding to make the LCD block smaller
        setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        // Add panels
        add(songPanel, BorderLayout.NORTH);
        add(timelinePanel, BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension songPref = songPanel.getPreferredSize();
        Dimension timelinePref = timelinePanel.getPreferredSize();
        Insets insets = getInsets();

        // width: at least a sensible minimum, but large enough to fit timeline
        int width = Math.max(360, Math.max(songPref.width, timelinePref.width) + insets.left + insets.right + 12);
        // height: sum of child preferred heights + inner padding
        int height = insets.top + insets.bottom + songPref.height + timelinePref.height + 6;
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw LCD background with thick border for CD player look
        g2d.setColor(PANEL_DARK_SHADOW);
        g2d.fillRoundRect(0, 0, width, height, 18, 18);
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(4, 4, width - 8, height - 8, 14, 14);
        g2d.setColor(LCD_BACKGROUND);
        g2d.fillRoundRect(8, 8, width - 16, height - 16, 10, 10);

        // Draw scanline effect
        g2d.setColor(new Color(0, 20, 0, 30));
        for (int i = 10; i < height - 10; i += 2) {
            g2d.drawLine(10, i, width - 11, i);
        }

        g2d.dispose();
    }
}
