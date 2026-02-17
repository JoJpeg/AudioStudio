package gui.retro;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import application.ApplicationResources;
import audioPlayer.AudioPlayer;
import data.DataManager;
import data.FileManager;
import data.SongData;
import static gui.retro.RetroTheme.*;

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
        RetroRackPanel filesRack = new RetroRackPanel(new BorderLayout(8, 8));

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
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(songList);
        scrollPane.getViewport().setBackground(LCD_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 2));
        filesRack.add(scrollPane, BorderLayout.CENTER);

        openButton = new JButton("Open");
        openButton.addActionListener(this);
        filesRack.add(openButton, BorderLayout.SOUTH);

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
        closeButton.setPreferredSize(new Dimension(40, 28));
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
        if (app.data.getSongs() != null) {
            for (SongData song : app.data.getSongs()) {
                songListModel.addElement(song);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == transportRack.getPlayButton()) {
            if (app.audioPlayer.isPaused()) {
                app.audioPlayer.resume();
            } else if (lastData != null) {
                app.audioPlayer._playAudio(lastData.getFilePath());
            }
        } else if (e.getSource() == transportRack.getPauseButton()) {
            app.audioPlayer.pause();
        } else if (e.getSource() == transportRack.getStopButton()) {
            app.audioPlayer.stop();
            transportRack.getTimeline().setCurrentTime(0);
            transportRack.getTimeline().setTotalTime(0);
        } else if (e.getSource() == openButton) {
            File file = app.fileManager.openFileDialog(FileManager.FileFilter.AUDIOFILE);
            if (file != null) {
                lastData = app.actionHandler.addSong(file);
                refreshSongList();
            }
        } else if (e.getSource() == closeButton) {
            mainWindow.dispose();
            System.exit(0);
        }
    }
}
