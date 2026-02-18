package audioPlayer;

import java.io.File;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.spi.AudioFileReader;

/**
 * Audio player supporting WAV, AIFF, AU, and MP3 (with MP3SPI).
 * 
 * Required JARs for MP3 support:
 * - mp3spi1.9.5.jar
 * - tritonus_share.jar
 * - jl1.0.1.jar
 */
public class AudioPlayer {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Clip clip;
    private AudioInputStream audioStream;
    private AudioInputStream decodedStream;
    private String currentFilePath;
    private float currentVolume = 1.0f;

    public static TransportState transportState = TransportState.STOPPED;

    public enum TransportState {
        PLAYING, PAUSED, STOPPED
    }

    public void play(String filePath) {
        executor.submit(() -> {
            System.out.println("Received request to play: " + filePath);
            if (currentFilePath != null && currentFilePath.equals(filePath) && clip != null && clip.isRunning()) {
                // If already playing the same file, do nothing
                return;
            }
            _stopAudio();
            _playAudio(filePath);
        });
    }

    public void stop() {
        executor.submit(this::_stopAudio);

    }

    public void pause() {
        executor.submit(this::_pauseAudio);
    }

    public void resume() {
        executor.submit(this::_resumeAudio);
    }

    public void setVolume(float volume) {
        executor.submit(() -> {
            currentVolume = Math.max(0.0f, Math.min(1.0f, volume));
            _applyVolume();
        });
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public boolean isPaused() {
        return clip != null && !clip.isRunning() && currentFilePath != null;
    }

    public long getPositionMs() {
        if (clip != null) {
            return clip.getMicrosecondPosition() / 1000;
        }
        return 0;
    }

    public long getDurationMs() {
        if (clip != null) {
            return clip.getMicrosecondLength() / 1000;
        }
        return 0;
    }

    public void seek(long positionMs) {
        executor.submit(() -> {
            if (clip != null) {
                clip.setMicrosecondPosition(positionMs * 1000);
            }
        });
    }

    private String getFileExtension(String filePath) {
        int lastDot = filePath.lastIndexOf('.');
        if (lastDot > 0) {
            return filePath.substring(lastDot + 1);
        }
        return "";
    }

    private void _stopAudio() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        transportState = TransportState.STOPPED;
        _closeStreams();
        currentFilePath = null;
    }

    private void _pauseAudio() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            transportState = TransportState.PAUSED;
        }
    }

    private void _resumeAudio() {
        if (clip != null && !clip.isRunning()) {
            clip.start();
            transportState = TransportState.PLAYING;
            System.out.println("Resuming audio: " + currentFilePath);
        }
    }

    private void _applyVolume() {
        _setVolumeImmediate(currentVolume);
    }

    /**
     * Sets the volume immediately without storing as current volume.
     * Used internally for fade effects.
     */
    private void _setVolumeImmediate(float volume) {
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Convert linear volume (0.0 - 1.0) to decibels
                float dB;
                if (volume <= 0.0f) {
                    dB = gainControl.getMinimum();
                } else {
                    dB = (float) (20.0 * Math.log10(volume));
                    // Clamp to valid range
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                }
                gainControl.setValue(dB);
            } catch (Exception e) {
                System.err.println("Volume control not available: " + e.getMessage());
            }
        }
    }

    /**
     * Performs a quick fade-in to prevent audio clicks at playback start.
     * 
     * @param durationMs Duration of the fade-in in milliseconds
     */
    private void _fadeIn(int durationMs) {
        int steps = 100;
        int stepDelay = durationMs / steps;

        for (int i = 1; i <= steps; i++) {
            float fadeVolume = (float) i / steps * currentVolume;
            _setVolumeImmediate(fadeVolume);
            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        // Ensure we end at the exact target volume
        _setVolumeImmediate(currentVolume);
    }

    private void _closeStreams() {
        if (decodedStream != null) {
            try {
                decodedStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            decodedStream = null;
        }
        if (audioStream != null) {
            try {
                audioStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioStream = null;
        }
    }

    public void _playAudio(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            System.err.println("Invalid file path: " + filePath);
            return;
        }
        if (clip != null && clip.isRunning()) {
            System.out.println("Stopping currently playing audio before starting new one.");
            _stopAudio();
        }
        try {
            File audioFile = new File(filePath);
            System.out.println("Attempting to play: " + audioFile.getAbsolutePath());

            String ext = getFileExtension(filePath).toLowerCase();
            System.out.println("File extension: " + ext);

            // For MP3 files, use AudioSystem which will use MP3SPI
            if (ext.equals("mp3")) {
                audioStream = AudioSystem.getAudioInputStream(audioFile);
                AudioFormat baseFormat = audioStream.getFormat();

                // Decode MP3 to PCM
                AudioFormat decodedFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        baseFormat.getSampleRate(),
                        16,
                        baseFormat.getChannels(),
                        baseFormat.getChannels() * 2,
                        baseFormat.getSampleRate(),
                        false);

                decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream);
                System.out.println("Decoded MP3 to PCM");
            } else {
                // For WAV, AIFF, AU - explicitly skip MP3SPI reader to avoid misdetection
                System.out.println("Playing native format for: " + ext);
                audioStream = getAudioInputStreamSkippingMp3Spi(audioFile);
                decodedStream = audioStream;
                System.out.println("Playing native format: " + audioStream.getFormat().getEncoding());
            }

            DataLine.Info info = new DataLine.Info(Clip.class, decodedStream.getFormat());
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(decodedStream);
            currentFilePath = filePath;

            // Start with silence and fade in to prevent click
            _setVolumeImmediate(0.0f);
            clip.start();
            transportState = TransportState.PLAYING;
            _fadeIn(20); // 20ms fade-in to eliminate click
            System.out.println("Playing audio: " + filePath);

        } catch (Exception e) {
            System.err.println("Error playing audio: " + e.getMessage());
            e.printStackTrace();
            _closeStreams();
        }
    }

    /**
     * Gets an AudioInputStream while skipping the MP3SPI reader.
     * This prevents MP3SPI from incorrectly detecting WAV/AIFF files as MPEG.
     */
    private AudioInputStream getAudioInputStreamSkippingMp3Spi(File file) throws Exception {
        ServiceLoader<AudioFileReader> readers = ServiceLoader.load(AudioFileReader.class);

        for (AudioFileReader reader : readers) {
            String readerName = reader.getClass().getName().toLowerCase();
            // Skip any MP3-related readers
            if (readerName.contains("mp3") || readerName.contains("mpeg")) {
                continue;
            }
            try {
                AudioInputStream stream = reader.getAudioInputStream(file);
                System.out.println("Using reader: " + reader.getClass().getName());
                return stream;
            } catch (Exception e) {
                // This reader can't handle the file, try next
            }
        }

        throw new javax.sound.sampled.UnsupportedAudioFileException(
                "No suitable audio reader found for: " + file.getName());
    }
}
