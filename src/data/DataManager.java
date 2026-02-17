package data;

import java.nio.channels.FileLock;
import java.util.List;
import java.util.stream.Collectors;

public class DataManager {
    /*
     * Handle Playlists
     * Handle song data
     * Handle ownership of songs by projects/artists
     * Handle Versions of Songfiles
     * Handle recently opened files
     */
 
    private FileManager fileManager;
 
    
    public void createPlaylist(PlaylistData newPlaylist) {
        // Create a new playlist
    }

    public List<SongData> getSongsByArtist(ProjectArtistData artist) {
        return fileManager.getGlobalData().getSongs().stream()
                .filter(song -> song.getOwners().contains(artist))
                .collect(Collectors.toList());
        // Return a list of songs owned by the given artist
    }
}
