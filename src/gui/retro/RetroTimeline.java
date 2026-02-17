package gui.retro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import static gui.retro.RetroTheme.*;

/**
 * A retro-styled timeline component with LCD display and LED-style progress bar.
 */
public class RetroTimeline extends JPanel {

    private static final int DISPLAY_WIDTH = 120;
    private static final int DISPLAY_HEIGHT = 32;
    private static final int BAR_HEIGHT = 20;
    private static final int BAR_SEGMENT_WIDTH = 6;
    private static final int BAR_SEGMENT_GAP = 2;

    private long currentTimeMs = 0;
    private long totalTimeMs = 0;
    private boolean isDragging = false;

    private TimelineListener listener;

    // Colors for LED segments
    private static final Color LED_OFF = new Color(20, 50, 20);
    private static final Color LED_ON = new Color(80, 255, 80);
    private static final Color LED_ON_GLOW = new Color(120, 255, 120, 100);

    public interface TimelineListener {
        void onSeek(long timeMs);
    }

    public RetroTimeline() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, 50));
        setupMouseListeners();
    }

    public void setTimelineListener(TimelineListener listener) {
        this.listener = listener;
    }

    public void setCurrentTime(long timeMs) {
        this.currentTimeMs = Math.max(0, Math.min(timeMs, totalTimeMs));
        repaint();
    }

    public void setTotalTime(long timeMs) {
        this.totalTimeMs = Math.max(0, timeMs);
        repaint();
    }

    public long getCurrentTime() {
        return currentTimeMs;
    }

    public long getTotalTime() {
        return totalTimeMs;
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isInBarArea(e.getPoint())) {
                    isDragging = true;
                    updateTimeFromMouse(e.getX());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    isDragging = false;
                    if (listener != null) {
                        listener.onSeek(currentTimeMs);
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    updateTimeFromMouse(e.getX());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                boolean inBar = isInBarArea(e.getPoint());
                setCursor(inBar ? new Cursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
    }

    private boolean isInBarArea(Point p) {
        int barStartX = DISPLAY_WIDTH + 20;
        int barEndX = getWidth() - 15;
        int barY = (getHeight() - BAR_HEIGHT) / 2;
        return p.x >= barStartX && p.x <= barEndX &&
                p.y >= barY && p.y <= barY + BAR_HEIGHT;
    }

    private void updateTimeFromMouse(int mouseX) {
        int barStartX = DISPLAY_WIDTH + 20;
        int barEndX = getWidth() - 15;
        int barWidth = barEndX - barStartX;

        if (barWidth > 0 && totalTimeMs > 0) {
            double ratio = (double) (mouseX - barStartX) / barWidth;
            ratio = Math.max(0, Math.min(1, ratio));
            currentTimeMs = (long) (ratio * totalTimeMs);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;

        // Draw time display panel (left)
        drawTimeDisplay(g2d, 10, centerY - DISPLAY_HEIGHT / 2, DISPLAY_WIDTH, DISPLAY_HEIGHT);

        // Draw LED progress bar
        int barStartX = DISPLAY_WIDTH + 20;
        int barEndX = width - 15;
        int barY = centerY - BAR_HEIGHT / 2;
        drawLEDBar(g2d, barStartX, barY, barEndX - barStartX, BAR_HEIGHT);

        g2d.dispose();
    }

    private void drawTimeDisplay(Graphics2D g2d, int x, int y, int width, int height) {
        // Draw LCD background with beveled border (inset effect)
        g2d.setColor(PANEL_DARK_SHADOW);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRect(x + 1, y + 1, width - 1, height - 1);
        g2d.setColor(LCD_BACKGROUND);
        g2d.fillRect(x + 2, y + 2, width - 3, height - 3);

        // Draw scanline effect
        g2d.setColor(new Color(0, 20, 0, 30));
        for (int i = y + 2; i < y + height - 2; i += 2) {
            g2d.drawLine(x + 2, i, x + width - 3, i);
        }

        // Format time string: "00:00 / 00:00"
        String timeText = formatTime(currentTimeMs) + " / " + formatTime(totalTimeMs);

        // Draw text
        g2d.setColor(LCD_TEXT);
        g2d.setFont(LCD_FONT);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(timeText)) / 2;
        int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(timeText, textX, textY);
    }

    private void drawLEDBar(Graphics2D g2d, int x, int y, int width, int height) {
        // Draw bar background (inset)
        g2d.setColor(PANEL_DARK_SHADOW);
        g2d.fillRoundRect(x - 4, y - 4, width + 8, height + 8, 6, 6);
        g2d.setColor(PANEL_SHADOW);
        g2d.fillRoundRect(x - 3, y - 3, width + 6, height + 6, 5, 5);
        g2d.setColor(new Color(25, 25, 28));
        g2d.fillRoundRect(x - 2, y - 2, width + 4, height + 4, 4, 4);

        // Calculate number of segments
        int segmentStep = BAR_SEGMENT_WIDTH + BAR_SEGMENT_GAP;
        int numSegments = (width - BAR_SEGMENT_GAP) / segmentStep;

        // Calculate how many segments should be lit
        double ratio = totalTimeMs > 0 ? (double) currentTimeMs / totalTimeMs : 0;
        int litSegments = (int) (numSegments * ratio);

        // Draw segments
        for (int i = 0; i < numSegments; i++) {
            int segX = x + i * segmentStep;
            boolean isLit = i < litSegments;

            if (isLit) {
                // Draw glow effect behind lit segment
                g2d.setColor(LED_ON_GLOW);
                g2d.fillRoundRect(segX - 1, y - 1, BAR_SEGMENT_WIDTH + 2, height + 2, 2, 2);

                // Draw lit segment with gradient
                GradientPaint ledGradient = new GradientPaint(
                        segX, y, LED_ON,
                        segX, y + height, new Color(50, 200, 50));
                g2d.setPaint(ledGradient);
                g2d.fillRoundRect(segX, y, BAR_SEGMENT_WIDTH, height, 2, 2);

                // Highlight on top
                g2d.setColor(new Color(180, 255, 180, 150));
                g2d.fillRoundRect(segX + 1, y + 1, BAR_SEGMENT_WIDTH - 2, 3, 1, 1);
            } else {
                // Draw off segment
                GradientPaint offGradient = new GradientPaint(
                        segX, y, LED_OFF,
                        segX, y + height, new Color(15, 35, 15));
                g2d.setPaint(offGradient);
                g2d.fillRoundRect(segX, y, BAR_SEGMENT_WIDTH, height, 2, 2);

                // Subtle highlight
                g2d.setColor(new Color(30, 60, 30, 100));
                g2d.fillRoundRect(segX + 1, y + 1, BAR_SEGMENT_WIDTH - 2, 2, 1, 1);
            }

            // Draw segment border
            g2d.setColor(new Color(10, 30, 10));
            g2d.drawRoundRect(segX, y, BAR_SEGMENT_WIDTH - 1, height - 1, 2, 2);
        }
    }

    private String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
