package gui.retro;

import static gui.retro.RetroTheme.LCD_BACKGROUND;
import static gui.retro.RetroTheme.LCD_BORDER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;

import application.ApplicationResources;
import data.PlaylistData;
import data.ProjectArtistData;

/**
 * Encapsulates the left "Groups" rack (Playlists / Projects) including
 * UI and actions. Calls back via {@code onFilterChange} when the selected
 * filter changes so the parent can refresh song list / UI.
 */
public class GroupRack implements ActionListener {
    private final ApplicationResources app;
    private final Component parentComponent; // used for dialogs
    private final Runnable onFilterChange; // callback to refresh songs when filter changes

    private final RetroRackPanel rackPanel;
    private JTabbedPane groupTabs;

    private DefaultListModel<PlaylistData> playlistListModel;
    private JList<PlaylistData> playlistList;

    private DefaultListModel<ProjectArtistData> projectListModel;
    private JList<ProjectArtistData> projectList;

    private javax.swing.JPanel tabActionPanel;
    private RetroActionButton actionMenuButton;
    private JPopupMenu actionPopupMenu;

    public GroupRack(ApplicationResources app, Component parentComponent, Runnable onFilterChange) {
        this.app = app;
        this.parentComponent = parentComponent;
        this.onFilterChange = onFilterChange;

        rackPanel = new RetroRackPanel(new BorderLayout(2, 2));
        rackPanel.setPreferredSize(new Dimension(220, 0));

        groupTabs = new JTabbedPane();

        // Playlists
        playlistListModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistListModel);
        playlistList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
                    notifyFilterChanged();
                }
            }
        });

        JScrollPane playlistScroll = new JScrollPane(playlistList);
        playlistScroll.getViewport().setBackground(LCD_BACKGROUND);
        playlistScroll.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        groupTabs.addTab("Playlists", playlistScroll);

        // Projects
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
                    notifyFilterChanged();
                }
            }
        });

        JScrollPane projectScroll = new JScrollPane(projectList);
        projectScroll.getViewport().setBackground(LCD_BACKGROUND);
        projectScroll.setBorder(BorderFactory.createLineBorder(LCD_BORDER, 1));
        groupTabs.addTab("Projects", projectScroll);

        // add tabbed pane
        rackPanel.add(groupTabs, BorderLayout.CENTER);

        // action panel (compact menu)
        tabActionPanel = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        tabActionPanel.setOpaque(false);
        tabActionPanel.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

        actionMenuButton = new RetroActionButton("Actions");
        actionPopupMenu = new JPopupMenu();
        actionMenuButton.addActionListener(evt -> showActionMenu(actionMenuButton));

        groupTabs.addChangeListener(ev -> updateTabActions());

        rackPanel.add(tabActionPanel, BorderLayout.SOUTH);

        // populate
        refreshPlaylistList();
        refreshProjectList();
        updateTabActions();
    }

    public RetroRackPanel getPanel() {
        return rackPanel;
    }

    public javax.swing.JPanel getGroupActionPanel() {
        return tabActionPanel;
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

    /**
     * Programmatically select a project/artist in the Projects tab and apply
     * the corresponding filter (used by external UI components).
     */
    public void selectProject(ProjectArtistData pa) {
        if (pa == null)
            return;
        // ensure projects are up-to-date
        refreshProjectList();
        projectList.setSelectedValue(pa, true);
        app.data.setSortedByObject(pa);
        playlistList.clearSelection();
        groupTabs.setSelectedIndex(1); // switch to Projects tab
        notifyFilterChanged();
    }

    private void notifyFilterChanged() {
        if (onFilterChange != null) {
            onFilterChange.run();
        }
    }

    /* ---------------------- actions ---------------------- */
    private void createNewPlaylist() {
        String name = JOptionPane.showInputDialog(parentComponent, "Playlist name:", "New Playlist",
                JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            PlaylistData pl = app.actionHandler.createPlaylist(name.trim());
            refreshPlaylistList();
            playlistList.setSelectedValue(pl, true);
            app.data.setSortedByObject(pl);
            notifyFilterChanged();
        }
    }

    private void createNewProject() {
        String name = JOptionPane.showInputDialog(parentComponent, "Artist/Project name:", "New Project/Artist",
                JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            ProjectArtistData pa = app.actionHandler.createProjectArtist(name.trim());
            refreshProjectList();
            projectList.setSelectedValue(pa, true);
            app.data.setSortedByObject(pa);
            notifyFilterChanged();
        }
    }

    private void editSelectedGroup() {
        int idx = groupTabs.getSelectedIndex();
        if (idx == 0) { // playlist rename
            PlaylistData sel = playlistList.getSelectedValue();
            if (sel != null) {
                String name = JOptionPane.showInputDialog(parentComponent, "Playlist name:", sel.getTitle());
                if (name != null && !name.trim().isEmpty()) {
                    app.actionHandler.renamePlaylist(sel, name.trim());
                    refreshPlaylistList();
                    playlistList.setSelectedValue(sel, true);
                }
            }
        } else if (idx == 1) { // project rename
            ProjectArtistData sel = projectList.getSelectedValue();
            if (sel != null) {
                String name = JOptionPane.showInputDialog(parentComponent, "Artist/Project name:", sel.getName());
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
                int resp = JOptionPane.showConfirmDialog(parentComponent, "Delete playlist '" + sel.getTitle() + "'?",
                        "Delete Playlist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    app.actionHandler.deletePlaylist(sel);
                    refreshPlaylistList();
                    app.data.setSortedByObject(null);
                    notifyFilterChanged();
                }
            }
        } else if (idx == 1) { // delete project
            ProjectArtistData sel = projectList.getSelectedValue();
            if (sel != null) {
                int resp = JOptionPane.showConfirmDialog(parentComponent,
                        "Delete project/artist '" + sel.getName() + "'?",
                        "Delete Project/Artist", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (resp == JOptionPane.YES_OPTION) {
                    app.actionHandler.deleteProjectArtist(sel);
                    refreshProjectList();
                    app.data.setSortedByObject(null);
                    notifyFilterChanged();
                }
            }
        }
    }

    private void clearGroupFilter() {
        app.data.setSortedByObject(null);
        playlistList.clearSelection();
        projectList.clearSelection();
        notifyFilterChanged();
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
                notifyFilterChanged();
                JOptionPane.showMessageDialog(parentComponent, assigned + " songs were assigned to projects/artists.",
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

    private void updateTabActions() {
        if (tabActionPanel == null)
            return;
        tabActionPanel.removeAll();

        // single compact actions menu (menu contents change depending on selected tab)
        tabActionPanel.add(actionMenuButton);

        tabActionPanel.revalidate();
        tabActionPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // only used by actionMenuButton currently
        showActionMenu(actionMenuButton);
    }
}
