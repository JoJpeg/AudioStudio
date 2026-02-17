package gui.retro;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import static gui.retro.RetroTheme.*;

public class RetroTransportButton extends JButton {

    public enum TransportType {
        PLAY, PAUSE, STOP, REWIND, FORWARD, RECORD
    }

    private TransportType transportType;
    private boolean isPressed = false;
    private boolean isHovered = false;

    public RetroTransportButton(TransportType type) {
        this.transportType = type;
        // smaller transport buttons for compact rack
        setPreferredSize(new Dimension(40, 28));
        setMinimumSize(new Dimension(36, 24));
        setMaximumSize(new Dimension(64, 34));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw the embossed button background
        drawEmbossedBackground(g2d, width, height);

        // Draw the transport icon
        drawTransportIcon(g2d, width, height);

        g2d.dispose();
    }

    private void drawEmbossedBackground(Graphics2D g2d, int width, int height) {
        if (isPressed) {
            // Pressed state - inverted emboss effect
            g2d.setColor(BUTTON_PRESSED);
            g2d.fillRect(0, 0, width, height);

            // Dark shadow on top and left (pressed in)
            g2d.setColor(BUTTON_DARK_SHADOW);
            g2d.drawLine(0, 0, width - 1, 0);
            g2d.drawLine(0, 0, 0, height - 1);

            g2d.setColor(BUTTON_SHADOW);
            g2d.drawLine(1, 1, width - 2, 1);
            g2d.drawLine(1, 1, 1, height - 2);

            // Light on bottom and right
            g2d.setColor(BUTTON_HIGHLIGHT);
            g2d.drawLine(0, height - 1, width - 1, height - 1);
            g2d.drawLine(width - 1, 0, width - 1, height - 1);
        } else {
            // Normal state - classic embossed look
            g2d.setColor(isHovered ? getButtonFaceHover() : BUTTON_FACE);
            g2d.fillRect(0, 0, width, height);

            // Outer highlight (top-left)
            g2d.setColor(BUTTON_HIGHLIGHT);
            g2d.drawLine(0, 0, width - 1, 0);
            g2d.drawLine(0, 0, 0, height - 1);

            // Inner highlight
            g2d.setColor(BUTTON_INNER_HIGHLIGHT);
            g2d.drawLine(1, 1, width - 2, 1);
            g2d.drawLine(1, 1, 1, height - 2);

            // Outer shadow (bottom-right)
            g2d.setColor(BUTTON_DARK_SHADOW);
            g2d.drawLine(0, height - 1, width - 1, height - 1);
            g2d.drawLine(width - 1, 0, width - 1, height - 1);

            // Inner shadow
            g2d.setColor(BUTTON_SHADOW);
            g2d.drawLine(1, height - 2, width - 2, height - 2);
            g2d.drawLine(width - 2, 1, width - 2, height - 2);
        }
    }

    private void drawTransportIcon(Graphics2D g2d, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int offset = isPressed ? 1 : 0;

        switch (transportType) {
            case PLAY:
                drawPlayIcon(g2d, centerX + offset, centerY + offset);
                break;
            case PAUSE:
                drawPauseIcon(g2d, centerX + offset, centerY + offset);
                break;
            case STOP:
                drawStopIcon(g2d, centerX + offset, centerY + offset);
                break;
            case REWIND:
                drawRewindIcon(g2d, centerX + offset, centerY + offset);
                break;
            case FORWARD:
                drawForwardIcon(g2d, centerX + offset, centerY + offset);
                break;
            case RECORD:
                drawRecordIcon(g2d, centerX + offset, centerY + offset);
                break;
        }
    }

    private void drawPlayIcon(Graphics2D g2d, int cx, int cy) {
        int[] xPoints = { cx - 6, cx - 6, cx + 8 };
        int[] yPoints = { cy - 8, cy + 8, cy };

        // Draw shadow for 3D effect
        g2d.setColor(BUTTON_DARK_SHADOW);
        int[] xShadow = { cx - 5, cx - 5, cx + 9 };
        int[] yShadow = { cy - 7, cy + 9, cy + 1 };
        g2d.fillPolygon(xShadow, yShadow, 3);

        // Draw green play triangle
        g2d.setColor(ICON_PLAY);
        g2d.fillPolygon(xPoints, yPoints, 3);

        // Highlight edge
        g2d.setColor(ICON_PLAY.brighter());
        g2d.drawLine(xPoints[0], yPoints[0], xPoints[2], yPoints[2]);
    }

    private void drawPauseIcon(Graphics2D g2d, int cx, int cy) {
        int barWidth = 4;
        int barHeight = 14;
        int gap = 3;

        // Left bar shadow
        g2d.setColor(BUTTON_DARK_SHADOW);
        g2d.fillRect(cx - gap - barWidth + 1, cy - barHeight / 2 + 1, barWidth, barHeight);

        // Right bar shadow
        g2d.fillRect(cx + gap + 1, cy - barHeight / 2 + 1, barWidth, barHeight);

        // Left bar
        g2d.setColor(ICON_PAUSE);
        g2d.fillRect(cx - gap - barWidth, cy - barHeight / 2, barWidth, barHeight);

        // Right bar
        g2d.fillRect(cx + gap, cy - barHeight / 2, barWidth, barHeight);
    }

    private void drawStopIcon(Graphics2D g2d, int cx, int cy) {
        int size = 12;

        // Shadow
        g2d.setColor(BUTTON_DARK_SHADOW);
        g2d.fillRect(cx - size / 2 + 1, cy - size / 2 + 1, size, size);

        // Stop square
        g2d.setColor(ICON_STOP);
        g2d.fillRect(cx - size / 2, cy - size / 2, size, size);
    }

    private void drawRewindIcon(Graphics2D g2d, int cx, int cy) {
        int triWidth = 8;
        int triHeight = 10;

        // First triangle (left)
        int[] x1 = { cx - 2, cx - 2 - triWidth, cx - 2 };
        int[] y1 = { cy - triHeight / 2, cy, cy + triHeight / 2 };

        // Second triangle (right)
        int[] x2 = { cx + 6, cx + 6 - triWidth, cx + 6 };
        int[] y2 = { cy - triHeight / 2, cy, cy + triHeight / 2 };

        g2d.setColor(BUTTON_DARK_SHADOW);
        g2d.fillPolygon(new int[] { x1[0] + 1, x1[1] + 1, x1[2] + 1 }, new int[] { y1[0] + 1, y1[1] + 1, y1[2] + 1 },
                3);
        g2d.fillPolygon(new int[] { x2[0] + 1, x2[1] + 1, x2[2] + 1 }, new int[] { y2[0] + 1, y2[1] + 1, y2[2] + 1 },
                3);

        g2d.setColor(ICON_REWIND_FORWARD);
        g2d.fillPolygon(x1, y1, 3);
        g2d.fillPolygon(x2, y2, 3);
    }

    private void drawForwardIcon(Graphics2D g2d, int cx, int cy) {
        int triWidth = 8;
        int triHeight = 10;

        // First triangle (left)
        int[] x1 = { cx - 6, cx - 6 + triWidth, cx - 6 };
        int[] y1 = { cy - triHeight / 2, cy, cy + triHeight / 2 };

        // Second triangle (right)
        int[] x2 = { cx + 2, cx + 2 + triWidth, cx + 2 };
        int[] y2 = { cy - triHeight / 2, cy, cy + triHeight / 2 };

        g2d.setColor(BUTTON_DARK_SHADOW);
        g2d.fillPolygon(new int[] { x1[0] + 1, x1[1] + 1, x1[2] + 1 }, new int[] { y1[0] + 1, y1[1] + 1, y1[2] + 1 },
                3);
        g2d.fillPolygon(new int[] { x2[0] + 1, x2[1] + 1, x2[2] + 1 }, new int[] { y2[0] + 1, y2[1] + 1, y2[2] + 1 },
                3);

        g2d.setColor(ICON_REWIND_FORWARD);
        g2d.fillPolygon(x1, y1, 3);
        g2d.fillPolygon(x2, y2, 3);
    }

    private void drawRecordIcon(Graphics2D g2d, int cx, int cy) {
        int radius = 7;

        // Shadow
        g2d.setColor(BUTTON_DARK_SHADOW);
        g2d.fillOval(cx - radius + 1, cy - radius + 1, radius * 2, radius * 2);

        // Red record circle
        g2d.setColor(ICON_RECORD);
        g2d.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // Highlight
        g2d.setColor(ICON_RECORD.brighter());
        g2d.drawArc(cx - radius + 2, cy - radius + 2, radius - 2, radius - 2, 45, 90);
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType type) {
        this.transportType = type;
        repaint();
    }
}
