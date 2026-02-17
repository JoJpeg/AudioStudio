package gui.retro;

import javax.swing.JButton;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static gui.retro.RetroTheme.*;

/**
 * Button styled like RetroTransportButton but for labelled actions.
 * Uses RetroTheme colors and an embossed look so it matches transport buttons.
 */
public class RetroActionButton extends JButton {
    private static final long serialVersionUID = 1L;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RetroActionButton(String text) {
        super(text);
        // Compact dimensions for tight, CD-player-like layout
        setPreferredSize(new Dimension(72, 22));
        setMinimumSize(new Dimension(48, 18));
        setMaximumSize(new Dimension(120, 28));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        // use a slightly smaller LCD font so labels fit better
        setFont(LCD_FONT.deriveFont(Font.BOLD, 11f));
        setForeground(LCD_TEXT);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new java.awt.Insets(1, 4, 1, 4));

        addMouseListener(new MouseAdapter() {
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
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background (embossed)
        if (isPressed) {
            g2d.setColor(BUTTON_PRESSED);
            g2d.fillRect(0, 0, w, h);

            g2d.setColor(BUTTON_DARK_SHADOW);
            g2d.drawLine(0, 0, w - 1, 0);
            g2d.drawLine(0, 0, 0, h - 1);
        } else {
            g2d.setColor(isHovered ? getButtonFaceHover() : BUTTON_FACE);
            g2d.fillRect(0, 0, w, h);

            g2d.setColor(BUTTON_HIGHLIGHT);
            g2d.drawLine(0, 0, w - 1, 0);
            g2d.drawLine(0, 0, 0, h - 1);

            g2d.setColor(BUTTON_DARK_SHADOW);
            g2d.drawLine(0, h - 1, w - 1, h - 1);
            g2d.drawLine(w - 1, 0, w - 1, h - 1);
        }

        // Text
        g2d.setFont(getFont());
        FontMetrics fm = g2d.getFontMetrics();
        String txt = getText();
        int txtW = fm.stringWidth(txt);
        int txtH = fm.getAscent();
        int x = (w - txtW) / 2;
        int y = (h + txtH) / 2 - 3;

        g2d.setColor(isPressed ? LCD_TEXT_SELECTED : LCD_TEXT);
        g2d.drawString(txt, x, y);

        g2d.dispose();
    }
}
