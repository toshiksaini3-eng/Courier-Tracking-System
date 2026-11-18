package com.couriertracking.ui.panel;

import com.couriertracking.util.DatabaseConnection;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Modern Deep Navy Sidebar with active highlight tabs and database status.
 */
public class SidebarPanel extends JPanel {

    public enum NavItem {
        DASHBOARD("Dashboard", "📊"),
        BOOK_PARCEL("Book Parcel", "📦"),
        TRACK_PARCEL("Track Parcel", "🔍"),
        MANAGE_PARCELS("Manage Parcels", "📋"),
        UPDATE_STATUS("Update Status", "🔄"),
        BRANCH_MANAGEMENT("Branch Management", "🏢"),
        DELIVERY_REPORTS("Delivery Reports", "📈"),
        SIMULATION("Live Simulator", "⚡"),
        ABOUT_PROJECT("About Project", "ℹ️"),
        EXIT("Exit Application", "🚪");

        private final String title;
        private final String icon;

        NavItem(String title, String icon) {
            this.title = title;
            this.icon = icon;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }
    }

    private final Consumer<NavItem> onNavigation;
    private final Runnable onOpenDbConfig;
    private final List<SidebarButton> buttons = new ArrayList<>();
    private NavItem activeItem = NavItem.DASHBOARD;
    private JLabel lblDbStatus;

    public SidebarPanel(Consumer<NavItem> onNavigation, Runnable onOpenDbConfig) {
        this.onNavigation = onNavigation;
        this.onOpenDbConfig = onOpenDbConfig;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_SIDEBAR_BG);
        setPreferredSize(new Dimension(240, 700));

        initComponents();
    }

    private void initComponents() {
        // 1. Brand Logo Header
        JPanel brandPanel = new JPanel();
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JLabel logo = new JLabel("🚚 CRX EXPRESS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Courier & Logistics System");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UIConstants.COLOR_SIDEBAR_TEXT_MUTED);

        brandPanel.add(logo);
        brandPanel.add(Box.createVerticalStrut(4));
        brandPanel.add(sub);

        // 2. Navigation Items List
        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(6, 12, 12, 12));

        for (NavItem item : NavItem.values()) {
            SidebarButton btn = new SidebarButton(item);
            buttons.add(btn);
            navPanel.add(btn);
            navPanel.add(Box.createVerticalStrut(4));
        }

        // 3. Database Status Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        lblDbStatus = new JLabel("● " + DatabaseConnection.getInstance().getActiveDatabaseType());
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblDbStatus.setForeground(DatabaseConnection.getInstance().isMySQL() ? 
                new Color(52, 211, 153) : new Color(251, 191, 36));

        JLabel btnDbSettings = new JLabel("⚙ Settings");
        btnDbSettings.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDbSettings.setForeground(UIConstants.COLOR_SIDEBAR_TEXT_MUTED);
        btnDbSettings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDbSettings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (onOpenDbConfig != null) {
                    onOpenDbConfig.run();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                btnDbSettings.setForeground(Color.WHITE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnDbSettings.setForeground(UIConstants.COLOR_SIDEBAR_TEXT_MUTED);
            }
        });

        footerPanel.add(lblDbStatus, BorderLayout.WEST);
        footerPanel.add(btnDbSettings, BorderLayout.EAST);

        add(brandPanel, BorderLayout.NORTH);
        add(navPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        setActiveItem(NavItem.DASHBOARD);
    }

    public void setActiveItem(NavItem item) {
        this.activeItem = item;
        for (SidebarButton btn : buttons) {
            btn.setActive(btn.item == item);
        }
    }

    public void updateDbStatus() {
        lblDbStatus.setText("● " + DatabaseConnection.getInstance().getActiveDatabaseType());
        lblDbStatus.setForeground(DatabaseConnection.getInstance().isMySQL() ? 
                new Color(52, 211, 153) : new Color(251, 191, 36));
    }

    private class SidebarButton extends JPanel {
        private final NavItem item;
        private boolean isActive = false;
        private boolean isHover = false;
        private final JLabel label;

        public SidebarButton(NavItem item) {
            this.item = item;
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(216, 42));
            setMaximumSize(new Dimension(216, 42));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));

            label = new JLabel(item.getIcon() + "  " + item.getTitle());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setForeground(UIConstants.COLOR_SIDEBAR_TEXT);
            add(label, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    setActiveItem(item);
                    if (onNavigation != null) {
                        onNavigation.accept(item);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            label.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            label.setForeground(active ? Color.WHITE : UIConstants.COLOR_SIDEBAR_TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (isActive) {
                g2.setColor(UIConstants.COLOR_SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            } else if (isHover) {
                g2.setColor(UIConstants.COLOR_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
