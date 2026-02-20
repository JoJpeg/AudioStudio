package data;

import java.util.ArrayList;

public class VersionedSongData extends Data {

    ArrayList<SongData> versions;
    SongData starred;
    boolean isEmpty = true;

    public void addSong(SongData newVersion) {
        if (versions == null) {
            versions = new ArrayList<>();
        }
        versions.add(newVersion);
        if (starred == null) {
            starred = newVersion;
        }
        newVersion.setParent(this);
        isEmpty = false;
    }

    void removeSong(SongData versionToRemove) {
        if (versions != null) {
            if (versions.contains(versionToRemove)) {
                versions.remove(versionToRemove);
                if (starred == versionToRemove) {
                    starred = versions.isEmpty() ? null : versions.get(0);
                }
                versionToRemove.setParent(null);
            }

        }
        if (versions == null || versions.isEmpty()) {
            isEmpty = true;
        }
    }

    public void setStarred(SongData starred) {
        if(starred != null && !versions.contains(starred)) {
            throw new IllegalArgumentException("The starred version must be one of the versions in the list.");
        }
        this.starred = starred;
    }

}
