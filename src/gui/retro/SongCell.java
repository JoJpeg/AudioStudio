package gui.retro;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JList;

public class SongCell extends BaseLCDListCellRenderer {
    boolean isExpanded = false;
    // a song cell shows a song name/title
    // if it has sub versions it can fold/unfold
    // to show versions of it
    // when a SongCell is clicked the Inspector
    // should show its data
    // double click shold play it
    // it has a context menu that makes it easy
    // to add / remove it from projects/artsts/queue
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
        data.SongData song = (data.SongData) value;
        boolean expanded = song != null && isExpanded;

        Component c = twoColumnComponent(song == null ? "" : song.getTitle(),
                song == null ? "" : String.valueOf(song.getDurationSeconds()),
                isSelected, index);

        Dimension pref = c.getPreferredSize();
        c.setPreferredSize(new Dimension(pref.width, expanded ? 96 : 32)); // taller when expanded
        return c;
    }
}
