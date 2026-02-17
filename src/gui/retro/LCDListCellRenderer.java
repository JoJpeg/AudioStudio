package gui.retro;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import java.awt.Component;
import java.util.function.Function;
import static gui.retro.RetroTheme.*;

/**
 * Generic LCD-style list cell renderer that uses RetroTheme colors/fonts.
 * A labelProvider is used to obtain the display text for each value.
 */
public class LCDListCellRenderer extends DefaultListCellRenderer {
    private static final long serialVersionUID = 1L;
    private final Function<Object, String> labelProvider;

    public LCDListCellRenderer(Function<Object, String> labelProvider) {
        this.labelProvider = labelProvider;
        setOpaque(true);
        setFont(LCD_FONT);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        String text = (value == null) ? "" : labelProvider.apply(value);
        setText(text);
        setBackground(isSelected ? LCD_BACKGROUND_SELECTED : LCD_BACKGROUND);
        setForeground(isSelected ? LCD_TEXT_SELECTED : LCD_TEXT);
        return this;
    }
}
