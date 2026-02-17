package gui.retro;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import data.ProjectArtistData;
import data.SongData;
import static gui.retro.RetroTheme.*;

public class SongListCellRenderer extends DefaultListCellRenderer {

    private JPanel panel;
    private JLabel titleLabel;
    private JLabel durationLabel;

    public SongListCellRenderer() {
        panel = new JPanel(new BorderLayout());
        titleLabel = new JLabel();
        durationLabel = new JLabel();

        titleLabel.setFont(LCD_FONT);
        durationLabel.setFont(LCD_FONT);

        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(durationLabel, BorderLayout.EAST);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {

        String displayText = "";
        String durationText = "";

        if (value instanceof SongData) {
            SongData song = (SongData) value;

            ArrayList<ProjectArtistData> projectArtists = song.getOwners();

            if (projectArtists != null && !projectArtists.isEmpty()) {
                StringBuilder b = new StringBuilder();
                for (ProjectArtistData pa : projectArtists) {
                    if (b.length() > 0) {
                        b.append(", ");
                    }
                    b.append(pa.getName());
                }
                displayText += b.toString() + " - ";
            } else {
                if (song.getGuessedArtist() != null && !song.getGuessedArtist().isEmpty()) {
                    displayText += song.getGuessedArtist() + " - ";
                } else {
                    displayText += "Unknown Artist - ";
                }
            }
            displayText += song.getTitle() != null ? song.getTitle() : "Unknown";

            if (displayText.equals("Unknown Artist - Unknown")) {
                String filePath = song.getFilePath();
                displayText = filePath != null ? new java.io.File(filePath).getName() : "Unknown";
            }

            // Format duration as MM:SS
            long duration = song.getDurationSeconds();
            if (duration > 0) {
                long minutes = duration / 60;
                long seconds = duration % 60;
                durationText = String.format("%02d:%02d", minutes, seconds);
            }
        }

        titleLabel.setText(displayText);
        durationLabel.setText(durationText);

        Color bgColor;
        Color fgColor;
        if (isSelected) {
            bgColor = LCD_BACKGROUND_SELECTED;
            fgColor = LCD_TEXT_SELECTED;
        } else {
            bgColor = index % 2 == 0 ? LCD_BACKGROUND : LCD_BACKGROUND_ALT;
            fgColor = LCD_TEXT;
        }

        panel.setBackground(bgColor);
        titleLabel.setForeground(fgColor);
        titleLabel.setBackground(bgColor);
        durationLabel.setForeground(fgColor);
        durationLabel.setBackground(bgColor);

        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        panel.setOpaque(true);
        titleLabel.setOpaque(false);
        durationLabel.setOpaque(false);

        // Set preferred size to ensure proper height
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 32));

        return panel;
    }
}
