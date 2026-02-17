package gui.retro;

import javax.swing.JList;
import java.awt.Component;
import java.util.function.Function;
import static gui.retro.RetroTheme.*;

/**
 * Generic LCD-style list cell renderer that uses RetroTheme colors/fonts.
 * A labelProvider is used to obtain the display text for each value.
 */
public class LCDListCellRenderer extends BaseLCDListCellRenderer {
    private static final long serialVersionUID = 1L;
    private final Function<Object, String> labelProvider;
    private final Function<Object, String> secondaryProvider; // nullable

    public LCDListCellRenderer(Function<Object, String> labelProvider) {
        this(labelProvider, null);
    }

    public LCDListCellRenderer(Function<Object, String> labelProvider, Function<Object, String> secondaryProvider) {
        this.labelProvider = labelProvider;
        this.secondaryProvider = secondaryProvider;
        setOpaque(true);
        setFont(LCD_FONT);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        // If a secondary provider is supplied, render a two-column component from the base class.
        if (secondaryProvider != null) {
            String primary = (value == null) ? "" : labelProvider.apply(value);
            String secondary = (value == null) ? "" : secondaryProvider.apply(value);
            return twoColumnComponent(primary, secondary, isSelected, index);
        }

        // Fallback: single-label renderer (existing behavior)
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String text = (value == null) ? "" : labelProvider.apply(value);
        setText(text);
        applySingleLabelStyle(this, isSelected);
        return this;
    }
}
