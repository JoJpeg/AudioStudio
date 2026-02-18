package gui.retro;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import data.SongData;
import gui.retro.RetroTransportButton.TransportType;

public class TransportRack extends RetroRackPanel {

    private RetroTransportButton playButton;
    private RetroTransportButton pauseButton;
    private RetroTransportButton stopButton;
    

    private RetroTimeline timeline;
    private SongLCDPanel songPanel;
    private LCDParentPanel lcdPanel;

    public TransportRack(ActionListener listener) {
        // reduce vertical gaps to make rack compact
        super(new BorderLayout(0, 2));

        // Song LCD panel (now smaller by default)
        songPanel = new SongLCDPanel();

        // Create buttons panel with tighter gaps
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
        buttonsPanel.setOpaque(false);

        playButton = new RetroTransportButton(TransportType.PLAY);
        playButton.addActionListener(listener);

        pauseButton = new RetroTransportButton(TransportType.PAUSE);
        pauseButton.addActionListener(listener);

        stopButton = new RetroTransportButton(TransportType.STOP);
        stopButton.addActionListener(listener);



        buttonsPanel.add(playButton);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(stopButton);

        // Create timeline
        timeline = new RetroTimeline();
        lcdPanel = new LCDParentPanel(songPanel, timeline);

        add(buttonsPanel, BorderLayout.NORTH);
        add(lcdPanel, BorderLayout.SOUTH);
        // add(timeline, BorderLayout.SOUTH);
    }

    public RetroTransportButton getPlayButton() {
        return playButton;
    }

    public RetroTransportButton getPauseButton() {
        return pauseButton;
    }

    public RetroTransportButton getStopButton() {
        return stopButton;
    }

    public RetroTimeline getTimeline() {
        return timeline;
    }

    public SongLCDPanel getSongPanel() {
        return songPanel;
    }

    public void setSong(SongData song) {
        songPanel.setSong(song);
    }
}
