package gui.retro;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import application.ApplicationResources;
import audioPlayer.AudioPlayer;
import data.DataManager;
import data.FileManager;
import data.SongData;
import data.PlaylistData;
import data.ProjectArtistData;
import javax.swing.JTabbedPane;
import javax.swing.JOptionPane;
import static gui.retro.RetroTheme.*;

import java.util.Date;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;

public class AudioStudio implements ActionListener {
    ApplicationResources app;

    JWindow mainWindow;

    TransportRack transportRack;
    JButton openButton;
    JButton closeButton;
    JList<SongData> songList;
    DefaultListModel<SongData> songListModel;

    SongData lastData = null;

    // Group list (Playlists / Projects) - left rack
    JTabbedPane groupTabs;
    RetroRackPanel groupsRack;
    JList<PlaylistData> playlistList;
    DefaultListModel<PlaylistData> playlistListModel;
    JList<ProjectArtistData> projectList;
    DefaultListModel<ProjectArtistData> projectListModel;
    javax.swing.JPanel tabActionPanel; // holds tab-specific action buttons (extensible)
    RetroActionButton newPlaylistButton;
    RetroActionButton newProjectButton;
    RetroActionButton editButton;
    RetroActionButton deleteButton;
    RetroActionButton showAllButton;
    // single compact actions menu
    RetroActionButton actionMenuButton;
    JPopupMenu actionPopupMenu;

    // Timer for updating timeline
    private Timer timelineUpdateTimer;

    // For window dragging
    private Point dragOffset;

    // For window resizing
    private static final int RESIZE_BORDER = 8;
    private boolean isResizing = false;
    private int resizeDirection = 0;
    private static final int RESIZE_NONE = 0;
    private static final int RESIZE_N = 1;
    private static final int RESIZE_S = 2;
    private static final int RESIZE_E = 4;
    private static final int RESIZE_W = 8;
    private static final int RESIZE_NE = RESIZE_N | RESIZE_E;
    private static final int RESIZE_NW = RESIZE_N | RESIZE_W;
    private static final int RESIZE_SE = RESIZE_S | RESIZE_E;
    private static final int RESIZE_SW = RESIZE_S | RESIZE_W;

    public AudioStudio(ApplicationResources appData) {
        this.app = appData;
        mainWindow = new JWindow();
        mainWindow.setSize(800, 600);
        mainWindow.setMinimumSize(new Dimension(400, 300));
        mainWindow.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(WINDOW_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createLineBorder(WINDOW_BORDER, 2));

        // Create window header for dragging and closing
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create content panel to hold transport and files
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Create transport rack with buttons
        transportRack = new TransportRack(this);
        contentPanel.add(transportRack, BorderLayout.NORTH);

        // Set up timeline updates
        setupTimelineUpdates();

        // Create files rack with song list
        // Use tighter gaps inside racks to maximise usable area
        RetroRackPanel filesRack = new RetroRackPanel(new BorderLayout(2, 2));

        // Create song list panel
        songListModel = new DefaultListModel<>();
        refreshSongList();
        songList = new JList<>(songListModel);
        songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        songList.setCellRenderer(new SongListCellRenderer());
        songList.setBackground(LCD_BACKGROUND);
        songList.setFixedCellHeight(32); // Ensure consistent row height
        songList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SongData selected = songList.getSelectedValue();
                if (selected != null) {
                    lastData = selected;
                    transportRack.setSong(selected);
                }
            }
        });

        // Double-click to play song
        songList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    SongData selected = songList.getSelectedValue();
                    if (selected != null) {
                        app.audioPlayer.play(selected.getFilePath());
                        transportRack.setSong(selected);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.getViewport().setBackground(LCD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        filesRack.add(scrollPane, BorderLayout.CENTER);

        openButton = new JButton("Open");
        openButton.addActionListener(this);
        filesRack.add(openButton, BorderLayout.SOUTH);

        // Create groups rack (left) with Playlists / Projects tabs
        groupsRack = new RetroRackPanel(new BorderLayout(2, 2));
        groupsRack.setPreferredSize(new Dimension(220, 0));

        groupTabs = new JTabbedPane();

        // Playlists tab
        playlistListModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistListModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // LCD-style renderer (uses RetroTheme)
        playlistList.setCellRenderer(new LCDListCellRenderer(o -> {
            PlaylistData p = (PlaylistData) o;
            return p.getTitle() != null ? p.getTitle() : "(untitled)";
        }));
        playlistList.setFixedCellHeight(28);
        playlistList.setBackground(LCD_BACKGROUND);
        playlistList.setOpaque(true);
        playlistList.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                PlaylistData sel = playlistList.getSelectedValue();
                if (sel != null) {
                    app.data.setSortedByObject(sel);
                    projectList.clearSelection();
                    refreshSongList();
                }
            }
        });

        JScrollPane playlistScroll = new JScrollPane(playlistList);
        playlistScroll.getViewport().setBackground(LCD_BACKGROUND);
        playlistScroll.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        groupTabs.addTab("Playlists", playlistScroll);

        // Projects tab
        projectListModel = new DefaultListModel<>();
        projectList = new JList<>(projectListModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        projectList.setCellRenderer(new LCDListCellRenderer(o -> {
            ProjectArtistData pa = (ProjectArtistData) o;
            return pa.getName() != null ? pa.getName() : "(unnamed)";
        }));
        projectList.setFixedCellHeight(28);
        projectList.setBackground(LCD_BACKGROUND);
        projectList.setOpaque(true);
        projectList.addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                ProjectArtistData sel = projectList.getSelectedValue();
                if (sel != null) {
                    app.data.setSortedByObject(sel);
                    playlistList.clearSelection();
                    refreshSongList();
                }
            }
        });

        JScrollPane projectScroll = new JScrollPane(projectList);
        projectScroll.getViewport().setBackground(LCD_BACKGROUND);
        projectScroll.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        groupTabs.addTab("Projects", projectScroll);

        // add the tabbed pane directly (list backgrounds are now LCD-styled)
        groupsRack.add(groupTabs, BorderLayout.CENTER);

        // Action panel (dynamically updated per-tab) — uses RetroActionButton and is
        // extensible
        tabActionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 2));
        tabActionPanel.setOpaque(false);
        tabActionPanel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

        // action buttons (shared instances reused by updateTabActions)
        newPlaylistButton = new RetroActionButton("New Playlist");
        newPlaylistButton.addActionListener(this);
        newProjectButton = new RetroActionButton("New Project");
        newProjectButton.addActionListener(this);
        editButton = new RetroActionButton("Edit");
        editButton.addActionListener(this);
        deleteButton = new RetroActionButton("Delete");
        deleteButton.addActionListener(this);
        showAllButton = new RetroActionButton("All");
        showAllButton.addActionListener(evt -> {
            app.data.setSortedByObject(null);
            playlistList.clearSelection();
            projectList.clearSelection();
            refreshSongList();
        });

        // compact Actions menu button
        actionMenuButton = new RetroActionButton("Actions");
        actionPopupMenu = new JPopupMenu();
        actionMenuButton.addActionListener(evt -> showActionMenu(actionMenuButton));

        // switch tab listener to swap actions
        groupTabs.addChangeListener(ev -> updateTabActions());

        // place the action panel
        groupsRack.add(tabActionPanel, BorderLayout.SOUTH);

        // populate lists
        refreshPlaylistList();
        refreshProjectList();

        // ensure actions match initial tab
        updateTabActions();

        // populate lists
        refreshPlaylistList();
        refreshProjectList();

        contentPanel.add(groupsRack, BorderLayout.WEST);
        contentPanel.add(filesRack, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Add resize functionality
        setupResizeListeners();

        mainWindow.add(mainPanel);
        mainWindow.setVisible(true);

    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_BACKGROUND);
        headerPanel.setPreferredSize(new Dimension(0, 32));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, HEADER_BORDER));

        // Title label
        JLabel titleLabel = new JLabel("  Audio Studio");
        titleLabel.setForeground(HEADER_TEXT);
        titleLabel.setFont(HEADER_FONT);
        headerPanel.add(titleLabel, BorderLayout.CENTER);

        // Close button
        closeButton = new JButton("✕");
        closeButton.setPreferredSize(new Dimension(56, 40)); // Increased size
        closeButton.setFocusPainted(false);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setForeground(HEADER_BUTTON_TEXT);
        closeButton.setFont(HEADER_BUTTON_FONT);
        closeButton.addActionListener(this);
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(HEADER_BUTTON_HOVER);
                closeButton.setContentAreaFilled(true);
                closeButton.setBackground(HEADER_CLOSE_HOVER_BG);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(HEADER_BUTTON_TEXT);
                closeButton.setContentAreaFilled(false);
            }
        });
        headerPanel.add(closeButton, BorderLayout.EAST);

        // Add drag functionality to header
        headerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });

        headerPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentLocation = mainWindow.getLocation();
                mainWindow.setLocation(
                        currentLocation.x + e.getX() - dragOffset.x,
                        currentLocation.y + e.getY() - dragOffset.y);
            }
        });

        return headerPanel;
    }

    private void setupResizeListeners() {
        mainWindow.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                resizeDirection = getResizeDirection(e.getPoint());
                isResizing = resizeDirection != RESIZE_NONE;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isResizing = false;
                resizeDirection = RESIZE_NONE;
            }
        });

        mainWindow.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int direction = getResizeDirection(e.getPoint());
                updateCursor(direction);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isResizing) {
                    resize(e);
                }
            }
        });
    }

    private int getResizeDirection(Point p) {
        int width = mainWindow.getWidth();
        int height = mainWindow.getHeight();
        int direction = RESIZE_NONE;

        if (p.y < RESIZE_BORDER)
            direction |= RESIZE_N;
        if (p.y > height - RESIZE_BORDER)
            direction |= RESIZE_S;
        if (p.x < RESIZE_BORDER)
            direction |= RESIZE_W;
        if (p.x > width - RESIZE_BORDER)
            direction |= RESIZE_E;

        return direction;
    }

    private void updateCursor(int direction) {
        Cursor cursor;
        switch (direction) {
            case RESIZE_N:
            case RESIZE_S:
                cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                break;
            case RESIZE_E:
            case RESIZE_W:
                cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                break;
            case RESIZE_NE:
            case RESIZE_SW:
                cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                break;
            case RESIZE_NW:
            case RESIZE_SE:
                cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                break;
            default:
                cursor = Cursor.getDefaultCursor();
        }
        mainWindow.setCursor(cursor);
    }

    private void resize(MouseEvent e) {
        Point windowLocation = mainWindow.getLocation();
        Dimension windowSize = mainWindow.getSize();
        Dimension minSize = mainWindow.getMinimumSize();

        int newX = windowLocation.x;
        int newY = windowLocation.y;
        int newWidth = windowSize.width;
        int newHeight = windowSize.height;

        Point screenPoint = e.getLocationOnScreen();

        if ((resizeDirection & RESIZE_E) != 0) {
            newWidth = screenPoint.x - windowLocation.x;
        }
        if ((resizeDirection & RESIZE_S) != 0) {
            newHeight = screenPoint.y - windowLocation.y;
        }
        if ((resizeDirection & RESIZE_W) != 0) {
            int delta = screenPoint.x - windowLocation.x;
            newWidth = windowSize.width - delta;
            if (newWidth >= minSize.width) {
                newX = screenPoint.x;
            } else {
                newWidth = minSize.width;
            }
        }
        if ((resizeDirection & RESIZE_N) != 0) {
            int delta = screenPoint.y - windowLocation.y;
            newHeight = windowSize.height - delta;
            if (newHeight >= minSize.height) {
                newY = screenPoint.y;
            } else {
                newHeight = minSize.height;
            }
        }

        // Enforce minimum size
        newWidth = Math.max(newWidth, minSize.width);
        newHeight = Math.max(newHeight, minSize.height);

        mainWindow.setBounds(newX, newY, newWidth, newHeight);
    }

    private void setupTimelineUpdates() {
        // Set up seek listener
        transportRack.getTimeline().setTimelineListener(timeMs -> {
            app.audioPlayer.seek(timeMs);
        });

        // Create timer to update timeline every 100ms
        timelineUpdateTimer = new Timer(100, e -> {
            RetroTimeline timeline = transportRack.getTimeline();
            if (app.audioPlayer.isPlaying() || app.audioPlayer.isPaused()) {
                timeline.setTotalTime(app.audioPlayer.getDurationMs());
                timeline.setCurrentTime(app.audioPlayer.getPositionMs());
            }
        });
        timelineUpdateTimer.start();
    }

    public void refreshSongList() {
        songListModel.clear();
        java.util.List<SongData> songs = (app.data != null) ? app.data.getSongsSorted() : null;
        if (songs != null) {
            for (SongData song : songs) {
                songListModel.addElement(song);
            }
        }
    }

    public void refreshPlaylistList() {
        if (playlistListModel == null)
            return;
        playlistListModel.clear();
        if (app.data != null && app.data.getPlaylists() != null) {
            for (PlaylistData p : app.data.getPlaylists()) {
                playlistListModel.addElement(p);
            }
        }
    }

    public void refreshProjectList() {
        if (projectListModel == null)
            return;
        projectListModel.clear();
        if (app.data != null && app.data.getProjectsArtists() != null) {
            for (ProjectArtistData pa : app.data.getProjectsArtists()) {
                projectListModel.addElement(pa);
            }
        }
    }

    /* ---------------------- Group action helpers & popup ---------------------- */
    private void createNewPlaylist() {
        String name = JOptionPane.showInputDialog(mainWindow, "Playlist name:", "New Playlist",
                JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            PlaylistData pl = app.actionHandler.createPlaylist(name.trim());
            refreshPlaylistList();
            playlistList.setSelectedValue(pl, true);
            app.data.setSortedByObject(pl);
            refreshSongList();
        }
    }

    private void createNewProject() {
        String name = JOptionPane.showInputDialog(mainWindow, "Artist/Project name:", "New Project/Artist",
                JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            ProjectArtistData pa = app.actionHandler.createProjectArtist(name.trim());
            refreshProjectList();
            projectList.setSelectedValue(pa, true);
            app.data.setSortedByObject(pa);
            refreshSongList();
        }
    }

    private void editSelectedGroup() {
        int idx = groupTabs.getSelectedIndex();
        if (idx == 0) { // playlist rename
            PlaylistData sel = playlistList.getSelectedValue();
            if (sel != null) {
                String name = JOptionPane.showInputDialog(mainWindow, "Playlist name:", sel.getTitle());
                if (name != null && !name.trim().isEmpty()) {
                    app.actionHandler.renamePlaylist(sel, name.trim());
                    refreshPlaylistList();
                    playlistList.setSelectedValue(sel, true);
                }
            }
        } else if (idx == 1) { // project rename
            ProjectArtistData sel = projectList.getSelectedValue();
            if (sel != null) {
                String name = JOptionPane.showInputDialog(mainWindow, "Artist/Project name:", sel.getName());
                if (name != null && !name.trim().isEmpty()) {
                    app.actionHandler.renameProjectArtist(sel, name.trim());
                    refreshProjectList();
                    projectList.setSelectedValue(sel, true);
                }
            }
        }
    }

    private void deleteSelectedGroup() {
        int idx = groupTabs.getSelectedIndex();
        if (idx == 0) { // delete playlist
            PlaylistData sel = playlistList.getSelectedValue();
            if (sel != null) {
                int resp = JOptionPane.showConfirmDialog(mainWindow, "Delete playlist '" + sel.getTitle() + "'?",
                        "Delete Playlist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    app.actionHandler.deletePlaylist(sel);
                    refreshPlaylistList();
                    app.data.setSortedByObject(null);
                    refreshSongList();
                }
            }
        } else if (idx == 1) { // delete project
            ProjectArtistData sel = projectList.getSelectedValue();
            if (sel != null) {
                int resp = JOptionPane.showConfirmDialog(mainWindow,
                        "Delete project/artist '" + sel.getName() + "'?",
                        "Delete Project/Artist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    app.actionHandler.deleteProjectArtist(sel);
                    refreshProjectList();
                    app.data.setSortedByObject(null);
                    refreshSongList();
                }
            }
        }
    }

    private void clearGroupFilter() {
        app.data.setSortedByObject(null);
        playlistList.clearSelection();
        projectList.clearSelection();
        refreshSongList();
    }

    private void showActionMenu(java.awt.Component invoker) {
        actionPopupMenu.removeAll();
        int idx = groupTabs.getSelectedIndex();

        if (idx == 0) { // Playlists
            JMenuItem miNew = new JMenuItem("New Playlist");
            miNew.addActionListener(ae -> createNewPlaylist());
            actionPopupMenu.add(miNew);
        } else if (idx == 1) { // Projects
            JMenuItem miNew = new JMenuItem("New Project");
            miNew.addActionListener(ae -> createNewProject());
            actionPopupMenu.add(miNew);

            JMenuItem miSort = new JMenuItem("Sort songs to artists");
            miSort.addActionListener(ae -> {
                int assigned = app.actionHandler.sortSongsToArtists();
                refreshProjectList();
                refreshSongList();
                JOptionPane.showMessageDialog(mainWindow, assigned + " songs were assigned to projects/artists.",
                        "Sort Complete", JOptionPane.INFORMATION_MESSAGE);
            });
            actionPopupMenu.add(miSort);
        }

        JMenuItem miEdit = new JMenuItem("Edit");
        miEdit.addActionListener(ae -> editSelectedGroup());
        miEdit.setEnabled((idx == 0 && playlistList.getSelectedValue() != null)
                || (idx == 1 && projectList.getSelectedValue() != null));
        actionPopupMenu.add(miEdit);

        JMenuItem miDelete = new JMenuItem("Delete");
        miDelete.addActionListener(ae -> deleteSelectedGroup());
        miDelete.setEnabled((idx == 0 && playlistList.getSelectedValue() != null)
                || (idx == 1 && projectList.getSelectedValue() != null));
        actionPopupMenu.add(miDelete);

        actionPopupMenu.addSeparator();

        JMenuItem miShowAll = new JMenuItem("Show All");
        miShowAll.addActionListener(ae -> clearGroupFilter());
        actionPopupMenu.add(miShowAll);

        actionPopupMenu.show(invoker, 0, invoker.getHeight());
    }

    /**
     * Update the action buttons shown below the group tabs depending on selected
     * tab.
     * The panel is intentionally extensible so more actions can be added later.
     */
    private void updateTabActions() {
        if (tabActionPanel == null)
            return;
        tabActionPanel.removeAll();

        // single compact actions menu (menu contents change depending on selected tab)
        tabActionPanel.add(actionMenuButton);

        tabActionPanel.revalidate();
        tabActionPanel.repaint();
    }

    /**
     * Provides external access to the action panel so callers can add buttons
     * later.
     */
    public javax.swing.JPanel getGroupActionPanel() {
        return tabActionPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == transportRack.getPlayButton()) {
            if (app.audioPlayer.isPaused()) {
                app.audioPlayer.resume();
            } else if (lastData != null) {
                app.audioPlayer._playAudio(lastData.getFilePath());
                transportRack.setSong(lastData);
            }
        } else if (e.getSource() == transportRack.getPauseButton()) {
            app.audioPlayer.pause();
        } else if (e.getSource() == transportRack.getStopButton()) {
            app.audioPlayer.stop();
            transportRack.getTimeline().setCurrentTime(0);
            transportRack.getTimeline().setTotalTime(0);
            transportRack.setSong(null);
        } else if (e.getSource() == openButton) {
            File file = app.fileManager.openFileDialog(FileManager.FileFilter.AUDIOFILE);
            if (file != null) {
                lastData = app.actionHandler.addSong(file);
                refreshSongList();
                transportRack.setSong(lastData);
            }
        } else if (e.getSource() == newPlaylistButton) {
            createNewPlaylist();
        } else if (e.getSource() == newProjectButton) {
            createNewProject();
        } else if (e.getSource() == editButton) {
            editSelectedGroup();
        } else if (e.getSource() == deleteButton) {
            deleteSelectedGroup();
        } else if (e.getSource() == closeButton) {
            mainWindow.dispose();
            System.exit(0);
        }
    }
}
