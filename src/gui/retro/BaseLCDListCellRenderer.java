package gui.retro;

import javax.swing.*;
import java.awt.*;

import static gui.retro.RetroTheme.*;

/**
 * Base renderer that centralizes LCD styling, alternating-row coloring and a
 * reusable two-column (primary + secondary) layout used by list renderers.
 */
public abstract class BaseLCDListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;

    /**
     * Create a two-column component (primary centered, secondary right-aligned)
     * that matches the RetroTheme look used throughout the UI.
     */
    protected Component twoColumnComponent(String primaryText, String secondaryText, boolean isSelected, int index) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel primary = new JLabel(primaryText);
        JLabel secondary = new JLabel(secondaryText);

        primary.setFont(LCD_FONT);
        secondary.setFont(LCD_FONT);

        panel.add(primary, BorderLayout.CENTER);
        panel.add(secondary, BorderLayout.EAST);

        Color bgColor = isSelected ? LCD_BACKGROUND_SELECTED : (index % 2 == 0 ? LCD_BACKGROUND : LCD_BACKGROUND_ALT);
        Color fgColor = isSelected ? LCD_TEXT_SELECTED : LCD_TEXT;

        panel.setBackground(bgColor);
        primary.setForeground(fgColor);
        secondary.setForeground(fgColor);

        panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        panel.setOpaque(true);
        primary.setOpaque(false);
        secondary.setOpaque(false);

        // Ensure consistent row height used by previous implementation
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 32));

        return panel;
    }

    /**
     * Apply the standard single-label LCD look to a renderer component.
     * Call from subclasses that render directly using the DefaultListCellRenderer.
     *
     * Uses the row index to alternate background color when the row is not
     * selected (keeps selected color precedence).
     */
    protected void applySingleLabelStyle(Component rendererComponent, boolean isSelected, int index) {
        rendererComponent.setFont(LCD_FONT);
        if (rendererComponent instanceof JComponent) {
            JComponent jc = (JComponent) rendererComponent;
            jc.setOpaque(true);
            Color bgColor = isSelected ? LCD_BACKGROUND_SELECTED : (index % 2 == 0 ? LCD_BACKGROUND : LCD_BACKGROUND_ALT);
            Color fgColor = isSelected ? LCD_TEXT_SELECTED : LCD_TEXT;
            jc.setBackground(bgColor);
            jc.setForeground(fgColor);
        }
    }
}
