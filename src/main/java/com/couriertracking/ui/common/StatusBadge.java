package com.couriertracking.ui.common;

import com.couriertracking.model.ParcelStatus;
import com.couriertracking.util.UIConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Colored pill badge for visualizing parcel status in tables and headers.
 */
public class StatusBadge extends JLabel {
    private ParcelStatus status;
    private Color badgeColor;
    private Color textColor;

    public StatusBadge(ParcelStatus status) {
        super(status != null ? status.getDisplayName() : "Unknown", SwingConstants.CENTER);
        setStatus(status);
        setFont(new Font("Segoe UI", Font.BOLD, 11));
        setPreferredSize(new Dimension(110, 26));
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
        if (status != null) {
            setText(status.getDisplayName());
            this.badgeColor = status.getBackgroundColor();
            this.textColor = status.getPrimaryColor();
        } else {
            setText("N/A");
            this.badgeColor = new Color(241, 245, 249);
            this.textColor = UIConstants.COLOR_TEXT_SECONDARY;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw pill background
        g2.setColor(badgeColor);
        g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);

        // Draw pill border
        g2.setColor(textColor);
        g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 14, 14);

        g2.dispose();

        setForeground(textColor);
        super.paintComponent(g);
    }
}
