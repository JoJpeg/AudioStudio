package data;

import java.util.ArrayList;
import java.util.Date;

public class ProjectArtistData extends Data {

    enum ProjectType {
        ARTIST,
        PROJECT,
        LABEL
    }

    ProjectType projectType;
    String name; // Worldroom
    Date created;
    Note note;
    String description;
    String imagePath;
    ArrayList<SongData> songs;
    ProjectArtistData owner;
    ArrayList<ProjectArtistData> children;

    public ArrayList<SongData> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<SongData> songs) {
        this.songs = songs;
    }

    public void addSong(SongData newSong) {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        songs.add(newSong);
    }

    public ProjectType getProjectType() {
        return projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public ProjectArtistData getOwner() {
        return owner;
    }

    public void setOwner(ProjectArtistData owner) {
        this.owner = owner;
    }

    public ArrayList<ProjectArtistData> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<ProjectArtistData> children) {
        this.children = children;
    }

    public void addChild(ProjectArtistData child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public void removeChild(ProjectArtistData child) {
        if (children != null) {
            children.remove(child);
        }
    }

    public void removeSong(SongData song) {
        if (songs != null) {
            songs.remove(song);
        }
    }
}
