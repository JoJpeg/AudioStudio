package actions;

import java.io.File;
import java.util.ArrayList;
 
import application.ApplicationResources;
import data.SongData;

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
