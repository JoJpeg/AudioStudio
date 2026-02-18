package gui.retro;

import static gui.retro.RetroTheme.LCD_BACKGROUND;
import static gui.retro.RetroTheme.LCD_BORDER;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import application.ApplicationResources;
import data.FileManager;
import data.ProjectArtistData;
import data.SongData;

/**
 * Encapsulates the songs/files rack (list + open button).
 * Mirrors the old inline implementation from AudioStudio and provides
 * a simple selection callback so the parent can react to changes.
 */
public class SongsRack implements ActionListener {
    private final ApplicationResources app;
    private final RetroRackPanel rackPanel;

    private final DefaultListModel<SongData> songListModel;
    private final JList<SongData> songList;
    private final JButton openButton;

    private final Consumer<SongData> onSongSelected; // nullable
    private final Consumer<SongData> onSongDoubleClicked; // nullable

    public SongsRack(ApplicationResources app, Consumer<SongData> onSongSelected,
            Consumer<SongData> onSongDoubleClicked) {
        this.app = app;
        this.onSongSelected = onSongSelected;
        this.onSongDoubleClicked = onSongDoubleClicked;

        rackPanel = new RetroRackPanel(new BorderLayout(2, 2));

        songListModel = new DefaultListModel<>();
        refreshSongList();

        songList = new JList<>(songListModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setCellRenderer(new LCDListCellRenderer(
                o -> {
                    if (!(o instanceof SongData))
                        return "";
                    SongData song = (SongData) o;

                    ArrayList<ProjectArtistData> projectArtists = song.getOwners();
                    StringBuilder b = new StringBuilder();

                    String displayText = "";

                    if (projectArtists != null && !projectArtists.isEmpty()) {
                        for (ProjectArtistData pa : projectArtists) {
                            if (b.length() > 0)
                                b.append(", ");
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

                    return displayText;
                },
                o -> {
                    if (!(o instanceof SongData))
                        return "";
                    long duration = ((SongData) o).getDurationSeconds();
                    if (duration > 0) {
                        long minutes = duration / 60;
                        long seconds = duration % 60;
                        return String.format("%02d:%02d", minutes, seconds);
                    }
                    return "";
                }));
        songList.setBackground(LCD_BACKGROUND);
        songList.setFixedCellHeight(32);

        songList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SongData selected = songList.getSelectedValue();
                if (selected != null && onSongSelected != null) {
                    onSongSelected.accept(selected);
                }
            }
        });

        // Double-click: notify parent only (do NOT call player directly)
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SongData selected = songList.getSelectedValue();
                    if (selected != null) {
                        // Parent will handle playback/transport state via onSongDoubleClicked
                        if (onSongDoubleClicked != null) {
                            onSongDoubleClicked.accept(selected);
                        }
                        // ensure selection is applied (selection listener will notify onSongSelected)
                        songList.setSelectedValue(selected, true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.getViewport().setBackground(LCD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        rackPanel.add(scrollPane, BorderLayout.CENTER);

        openButton = new JButton("Open");
        openButton.addActionListener(this);
        rackPanel.add(openButton, BorderLayout.SOUTH);
    }

    public RetroRackPanel getPanel() {
        return rackPanel;
    }

    public JList<SongData> getList() {
        return songList;
    }

    public void refreshSongList() {
        songListModel.clear();
        java.util.List<SongData> songs = (app.data != null) ? app.data.getSongsSorted() : null;
        if (songs != null) {
            for (SongData s : songs) {
                songListModel.addElement(s);
            }
        }
    }

    public void setSelectedSong(SongData song) {
        if (song != null)
            songList.setSelectedValue(song, true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == openButton) {
            ArrayList<File> files = app.fileManager.openFileDialog(FileManager.FileFilter.AUDIOFILE);
            if (files != null) {
                for (File file : files) {
                    SongData song = app.actionHandler.addSong(file);
                    if (song != null) {
                        refreshSongList();
                        songList.setSelectedValue(song, true);
                        if (onSongSelected != null)
                            onSongSelected.accept(song);
                    }
                }
            }
        }
    }
}
