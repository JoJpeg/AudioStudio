package data;

import java.util.ArrayList;


public class PlaylistData extends Data {
    String title;
    ArrayList<String> songPaths; //TODO: save only a reference to the song, currentyl serialiting creates a vicious loop
    Note note; 
    

    public PlaylistData() {
    }

    public Note getNote() {
        return note;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getSongPaths() {
        return songPaths;
    }
 

    public void setNote(Note note) {
        this.note = note;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addSong(SongData newSong) {
        if (songPaths == null) {
            songPaths = new ArrayList<>();
        }
        songPaths.add(newSong.getFilePath());
    }

    public void removeSong(SongData songToRemove) {
        if (songPaths != null) {
            songPaths.remove(songToRemove.getFilePath());
        }
    }

}
