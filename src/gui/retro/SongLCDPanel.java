package gui.retro;

import static gui.retro.RetroTheme.LCD_BACKGROUND;
import static gui.retro.RetroTheme.LCD_FONT;
import static gui.retro.RetroTheme.LCD_TEXT;
import static gui.retro.RetroTheme.LCD_TEXT_SELECTED;
import static gui.retro.RetroTheme.PANEL_DARK_SHADOW;
import static gui.retro.RetroTheme.PANEL_SHADOW;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JPanel;

import data.ProjectArtistData;
import data.SongData;

/**
 * LCD-style panel for displaying currently playing song info, with expandable
 * details.
 */
public class SongLCDPanel extends JPanel {
    private SongData song;
    private boolean expanded = false;
    private JButton expandButton;

    // Clickable hit areas for expanded text
    private java.util.List<Rectangle> ownerClickAreas = new java.util.ArrayList<>();
    private java.util.List<data.ProjectArtistData> ownerClickTargets = new java.util.ArrayList<>();
    private Rectangle fileClickArea = null;

    // Callbacks (optional) - AudioStudio will register handlers
    private java.util.function.Consumer<String> onFileClick;
    private java.util.function.Consumer<data.ProjectArtistData> onOwnerClick;

    public SongLCDPanel() {
        setOpaque(false);
        // more compact height for the LCD song line
        setPreferredSize(new Dimension(260, 28));
        setLayout(new BorderLayout());
        expandButton = new JButton(" ");
        expandButton.setFont(new Font("SansSerif", Font.PLAIN, 8));
        expandButton.setFocusPainted(false);
        expandButton.setBorderPainted(false);
        expandButton.setContentAreaFilled(false);
        expandButton.setForeground(LCD_TEXT);
        expandButton.setMargin(new Insets(0, 0, 0, 0)); // Remove inner padding
        expandButton.setPreferredSize(new Dimension(48, 42));
        expandButton.addActionListener(e -> {
            expanded = !expanded;
            expandButton.setText(expanded ? "▼" : "◀");
            revalidate();
            Container p = getParent();
            if (p != null)
                p.revalidate();
            repaint();
        });
        // anchor expand button to the top-right corner
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setOpaque(false);
        eastPanel.add(expandButton, BorderLayout.NORTH);
        add(eastPanel, BorderLayout.EAST);

        // mouse handling for clickable expanded items
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!expanded || song == null)
                    return;
                Point pt = e.getPoint();
                if (fileClickArea != null && fileClickArea.contains(pt)) {
                    if (onFileClick != null) {
                        onFileClick.accept(song.getFilePath());
                    } else {
                        // fallback: try to open folder using Desktop
                        try {
                            java.io.File f = new java.io.File(song.getFilePath());
                            java.io.File dir = f.isDirectory() ? f : f.getParentFile();
                            if (dir != null && dir.exists() && java.awt.Desktop.isDesktopSupported()) {
                                java.awt.Desktop.getDesktop().open(dir);
                            }
                        } catch (Exception ex) {
                            // ignore fallback errors
                        }
                    }
                    return;
                }
                for (int i = 0; i < ownerClickAreas.size(); i++) {
                    if (ownerClickAreas.get(i).contains(pt)) {
                        data.ProjectArtistData pa = ownerClickTargets.get(i);
                        if (onOwnerClick != null) {
                            onOwnerClick.accept(pa);
                        }
                        return;
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!expanded || song == null) {
                    setCursor(Cursor.getDefaultCursor());
                    return;
                }
                Point pt = e.getPoint();
                boolean over = (fileClickArea != null && fileClickArea.contains(pt));
                if (!over) {
                    for (Rectangle r : ownerClickAreas) {
                        if (r.contains(pt)) {
                            over = true;
                            break;
                        }
                    }
                }
                setCursor(over ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
    }

    public void setSong(SongData song) {
        if (expandButton != null) {
            if (!expanded) {
                expandButton.setText("◀");
            } else {
                expandButton.setText("▼");
            }
        }
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

    /**
     * Register a callback invoked when the filepath area is clicked (receives
     * the song file path). If not set, a platform Desktop.open fallback is used.
     */
    public void setOnFileClick(java.util.function.Consumer<String> cb) {
        this.onFileClick = cb;
    }

    /**
     * Register a callback invoked when an owner name is clicked (receives the
     * corresponding ProjectArtistData).
     */
    public void setOnOwnerClick(java.util.function.Consumer<data.ProjectArtistData> cb) {
        this.onOwnerClick = cb;
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
        int textX = 12;
        int textY = 0;
        if (song != null) {
            g2d.setColor(LCD_TEXT);
            g2d.setFont(LCD_FONT);
            String mainInfo = song.getTitle() + " - " + song.getGuessedArtist();
            FontMetrics fm = g2d.getFontMetrics();
            if (expanded) {
                textY = (fm.getAscent() - fm.getDescent()) + 15;
            } else {
                // If expanded, move the main info up to make room for details
                textY += (height + (fm.getAscent() - fm.getDescent())) / 2;
            }
            g2d.drawString(mainInfo, textX, textY);
        }

        // Draw expanded info (and compute clickable hit areas)
        if (expanded && song != null) {
            g2d.setColor(LCD_TEXT_SELECTED);
            int yGap = 15;
            int mainBaseline = textY + yGap;
            int y = mainBaseline + 2;

            // reset hit areas
            ownerClickAreas.clear();
            ownerClickTargets.clear();
            fileClickArea = null;

            FontMetrics fm = g2d.getFontMetrics();

            if (song.getSubtitle() != null) {
                String s = "Subtitle: " + song.getSubtitle();
                g2d.drawString(s, 12, y);
                y += yGap;
            }

            String ver = "Version: " + song.getVersion();
            g2d.drawString(ver, 12, y);
            y += yGap;

            // draw file path (make it clickable)
            String fileLabel = "File: " + song.getFilePath();
            int fx = 12;
            g2d.drawString(fileLabel, fx, y);
            int fw = fm.stringWidth(fileLabel);
            fileClickArea = new Rectangle(fx, y - fm.getAscent(), fw, fm.getAscent() + fm.getDescent());
            // underline to indicate clickable
            g2d.setColor(LCD_TEXT);
            g2d.drawLine(fx, y + 2, fx + fw, y + 2);
            g2d.setColor(LCD_TEXT_SELECTED);
            y += yGap;

            if (song.getOwners() != null && !song.getOwners().isEmpty()) {
                g2d.drawString("Owners:", 12, y);
                y += yGap;
                for (ProjectArtistData owner : song.getOwners()) {
                    String ownerText = "- " + owner.getName();
                    int ox = 24;
                    g2d.drawString(ownerText, ox, y);
                    int ow = fm.stringWidth(ownerText);
                    ownerClickAreas.add(new Rectangle(ox, y - fm.getAscent(), ow, fm.getAscent() + fm.getDescent()));
                    ownerClickTargets.add(owner);
                    // underline owner name (skip the leading "- ")
                    int dashW = fm.stringWidth("- ");
                    int nameX = ox + dashW;
                    int nameW = fm.stringWidth(owner.getName());
                    g2d.setColor(LCD_TEXT);
                    g2d.drawLine(nameX, y + 2, nameX + nameW, y + 2);
                    g2d.setColor(LCD_TEXT_SELECTED);
                    y += yGap;
                }
            }
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
