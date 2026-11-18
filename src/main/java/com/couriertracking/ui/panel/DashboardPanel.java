package com.couriertracking.ui.panel;

import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.TrackingService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.common.StatCard;
import com.couriertracking.ui.dialog.ReceiptViewDialog;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

/**
 * Main Operations Dashboard displaying live KPIs, recent shipments, and activity audit feed.
 * All metrics are computed dynamically from MySQL / JDBC.
 */
public class DashboardPanel extends JPanel {
    private final ParcelService parcelService;
    private final TrackingService trackingService;
    private final Consumer<String> onNavigateToTrack;
    private final Runnable onNavigateToBook;

    private StatCard cardTotal;
    private StatCard cardBooked;
    private StatCard cardInTransit;
    private StatCard cardOutForDelivery;
    private StatCard cardDelivered;
    private StatCard cardPending;

    private ModernTable tblRecentParcels;
    private DefaultTableModel modelRecentParcels;

    private ModernTable tblActivities;
    private DefaultTableModel modelActivities;

    private JLabel lblLastUpdated;
    private List<Parcel> currentRecentList;

    public DashboardPanel(ParcelService parcelService, TrackingService trackingService,
                          Consumer<String> onNavigateToTrack, Runnable onNavigateToBook) {
        this.parcelService = parcelService;
        this.trackingService = trackingService;
        this.onNavigateToTrack = onNavigateToTrack;
        this.onNavigateToBook = onNavigateToBook;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadDashboardData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Action & Summary Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftActions.setOpaque(false);

        JButton btnBookQuick = new JButton("+ Book New Parcel");
        UIConstants.stylePrimaryButton(btnBookQuick);
        btnBookQuick.addActionListener(e -> {
            if (onNavigateToBook != null) onNavigateToBook.run();
        });

        JButton btnRefresh = new JButton("🔄 Refresh Data");
        UIConstants.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadDashboardData());

        leftActions.add(btnBookQuick);
        leftActions.add(btnRefresh);

        lblLastUpdated = new JLabel("Live Database Synchronized");
        lblLastUpdated.setFont(UIConstants.FONT_SMALL);
        lblLastUpdated.setForeground(UIConstants.COLOR_TEXT_MUTED);

        topBar.add(leftActions, BorderLayout.WEST);
        topBar.add(lblLastUpdated, BorderLayout.EAST);

        // 2. Six KPI Cards Grid
        JPanel statsGrid = new JPanel(new GridLayout(2, 3, 16, 16));
        statsGrid.setOpaque(false);
        statsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        cardTotal = new StatCard("Total Parcels", "0", "All time bookings", UIConstants.COLOR_PRIMARY);
        cardBooked = new StatCard("Booked Parcels", "0", "Awaiting dispatch", ParcelStatus.BOOKED.getPrimaryColor());
        cardInTransit = new StatCard("In Transit", "0", "En route on network", ParcelStatus.IN_TRANSIT.getPrimaryColor());
        cardOutForDelivery = new StatCard("Out for Delivery", "0", "With delivery agent", ParcelStatus.OUT_FOR_DELIVERY.getPrimaryColor());
        cardDelivered = new StatCard("Delivered Parcels", "0", "Completed deliveries", ParcelStatus.DELIVERED.getPrimaryColor());
        cardPending = new StatCard("Pending Deliveries", "0", "Active consignments", UIConstants.COLOR_WARNING);

        statsGrid.add(cardTotal);
        statsGrid.add(cardBooked);
        statsGrid.add(cardInTransit);
        statsGrid.add(cardOutForDelivery);
        statsGrid.add(cardDelivered);
        statsGrid.add(cardPending);

        // 3. Middle Section: Recent Bookings Table
        CardPanel recentCard = new CardPanel();
        recentCard.setLayout(new BorderLayout(0, 12));

        JPanel recentHeader = new JPanel(new BorderLayout());
        recentHeader.setOpaque(false);
        JLabel lblRecentTitle = new JLabel("Recent Parcel Bookings");
        lblRecentTitle.setFont(UIConstants.FONT_TITLE);
        lblRecentTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        JLabel lblRecentHint = new JLabel("(Double-click row to view Airway Bill)");
        lblRecentHint.setFont(UIConstants.FONT_SMALL);
        lblRecentHint.setForeground(UIConstants.COLOR_TEXT_MUTED);

        recentHeader.add(lblRecentTitle, BorderLayout.WEST);
        recentHeader.add(lblRecentHint, BorderLayout.EAST);

        String[] recentCols = {"Consignment No", "Sender", "Receiver", "Route (From → To)", "Weight (kg)", "Charge", "Status", "Booked At"};
        modelRecentParcels = new DefaultTableModel(recentCols, 0);
        tblRecentParcels = new ModernTable(modelRecentParcels);
        tblRecentParcels.getColumnModel().getColumn(6).setCellRenderer(tblRecentParcels.getDefaultRenderer(ParcelStatus.class));

        tblRecentParcels.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblRecentParcels.getSelectedRow() >= 0) {
                    int row = tblRecentParcels.getSelectedRow();
                    if (currentRecentList != null && row < currentRecentList.size()) {
                        Parcel p = currentRecentList.get(row);
                        ReceiptViewDialog dlg = new ReceiptViewDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(DashboardPanel.this), p);
                        dlg.setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollRecent = new JScrollPane(tblRecentParcels);
        scrollRecent.setPreferredSize(new Dimension(0, 220));
        scrollRecent.setBorder(BorderFactory.createEmptyBorder());

        recentCard.add(recentHeader, BorderLayout.NORTH);
        recentCard.add(scrollRecent, BorderLayout.CENTER);

        // 4. Bottom Section: Latest Delivery Activities Feed
        CardPanel activityCard = new CardPanel();
        activityCard.setLayout(new BorderLayout(0, 12));

        JLabel lblActivityTitle = new JLabel("Latest Delivery & Transit Activities");
        lblActivityTitle.setFont(UIConstants.FONT_TITLE);
        lblActivityTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        String[] actCols = {"Consignment No", "Status", "Current Location", "Remarks / Activity", "Timestamp"};
        modelActivities = new DefaultTableModel(actCols, 0);
        tblActivities = new ModernTable(modelActivities);
        tblActivities.getColumnModel().getColumn(1).setCellRenderer(tblActivities.getDefaultRenderer(ParcelStatus.class));

        JScrollPane scrollActivities = new JScrollPane(tblActivities);
        scrollActivities.setPreferredSize(new Dimension(0, 200));
        scrollActivities.setBorder(BorderFactory.createEmptyBorder());

        activityCard.add(lblActivityTitle, BorderLayout.NORTH);
        activityCard.add(scrollActivities, BorderLayout.CENTER);

        // Assemble Content
        scrollContent.add(topBar);
        scrollContent.add(Box.createVerticalStrut(16));
        scrollContent.add(statsGrid);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(recentCard);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(activityCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    public void loadDashboardData() {
        try {
            // 1. Statistics
            DashboardStats stats = parcelService.getDashboardStatistics();
            cardTotal.setValue(String.valueOf(stats.getTotalParcels()));
            cardBooked.setValue(String.valueOf(stats.getBookedParcels()));
            cardInTransit.setValue(String.valueOf(stats.getInTransitParcels()));
            cardOutForDelivery.setValue(String.valueOf(stats.getOutForDeliveryParcels()));
            cardDelivered.setValue(String.valueOf(stats.getDeliveredParcels()));
            cardPending.setValue(String.valueOf(stats.getPendingDeliveries()));

            // 2. Recent Parcels Table
            currentRecentList = parcelService.getRecentParcels(8);
            modelRecentParcels.setRowCount(0);
            for (Parcel p : currentRecentList) {
                modelRecentParcels.addRow(new Object[]{
                    p.getConsignmentNo(),
                    p.getSenderName(),
                    p.getReceiverName(),
                    p.getSourceBranchName() + " → " + p.getDestinationBranchName(),
                    String.format("%.2f", p.getWeight()),
                    p.getFormattedCharge(),
                    p.getStatus(),
                    p.getFormattedBookingDate()
                });
            }

            // 3. Recent Activities Feed
            List<TrackingHistory> activities = trackingService.getRecentActivities(8);
            modelActivities.setRowCount(0);
            for (TrackingHistory th : activities) {
                modelActivities.addRow(new Object[]{
                    th.getConsignmentNo(),
                    th.getStatus(),
                    th.getLocation(),
                    th.getRemarks(),
                    th.getFormattedDate()
                });
            }

            lblLastUpdated.setText("Last Synced: " + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load dashboard data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
