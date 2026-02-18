package data;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalData extends Data {
    public String guiStyle;
    public ArrayList<PlaylistData> playlists;
    public ArrayList<ProjectArtistData> projectsArtists;
    public HashMap<String, SongData> songs;
    public ArrayList<String> trackedFolders;
    public Object sortedByObject; // can be a playlist or a project/artist, used to sort the songs list in the UI

    public String getGuiStyle() {
        return guiStyle;
    }

    public ArrayList<PlaylistData> getPlaylists() {
        return playlists;
    }

    public ArrayList<ProjectArtistData> getProjectsArtists() {
        return projectsArtists;
    }

    public ArrayList<SongData> getSongs() {
        if (songs != null) {
            return new ArrayList<>(songs.values());
        } else {
            return new ArrayList<>();
        }
    }

    public SongData getSong(String filePath) {
        if (songs != null) {
            return songs.get(filePath);
        } else {
            return null;
        }
    }

    public ArrayList<String> getTrackedFolders() {
        return trackedFolders;
    }

    public void setGuiStyle(String guiStyle) {
        this.guiStyle = guiStyle;
    }

    public void setPlaylists(ArrayList<PlaylistData> playlists) {
        this.playlists = playlists;
    }

    public void setProjectsArtists(ArrayList<ProjectArtistData> projectsArtists) {
        this.projectsArtists = projectsArtists;
    }

    public void setTrackedFolders(ArrayList<String> trackedFolders) {
        this.trackedFolders = trackedFolders;
    }

    public ArrayList<SongData> getSongsSorted() {

        ArrayList<SongData> songsList = getSongs();
        if (sortedByObject instanceof PlaylistData) {
            return getSongsByPaths(((PlaylistData) sortedByObject).getSongPaths());
        } else if (sortedByObject instanceof ProjectArtistData) {
            return getSongsByPaths(((ProjectArtistData) sortedByObject).getSongPaths());
        } else {
            return songsList;
        }
    }

    public void setSortedByObject(Object sortedByObject) {
        this.sortedByObject = sortedByObject;
    }

    public void putSong(SongData newSong) {
        if (songs == null) {
            songs = new HashMap<>();
        }
        songs.put(newSong.getFilePath(), newSong);
    }

    public void removeSongByPath(String filePath) {
        if (songs != null) {
            songs.remove(filePath);
        }
    }

    public void addPlaylist(PlaylistData newPlaylist) {
        if (playlists == null) {
            playlists = new ArrayList<>();
        }
        playlists.add(newPlaylist);
    }

    public void addProjectArtist(ProjectArtistData newProjectArtist) {
        if (projectsArtists == null) {
            projectsArtists = new ArrayList<>();
        }
        projectsArtists.add(newProjectArtist);
    }

    public void addTrackedFolder(String newFolder) {
        if (trackedFolders == null) {
            trackedFolders = new ArrayList<>();
        }
        trackedFolders.add(newFolder);
    }

    public void removeSong(SongData songToRemove) {
        if (songs != null) {
            songs.remove(songToRemove.getFilePath());
        }
    }

    public void removePlaylist(PlaylistData playlistToRemove) {
        if (playlists != null) {
            playlists.remove(playlistToRemove);
        }
    }

    public void removeProjectArtist(ProjectArtistData projectArtistToRemove) {
        if (projectsArtists != null) {
            projectsArtists.remove(projectArtistToRemove);
        }
    }

    public void removeTrackedFolder(String folderToRemove) {
        if (trackedFolders != null) {
            trackedFolders.remove(folderToRemove);
        }
    }

    private ArrayList<SongData> getSongsByPaths(ArrayList<String> paths) {
        if (paths == null) {
            return new ArrayList<>();
        }
        ArrayList<SongData> result = new ArrayList<>();
        if (songs != null) {
            for (String path : paths) {
                SongData song = getSongByPath(path);
                if (song != null) {
                    result.add(song);
                }
            }
        }
        return result;
    }

    private SongData getSongByPath(String path) {
        ArrayList<SongData> songs = getSongs();
        if (songs != null) {
            for (SongData song : songs) {
                if (song.getFilePath().equals(path)) {
                    return song;
                }
            }
        }
        return null;
    }

}
