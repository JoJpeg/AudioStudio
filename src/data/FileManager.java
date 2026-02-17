package data;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import application.ApplicationResources;

public class FileManager {
    String applicationPath = "";
    ApplicationResources app;
    Gson gson;

    public FileManager(ApplicationResources appData) {
        this.app = appData;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            applicationPath = new File(new File(".").getCanonicalPath()).getPath() + "/data/";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum FileFilter {
        SONGFILE, AUDIOFILE, PLAYLISTFILE, PROJECTFILE, ARTISTFILE, IMAGEFILE, NONE
    }

    public GlobalData getGlobalData() {
        if (app.data == null) {
            loadGlobalData();
        }
        return app.data;
    }

    private void loadGlobalData() {
        // Load global data from a file, e.g. using Gson
        try {
            String path = applicationPath + File.separator + "globalData.json";
            File file = new File(path);

            app.data = gson.fromJson(new FileReader(file), GlobalData.class);
        } catch (Exception e) {
            e.printStackTrace();
            app.data = new GlobalData(); // Create new if loading fails
        }

    }

    public void saveGlobals() {
        System.out.println("Saving global data...");
        if (app.data == null) {
            System.out.println("Warning: globalData is null, nothing to save.");
            return;
        }
        saveData(app.data, new File(applicationPath + File.separator + "globalData.json"));
    }

    private ArrayList<File> getFolderContents(File folder) {
        ArrayList<File> contents = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    contents.add(file);
                }
            }
        }
        return contents;
    }

    public File openFileDialog(FileFilter filter) {
        FileDialog fileDialog = new FileDialog((Frame) null, "Open File", FileDialog.LOAD);

        // Set filename filter based on the filter type
        if (filter != FileFilter.NONE) {
            final Set<String> extensions = switch (filter) {
                case AUDIOFILE -> Set.of("mp3", "wav", "flac", "aiff", "aac", "ogg");
                case SONGFILE -> Set.of("ast");
                case PLAYLISTFILE -> Set.of("aspl");
                case PROJECTFILE -> Set.of("asproj");
                case ARTISTFILE -> Set.of("asart");
                case IMAGEFILE -> Set.of("png", "jpg", "jpeg", "gif");
                default -> Set.of();
            };

            if (!extensions.isEmpty()) {
                fileDialog.setFilenameFilter((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return extensions.stream().anyMatch(ext -> lowerName.endsWith("." + ext));
                });
            }
        }

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            return new File(directory, filename);
        }
        return null;
    }

    public void saveData(Data data, File file) {
        // Save data to a file, e.g. using Gson
        System.out.println("saveData called with data type: " + (data != null ? data.getClass().getName() : "null"));
        System.out.println("Saving to file: " + file.getAbsolutePath());

        if (data instanceof GlobalData) {
            data = (GlobalData) data;
            try {
                String json = gson.toJson(data);
                java.nio.file.Files.write(file.toPath(), json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (data instanceof SongData) {
            data = (SongData) data;
            try {
                String json = gson.toJson(data);
                java.nio.file.Files.write(file.toPath(), json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (data instanceof PlaylistData) {
            data = (PlaylistData) data;
            try {
                String json = gson.toJson(data);
                java.nio.file.Files.write(file.toPath(), json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (data instanceof ProjectArtistData) {
            data = (ProjectArtistData) data;
            try {
                String json = gson.toJson(data);
                java.nio.file.Files.write(file.toPath(), json.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static int getSongDurationSeconds(File file) {
        try {
            javax.sound.sampled.AudioFileFormat fileFormat = javax.sound.sampled.AudioSystem.getAudioFileFormat(file);

            // For MP3 files (via MP3SPI), duration is available in properties as
            // microseconds
            java.util.Map<String, Object> properties = fileFormat.properties();
            if (properties != null && properties.containsKey("duration")) {
                Long microseconds = (Long) properties.get("duration");
                if (microseconds != null) {
                    return (int) (microseconds / 1_000_000);
                }
            }

            // Fallback for standard audio formats (WAV, AIFF, etc.)
            javax.sound.sampled.AudioFormat format = fileFormat.getFormat();
            long frames = fileFormat.getFrameLength();
            float frameRate = format.getFrameRate();
            if (frames > 0 && frameRate > 0) {
                return (int) (frames / frameRate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Return 0 if duration cannot be determined
    }

}
