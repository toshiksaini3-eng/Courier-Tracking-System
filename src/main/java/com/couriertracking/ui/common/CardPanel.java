package com.couriertracking.ui.common;

import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Modern Card Container with rounded corners and clean border.
 */
public class CardPanel extends JPanel {
    private int cornerRadius = 12;

    public CardPanel() {
        super(new BorderLayout());
        setOpaque(false);
        setBorder(UIConstants.BORDER_CARD);
    }

    public CardPanel(int cornerRadius) {
        this();
        this.cornerRadius = cornerRadius;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Background
        g2.setColor(UIConstants.COLOR_CARD_BG);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Border
        g2.setColor(UIConstants.COLOR_CARD_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
