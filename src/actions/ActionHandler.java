package actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import application.ApplicationResources;
import data.SongData;
import data.PlaylistData;
import data.ProjectArtistData;

public class ActionHandler {
    // This class will handle all actions that can be performed in the application,
    // such as creating a new project, adding a song to a playlist, etc.
    // here we will define methods for each action, and these methods will be called
    // from the GUI when the user performs an action.
    // this enabes us to also implement undo/redo functionality in the future,
    // as we can keep track of the actions performed by the user.

    ArrayList<Action> actionHistory;
    ApplicationResources app;

    public ActionHandler(ApplicationResources appData) {
        this.app = appData;
        actionHistory = new ArrayList<>();
    }

    public SongData addSong(File file) {

        Action addSongAction = new Action() {
            public SongData actionData = null;

            @Override
            public Action execute() {
                SongData newSong = new SongData();
                newSong.setFilePath(file.getAbsolutePath());
                this.actionData = newSong; // Store the newSong in the action's data for undo/redo purposes.
                app.data.addSong(newSong);
                String guessedTitle = "";
                String guessedArtist = "";

                long durationSeconds = data.FileManager.getSongDurationSeconds(file);
                newSong.setDurationSeconds(durationSeconds);

                // guess the title by splitting the filename by " - " and taking the first part
                // as artist and second part as title
                String filename = file.getName();
                // remove the file extension
                if (filename.contains(".")) {
                    filename = filename.substring(0, filename.lastIndexOf('.'));
                }
                if (filename.contains(" - ")) {
                    String[] parts = filename.split(" - ");
                    if (parts.length >= 2) {
                        guessedArtist = parts[0].trim();
                        guessedTitle = parts[1].trim();
                    }
                }

                newSong.setTitle(guessedTitle);
                newSong.setGuessedArtist(guessedArtist);

                app.fileManager.saveGlobals();
                return this; // Return the action itself for undo functionality.
            }

            @Override
            public Action undo() {
                app.data.removeSong(this.actionData);
                app.fileManager.saveGlobals();
                return this; // Return the action itself for redo functionality.
            }

            @Override
            public Object getData() {
                return this.actionData;
            }
        };
        executeAction(addSongAction);
        return (SongData) addSongAction.getData();

    }

    /**
     * Create a new playlist via an Action so it can be undone/redone later.
     */
    public PlaylistData createPlaylist(String title) {
        Action action = new Action() {
            PlaylistData created = null;

            @Override
            public Action execute() {
                created = new PlaylistData();
                created.setTitle(title);
                app.data.addPlaylist(created);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                app.data.removePlaylist(created);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return created;
            }
        };
        executeAction(action);
        return (PlaylistData) action.getData();
    }

    /**
     * Create a new project/artist via an Action.
     */
    public ProjectArtistData createProjectArtist(String name) {
        Action action = new Action() {
            ProjectArtistData created = null;

            @Override
            public Action execute() {
                created = new ProjectArtistData();
                created.setName(name);
                created.setCreated(new java.util.Date());
                app.data.addProjectArtist(created);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                app.data.removeProjectArtist(created);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return created;
            }
        };
        executeAction(action);
        return (ProjectArtistData) action.getData();
    }

    /**
     * Rename a playlist (undoable).
     */
    public void renamePlaylist(PlaylistData playlist, String newTitle) {
        if (playlist == null)
            return;
        Action action = new Action() {
            String oldTitle = playlist.getTitle();

            @Override
            public Action execute() {
                playlist.setTitle(newTitle);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                playlist.setTitle(oldTitle);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return playlist;
            }
        };
        executeAction(action);
    }

    /**
     * Rename a project/artist (undoable).
     */
    public void renameProjectArtist(ProjectArtistData pa, String newName) {
        if (pa == null)
            return;
        Action action = new Action() {
            String oldName = pa.getName();

            @Override
            public Action execute() {
                pa.setName(newName);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                pa.setName(oldName);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return pa;
            }
        };
        executeAction(action);
    }

    /**
     * Delete playlist (undoable).
     */
    public void deletePlaylist(PlaylistData playlist) {
        if (playlist == null)
            return;
        Action action = new Action() {
            PlaylistData removed = playlist;

            @Override
            public Action execute() {
                app.data.removePlaylist(removed);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                app.data.addPlaylist(removed);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return removed;
            }
        };
        executeAction(action);
    }

    /**
     * Delete project/artist (undoable).
     */
    public void deleteProjectArtist(ProjectArtistData pa) {
        if (pa == null)
            return;
        Action action = new Action() {
            ProjectArtistData removed = pa;

            @Override
            public Action execute() {
                app.data.removeProjectArtist(removed);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                app.data.addProjectArtist(removed);
                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                return removed;
            }
        };
        executeAction(action);
    }

    /**
     * Scan songs without owners and assign them to ProjectArtist entries based on
     * their guessedArtist. "Unknown" (case-insensitive) or empty guessed names
     * are ignored. This operation is undoable and returns the number of songs
     * assigned.
     */
    public int sortSongsToArtists() {
        Action action = new Action() {
            Map<ProjectArtistData, List<SongData>> assignments = new HashMap<>();
            List<ProjectArtistData> createdProjects = new ArrayList<>();

            @Override
            public Action execute() {
                if (app.data == null || app.data.getSongs() == null)
                    return this;

                for (SongData s : app.data.getSongs()) {
                    if (s == null)
                        continue;
                    if (s.getOwners() != null && !s.getOwners().isEmpty())
                        continue; // already has owners

                    String guessed = s.getGuessedArtist();
                    if (guessed == null)
                        continue;
                    guessed = guessed.trim();
                    if (guessed.isEmpty() || guessed.equalsIgnoreCase("unknown"))
                        continue;

                    // find existing project/artist
                    ProjectArtistData target = null;
                    if (app.data.getProjectsArtists() != null) {
                        for (ProjectArtistData pa : app.data.getProjectsArtists()) {
                            if (pa.getName() != null && pa.getName().equalsIgnoreCase(guessed)) {
                                target = pa;
                                break;
                            }
                        }
                    }

                    if (target == null) {
                        target = new ProjectArtistData();
                        target.setName(guessed);
                        target.setCreated(new java.util.Date());
                        app.data.addProjectArtist(target);
                        createdProjects.add(target);
                    }

                    // attach song to project and mark owner
                    target.addSong(s);
                    if (s.getOwners() == null)
                        s.setOwners(new ArrayList<>());
                    s.getOwners().add(target);

                    assignments.computeIfAbsent(target, k -> new ArrayList<>()).add(s);
                }

                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Action undo() {
                // remove song assignments
                for (Map.Entry<ProjectArtistData, List<SongData>> e : assignments.entrySet()) {
                    ProjectArtistData pa = e.getKey();
                    for (SongData s : e.getValue()) {
                        if (s.getOwners() != null)
                            s.getOwners().remove(pa);
                        pa.removeSong(s);
                    }
                }

                // remove created projects
                for (ProjectArtistData created : createdProjects) {
                    app.data.removeProjectArtist(created);
                }

                app.fileManager.saveGlobals();
                return this;
            }

            @Override
            public Object getData() {
                int count = 0;
                for (List<SongData> l : assignments.values())
                    count += l.size();
                return count;
            }
        };

        executeAction(action);
        Object data = action.getData();
        return (data instanceof Integer) ? (Integer) data : 0;
    }

    private void executeAction(Action action) {
        action.execute();
        actionHistory.add(action);
    }

    public void undoLastAction() {
        if (!actionHistory.isEmpty()) {
            Action lastAction = actionHistory.remove(actionHistory.size() - 1);
            lastAction.undo();
        }
    }

}
