package data;

import java.util.ArrayList;


public class PlaylistData extends Data {
    String title;
    ArrayList<SongData> songs; //TODO: save only a reference to the song, currentyl serialiting creates a vicious loop
    Note note;

    public ArrayList<SongData> getSongs() {
        return songs;
    }

    public Note getNote() {
        return note;
    }

    public String getTitle() {
        return title;
    }

    public void setSongs(ArrayList<SongData> songs) {
        this.songs = songs;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void addSong(SongData newSong) {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        songs.add(newSong);
    }

    public void removeSong(SongData songToRemove) {
        if (songs != null) {
            songs.remove(songToRemove);
        }
    }

}
