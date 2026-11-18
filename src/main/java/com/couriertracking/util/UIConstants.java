package com.couriertracking.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;

/**
 * Design System Constants for Courier Tracking System.
 * Clean, professional logistics SaaS aesthetic: Blue, Navy, White, Light Slate.
 */
public final class UIConstants {
    private UIConstants() {}

    // Primary Colors
    public static final Color COLOR_PRIMARY = new Color(37, 99, 235);       // #2563EB Royal Blue
    public static final Color COLOR_PRIMARY_DARK = new Color(30, 58, 138);  // #1E3A8A Navy
    public static final Color COLOR_PRIMARY_HOVER = new Color(29, 78, 216); // #1D4ED8
    public static final Color COLOR_ACCENT = new Color(14, 165, 233);       // #0EA5E9 Sky

    // Sidebar & Navigation Colors
    public static final Color COLOR_SIDEBAR_BG = new Color(15, 23, 42);     // #0F172A Deep Slate Navy
    public static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59);  // #1E293B
    public static final Color COLOR_SIDEBAR_ACTIVE = new Color(37, 99, 235); // #2563EB
    public static final Color COLOR_SIDEBAR_TEXT = new Color(226, 232, 240); // #E2E8F0
    public static final Color COLOR_SIDEBAR_TEXT_MUTED = new Color(148, 163, 184); // #94A3B8

    // Background & Surfaces
    public static final Color COLOR_BACKGROUND = new Color(248, 250, 252);  // #F8FAFC Ultra Light Gray
    public static final Color COLOR_CARD_BG = Color.WHITE;
    public static final Color COLOR_CARD_BORDER = new Color(226, 232, 240); // #E2E8F0
    public static final Color COLOR_CARD_BORDER_HOVER = new Color(148, 163, 184);

    // Text Colors
    public static final Color COLOR_TEXT_PRIMARY = new Color(15, 23, 42);   // #0F172A
    public static final Color COLOR_TEXT_SECONDARY = new Color(71, 85, 105); // #475569
    public static final Color COLOR_TEXT_MUTED = new Color(148, 163, 184);  // #94A3B8

    // Status Colors
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981 Emerald
    public static final Color COLOR_WARNING = new Color(245, 158, 11);      // #F59E0B Amber
    public static final Color COLOR_DANGER = new Color(239, 68, 68);        // #EF4444 Red
    public static final Color COLOR_INFO = new Color(6, 182, 212);          // #06B6D4 Cyan

    // Typography
    public static final Font FONT_HEADER_LARGE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER_MEDIUM = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.BOLD, 13);

    // Standard Borders
    public static final Border BORDER_CARD = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
    );

    public static final Border BORDER_EMPTY_PADDING = BorderFactory.createEmptyBorder(20, 24, 20, 24);

    /**
     * Styles a modern button with rounded corners and consistent padding.
     */
    public static void stylePrimaryButton(JButton button) {
        button.setBackground(COLOR_PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(241, 245, 249));
        button.setForeground(COLOR_TEXT_PRIMARY);
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleSuccessButton(JButton button) {
        button.setBackground(COLOR_SUCCESS);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }

    public static void styleDangerButton(JButton button) {
        button.setBackground(COLOR_DANGER);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BODY_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
    }
}
