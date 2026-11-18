package com.couriertracking.ui.common;

import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Metric KPI Card for Dashboard analytics.
 */
public class StatCard extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblValue;
    private final JLabel lblSubtitle;
    private Color accentColor;

    public StatCard(String title, String value, String subtitle, Color accentColor) {
        super(new BorderLayout(10, 8));
        this.accentColor = accentColor;
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        setPreferredSize(new Dimension(190, 105));

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setOpaque(false);

        lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitle.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblSubtitle = new JLabel(subtitle != null ? subtitle : "");
        lblSubtitle.setFont(UIConstants.FONT_SMALL);
        lblSubtitle.setForeground(UIConstants.COLOR_TEXT_MUTED);

        content.add(lblTitle);
        content.add(javax.swing.Box.createVerticalStrut(4));
        content.add(lblValue);
        if (subtitle != null && !subtitle.isEmpty()) {
            content.add(javax.swing.Box.createVerticalStrut(4));
            content.add(lblSubtitle);
        }

        add(content, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String subtitle) {
        lblSubtitle.setText(subtitle);
    }

    public void setAccentColor(Color color) {
        this.accentColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card Background
        g2.setColor(UIConstants.COLOR_CARD_BG);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        // Accent bar on top or left
        g2.setColor(accentColor);
        g2.fillRoundRect(0, 0, 5, getHeight() - 1, 4, 4);

        // Border
        g2.setColor(UIConstants.COLOR_CARD_BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
