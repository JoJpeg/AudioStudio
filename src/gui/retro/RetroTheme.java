package gui.retro;

import java.awt.Color;
import java.awt.Font;

/**
 * Centralized theme file for all colors and fonts used in the Retro GUI.
 */
public class RetroTheme {

    // ========== Window & Panel Colors ==========
    public static final Color WINDOW_BACKGROUND = new Color(40, 40, 45);
    public static final Color WINDOW_BORDER = new Color(60, 60, 65);

    // ========== Header Colors ==========
    public static final Color HEADER_BACKGROUND = new Color(50, 50, 55);
    public static final Color HEADER_BORDER = new Color(70, 70, 75);
    public static final Color HEADER_TEXT = new Color(200, 200, 200);
    public static final Color HEADER_BUTTON_TEXT = new Color(180, 180, 180);
    public static final Color HEADER_BUTTON_HOVER = Color.WHITE;
    public static final Color HEADER_CLOSE_HOVER_BG = new Color(200, 70, 70);

    // ========== Retro Panel Colors (Embossed Gray) ==========
    public static final Color PANEL_FACE = new Color(192, 192, 192);
    public static final Color PANEL_HIGHLIGHT = new Color(255, 255, 255);
    public static final Color PANEL_SHADOW = new Color(128, 128, 128);
    public static final Color PANEL_DARK_SHADOW = new Color(64, 64, 64);
    public static final Color PANEL_INNER_HIGHLIGHT = new Color(230, 230, 230);

    // ========== Screw Colors ==========
    public static final Color SCREW_OUTER = new Color(100, 100, 100);
    public static final Color SCREW_INNER = new Color(160, 160, 160);
    public static final Color SCREW_SLOT = new Color(50, 50, 50);
    public static final Color SCREW_HIGHLIGHT = new Color(200, 200, 200);

    // ========== Button Colors ==========
    public static final Color BUTTON_FACE = new Color(192, 192, 192);
    public static final Color BUTTON_HIGHLIGHT = new Color(255, 255, 255);
    public static final Color BUTTON_SHADOW = new Color(128, 128, 128);
    public static final Color BUTTON_DARK_SHADOW = new Color(64, 64, 64);
    public static final Color BUTTON_PRESSED = new Color(172, 172, 172);
    public static final Color BUTTON_INNER_HIGHLIGHT = new Color(230, 230, 230);

    // ========== Transport Icon Colors ==========
    public static final Color ICON_PLAY = new Color(34, 139, 34);
    public static final Color ICON_STOP = new Color(60, 60, 60);
    public static final Color ICON_PAUSE = new Color(60, 60, 60);
    public static final Color ICON_RECORD = new Color(200, 30, 30);
    public static final Color ICON_REWIND_FORWARD = new Color(60, 60, 60);

    // ========== LCD Display Colors ==========
    public static final Color LCD_BACKGROUND = new Color(15, 30, 15);
    public static final Color LCD_BACKGROUND_ALT = new Color(20, 40, 20);
    public static final Color LCD_BACKGROUND_SELECTED = new Color(30, 80, 30);
    public static final Color LCD_TEXT = new Color(50, 255, 50);
    public static final Color LCD_TEXT_SELECTED = new Color(150, 255, 150);
    public static final Color LCD_BORDER = new Color(30, 60, 30);

    // ========== Fonts ==========
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 13);
    public static final Font HEADER_BUTTON_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font LCD_FONT = new Font("Monospaced", Font.BOLD, 14);

    // ========== Utility Methods ==========

    /**
     * Returns a brighter version of the button face color for hover effects.
     */
    public static Color getButtonFaceHover() {
        return BUTTON_FACE.brighter();
    }

    /**
     * Returns a brighter version of the panel face color for hover effects.
     */
    public static Color getPanelFaceHover() {
        return PANEL_FACE.brighter();
    }
}
