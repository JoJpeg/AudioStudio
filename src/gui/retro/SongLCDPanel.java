package gui.retro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import data.SongData;
import data.ProjectArtistData;
import java.util.List;
import static gui.retro.RetroTheme.*;

/**
 * LCD-style panel for displaying currently playing song info, with expandable
 * details.
 */
public class SongLCDPanel extends JPanel {
    private SongData song;
    private boolean expanded = false;
    private JButton expandButton;

    public SongLCDPanel() {
        setOpaque(false);
        // more compact height for the LCD song line
        setPreferredSize(new Dimension(260, 28));
        setLayout(new BorderLayout());
        expandButton = new JButton("▶");
        expandButton.setFont(new Font("SansSerif", Font.PLAIN, 10));
        expandButton.setFocusPainted(false);
        expandButton.setBorderPainted(false);
        expandButton.setContentAreaFilled(false);
        expandButton.setForeground(LCD_TEXT);
        expandButton.setMargin(new Insets(0, 0, 0, 0)); // Remove inner padding
        expandButton.setPreferredSize(new Dimension(34, 24));
        expandButton.addActionListener(e -> {
            expanded = !expanded;
            expandButton.setText(expanded ? "▼" : "▶");
            revalidate();
            Container p = getParent();
            if (p != null)
                p.revalidate();
            repaint();
        });
        add(expandButton, BorderLayout.EAST);
    }

    public void setSong(SongData song) {
        this.song = song;
        revalidate();
        repaint();
    }

    public SongData getSong() {
        return song;
    }

    public boolean isExpanded() {
        return expanded;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw LCD background
        g2d.setColor(PANEL_DARK_SHADOW);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRect(1, 1, width - 2, height - 2);
        g2d.setColor(LCD_BACKGROUND);
        g2d.fillRect(2, 2, width - 4, height - 4);

        // Draw scanline effect
        g2d.setColor(new Color(0, 20, 0, 30));
        for (int i = 2; i < height - 2; i += 2) {
            g2d.drawLine(2, i, width - 3, i);
        }

        // Draw main info
        if (song != null) {
            g2d.setColor(LCD_TEXT);
            g2d.setFont(LCD_FONT);
            String mainInfo = song.getTitle() + " - " + song.getGuessedArtist();
            FontMetrics fm = g2d.getFontMetrics();
            int textX = 12;
            int textY = 2 + (height + fm.getAscent() - fm.getDescent()) / 2;
            g2d.drawString(mainInfo, textX, textY);
        }

        // Draw expanded info
        if (expanded && song != null) {
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2d.setColor(LCD_TEXT_SELECTED);
            int y = height + 18;
            g2d.drawString("Subtitle: " + song.getSubtitle(), 12, y);
            y += 18;
            g2d.drawString("Version: " + song.getVersion(), 12, y);
            y += 18;

            //draw file path
            g2d.drawString("File: " + song.getFilePath(), 12, y);
            y += 18;
            if (song.getOwners() != null) {
                g2d.drawString("Owners:", 12, y);
                y += 16;
                for (ProjectArtistData owner : song.getOwners()) {
                    g2d.drawString("- " + owner.getName(), 24, y);
                    y += 16;
                }
            }
            // Song versions (if available)
            // ...could be added here if SongData exposes them
        }
        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        if (expanded && song != null) {
            int ownerCount = song.getOwners() != null ? song.getOwners().size() : 0;
            return new Dimension(320, 40 + 18 * (3 + ownerCount));
        }
        return new Dimension(320, 40);
    }
}
