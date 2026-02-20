package data;

import java.util.ArrayList;
import java.util.Date;

public class SongData extends Data {

    String title;
    String guessedArtist;
    String subtitle;
    String filePath;
    int version;
    Note note;
    ArrayList<ProjectArtistData> owners;
    ArrayList<Date> changeHistory;
    VersionedSongData parent;
    transient long durationSeconds = -1;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGuessedArtist(String guessedArtist) {
        this.guessedArtist = guessedArtist;
    }

    public String getGuessedArtist() {
        return guessedArtist;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public ArrayList<ProjectArtistData> getOwners() {
        return owners;
    }

    public void setOwners(ArrayList<ProjectArtistData> owners) {
        this.owners = owners;
    }

    public long getDurationSeconds() {
        if (durationSeconds < 0 && filePath != null) {
            durationSeconds = FileManager.getSongDurationSeconds(new java.io.File(filePath));
        }
        return durationSeconds;
    }

    public void setDurationSeconds(long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public VersionedSongData getParent() {
        return parent;
    }

    public void setParent(VersionedSongData parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    // generate hash
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        return result;
    }
}
