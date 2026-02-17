package gui.retro;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import gui.retro.RetroTransportButton.TransportType;

public class TransportRack extends RetroRackPanel {

    private RetroTransportButton playButton;
    private RetroTransportButton pauseButton;
    private RetroTransportButton stopButton;
    private RetroTimeline timeline;

    public TransportRack(ActionListener listener) {
        super(new BorderLayout(0, 8));

        // Create buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
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

        add(buttonsPanel, BorderLayout.NORTH);
        add(timeline, BorderLayout.CENTER);
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
}
