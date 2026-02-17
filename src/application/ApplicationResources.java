package application;

import actions.ActionHandler;
import audioPlayer.AudioPlayer;
import data.DataManager;
import data.FileManager;
import data.GlobalData;

public class ApplicationResources {

    public DataManager dataManager;
    public FileManager fileManager;
    public AudioPlayer audioPlayer;
    public ActionHandler actionHandler;
    public GlobalData data;

    public ApplicationResources() {
        dataManager = new DataManager();
        fileManager = new FileManager(this);
        audioPlayer = new AudioPlayer();
        actionHandler = new ActionHandler(this);
        data = fileManager.getGlobalData(); // Load the global data from the file manager
        if (data == null) {
            data = new GlobalData(); // Create new if loading fails
        }
    }
}
