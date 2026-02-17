package gui.retro;

import javax.swing.*;
import java.awt.*;
import static gui.retro.RetroTheme.*;

public class RetroRackPanel extends JPanel {

    // Reduced thickness/margins to make panels more compact
    private static final int BORDER_THICKNESS = 2;
    private static final int SCREW_SIZE = 14;
    private static final int SCREW_MARGIN = 6;
    private static final int CONTENT_PADDING = SCREW_MARGIN + SCREW_SIZE + 2; // smaller padding to save space

    public RetroRackPanel() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
                CONTENT_PADDING,
                CONTENT_PADDING,
                CONTENT_PADDING,
                CONTENT_PADDING));
    }

    public RetroRackPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(
                CONTENT_PADDING,
                CONTENT_PADDING,
                CONTENT_PADDING,
                CONTENT_PADDING));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Draw the embossed panel background
        drawEmbossedBackground(g2d, width, height);

        // Draw screws in each corner
        drawScrew(g2d, SCREW_MARGIN, SCREW_MARGIN); // Top-left
        drawScrew(g2d, width - SCREW_MARGIN - SCREW_SIZE, SCREW_MARGIN); // Top-right
        drawScrew(g2d, SCREW_MARGIN, height - SCREW_MARGIN - SCREW_SIZE); // Bottom-left
        drawScrew(g2d, width - SCREW_MARGIN - SCREW_SIZE, height - SCREW_MARGIN - SCREW_SIZE); // Bottom-right

        g2d.dispose();

        super.paintComponent(g);
    }

    private void drawEmbossedBackground(Graphics2D g2d, int width, int height) {
        // Fill main background
        g2d.setColor(PANEL_FACE);
        g2d.fillRect(0, 0, width, height);

        // Draw outer embossed border (raised effect)
        // Top highlight
        g2d.setColor(PANEL_HIGHLIGHT);
        for (int i = 0; i < BORDER_THICKNESS; i++) {
            g2d.drawLine(i, i, width - 1 - i, i);
            g2d.drawLine(i, i, i, height - 1 - i);
        }

        // Bottom shadow
        g2d.setColor(PANEL_DARK_SHADOW);
        for (int i = 0; i < BORDER_THICKNESS; i++) {
            g2d.drawLine(i, height - 1 - i, width - 1 - i, height - 1 - i);
            g2d.drawLine(width - 1 - i, i, width - 1 - i, height - 1 - i);
        }

        // Inner shadow line for depth
        g2d.setColor(PANEL_SHADOW);
        g2d.drawLine(BORDER_THICKNESS, height - BORDER_THICKNESS - 1, width - BORDER_THICKNESS - 1,
                height - BORDER_THICKNESS - 1);
        g2d.drawLine(width - BORDER_THICKNESS - 1, BORDER_THICKNESS, width - BORDER_THICKNESS - 1,
                height - BORDER_THICKNESS - 1);

        // Draw inner recessed area
        int inset = BORDER_THICKNESS + 2;

        // Inner top-left shadow (recessed)
        g2d.setColor(PANEL_SHADOW);
        g2d.drawLine(inset, inset, width - inset - 1, inset);
        g2d.drawLine(inset, inset, inset, height - inset - 1);

        // Inner bottom-right highlight (recessed)
        g2d.setColor(PANEL_HIGHLIGHT);
        g2d.drawLine(inset, height - inset - 1, width - inset - 1, height - inset - 1);
        g2d.drawLine(width - inset - 1, inset, width - inset - 1, height - inset - 1);

        // Subtle texture/grain effect
        g2d.setColor(new Color(180, 180, 180, 30));
        for (int y = 0; y < height; y += 2) {
            g2d.drawLine(0, y, width, y);
        }
    }

    private void drawScrew(Graphics2D g2d, int x, int y) {
        // Outer shadow ring
        g2d.setColor(PANEL_DARK_SHADOW);
        g2d.fillOval(x + 1, y + 1, SCREW_SIZE, SCREW_SIZE);

        // Outer screw ring
        g2d.setColor(SCREW_OUTER);
        g2d.fillOval(x, y, SCREW_SIZE, SCREW_SIZE);

        // Inner screw face
        g2d.setColor(SCREW_INNER);
        g2d.fillOval(x + 2, y + 2, SCREW_SIZE - 4, SCREW_SIZE - 4);

        // Screw highlight (top-left)
        g2d.setColor(SCREW_HIGHLIGHT);
        g2d.drawArc(x + 2, y + 2, SCREW_SIZE - 5, SCREW_SIZE - 5, 45, 135);

        // Screw shadow (bottom-right)
        g2d.setColor(SCREW_OUTER);
        g2d.drawArc(x + 3, y + 3, SCREW_SIZE - 5, SCREW_SIZE - 5, 225, 135);

        // Draw Phillips head slot (cross pattern)
        int centerX = x + SCREW_SIZE / 2;
        int centerY = y + SCREW_SIZE / 2;
        int slotLength = 4;

        g2d.setColor(SCREW_SLOT);
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Horizontal slot
        g2d.drawLine(centerX - slotLength, centerY, centerX + slotLength, centerY);
        // Vertical slot
        g2d.drawLine(centerX, centerY - slotLength, centerX, centerY + slotLength);

        // Reset stroke
        g2d.setStroke(new BasicStroke(1f));
    }
}
