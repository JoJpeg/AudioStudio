package data;

import java.util.ArrayList;

public class GlobalData extends Data {
    public String guiStyle;
    public ArrayList<PlaylistData> playlists;
    public ArrayList<ProjectArtistData> projectsArtists;
    public ArrayList<SongData> songs;
    public ArrayList<String> trackedFolders;
    transient Object sortedByObject = null;

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
        return songs;
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

    public void setSongs(ArrayList<SongData> songs) {
        this.songs = songs;
    }

    public void setTrackedFolders(ArrayList<String> trackedFolders) {
        this.trackedFolders = trackedFolders;
    }

    public ArrayList<SongData> getSongsSorted() {
        if (sortedByObject instanceof PlaylistData) {
            return ((PlaylistData) sortedByObject).getSongs();
        } else if (sortedByObject instanceof ProjectArtistData) {
            return ((ProjectArtistData) sortedByObject).getSongs();
        } else {
            return songs;
        }
    }

    public void setSortedByObject(Object sortedByObject) {
        this.sortedByObject = sortedByObject;
    }

    public void addSong(SongData newSong) {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        songs.add(newSong);
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
            songs.remove(songToRemove);
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

}
