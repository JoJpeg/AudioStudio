package gui;

public class GuiManager {
    private static GuiManager instance;

    private GuiManager() {
        // Private constructor to prevent instantiation
    }

    public static GuiManager get() {
        if (instance == null) {
            instance = new GuiManager();
        }
        return instance;
    }

    // Add methods to manage GUI components here
}
