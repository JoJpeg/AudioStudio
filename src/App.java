import application.ApplicationResources;
import audioPlayer.AudioPlayer;
import data.DataManager;
import data.FileManager;
import gui.retro.AudioStudio;

public class App {
    public static ApplicationResources data;

    public static void main(String[] args) throws Exception {
        ApplicationResources appData = new ApplicationResources();
        data = appData;
        new AudioStudio(appData);
    }
}
