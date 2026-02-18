package gui.retro;

import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.event.MouseEvent;

import application.ApplicationResources;
import data.ProjectArtistData;
import data.SongData;

import static gui.retro.RetroTheme.LCD_BACKGROUND;
import static gui.retro.RetroTheme.LCD_BORDER;

public class SongList {

    private final DefaultListModel<SongData> songListModel;
    private final JList<SongData> songList;
    private ApplicationResources app;
    public SongData selectedSong;

    Consumer<SongData> onSongDoubleClicked;;
    Consumer<SongData> onSongSelected;

    public SongList(ApplicationResources app) {
        this.app = app;

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
    }

    void setupListeners(ApplicationResources app) {
        onSongDoubleClicked = song -> {
            if (app.audioPlayer != null) {
                app.audioPlayer.play(song.getFilePath());
            }
        };

        onSongSelected = selected -> {
            if (selected != null) {
                selectedSong = selected;
                // transportRack.setSong(selected);
            }
            if (!app.audioPlayer.isPlaying() && !app.audioPlayer.isPaused()) {
                // transportRack.setSong(selected); TODO:implement
            }
        };

    }

    public SongData getSelectedSong() {
        return selectedSong;
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

}
