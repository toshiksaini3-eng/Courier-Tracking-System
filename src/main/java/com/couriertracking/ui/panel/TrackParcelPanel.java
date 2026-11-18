package com.couriertracking.ui.panel;

import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.TrackingService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.common.StatusBadge;
import com.couriertracking.ui.dialog.ReceiptViewDialog;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

/**
 * Public & Internal Tracking Interface with visual 5-stage progress stepper
 * and chronological audit timeline.
 */
public class TrackParcelPanel extends JPanel {
    private final ParcelService parcelService;
    private final TrackingService trackingService;
    private final Consumer<String> onNavigateToUpdate;

    private JTextField txtSearch;
    private ProgressStepperPanel stepperPanel;
    private CardPanel detailsCard;
    private CardPanel timelineCard;

    private JLabel lblCno;
    private StatusBadge badgeStatus;
    private JLabel lblSender;
    private JLabel lblReceiver;
    private JLabel lblRoute;
    private JLabel lblSpecs;
    private JLabel lblBookingDate;

    private ModernTable tblTimeline;
    private DefaultTableModel modelTimeline;
    private Parcel currentParcel;

    public TrackParcelPanel(ParcelService parcelService, TrackingService trackingService,
                            Consumer<String> onNavigateToUpdate) {
        this.parcelService = parcelService;
        this.trackingService = trackingService;
        this.onNavigateToUpdate = onNavigateToUpdate;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Search Bar Card
        CardPanel searchCard = new CardPanel();
        searchCard.setLayout(new BorderLayout(16, 0));
        searchCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchLeft.setOpaque(false);

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        txtSearch = new JTextField(24);
        txtSearch.setFont(new Font("Consolas", Font.BOLD, 15));
        txtSearch.putClientProperty("JTextField.placeholderText", "Enter Consignment Number (e.g. CRX202611180001)");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSearch.addActionListener(e -> executeTrack());

        JButton btnTrack = new JButton("Track Consignment");
        UIConstants.stylePrimaryButton(btnTrack);
        btnTrack.addActionListener(e -> executeTrack());

        searchLeft.add(lblSearchIcon);
        searchLeft.add(txtSearch);
        searchLeft.add(btnTrack);

        searchCard.add(searchLeft, BorderLayout.WEST);

        // 2. Stepper Progress Card
        CardPanel stepperCard = new CardPanel();
        stepperCard.setLayout(new BorderLayout(0, 10));
        JLabel lblStepperTitle = new JLabel("Live Shipment Progress Stepper");
        lblStepperTitle.setFont(UIConstants.FONT_TITLE);
        lblStepperTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        stepperPanel = new ProgressStepperPanel();
        stepperCard.add(lblStepperTitle, BorderLayout.NORTH);
        stepperCard.add(stepperPanel, BorderLayout.CENTER);

        // 3. Details Card
        detailsCard = new CardPanel();
        detailsCard.setLayout(new BorderLayout(0, 14));

        JPanel detailsHeader = new JPanel(new BorderLayout());
        detailsHeader.setOpaque(false);

        JPanel cnoBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cnoBox.setOpaque(false);
        lblCno = new JLabel("No Consignment Selected");
        lblCno.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCno.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        badgeStatus = new StatusBadge(ParcelStatus.BOOKED);
        cnoBox.add(lblCno);
        cnoBox.add(badgeStatus);

        JPanel detailActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        detailActions.setOpaque(false);

        JButton btnAirwayBill = new JButton("Print Airway Bill");
        UIConstants.styleSecondaryButton(btnAirwayBill);
        btnAirwayBill.addActionListener(e -> {
            if (currentParcel != null) {
                ReceiptViewDialog dlg = new ReceiptViewDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(this), currentParcel);
                dlg.setVisible(true);
            }
        });

        JButton btnUpdate = new JButton("Update Status ➔");
        UIConstants.stylePrimaryButton(btnUpdate);
        btnUpdate.addActionListener(e -> {
            if (currentParcel != null && onNavigateToUpdate != null) {
                onNavigateToUpdate.accept(currentParcel.getConsignmentNo());
            }
        });

        detailActions.add(btnAirwayBill);
        detailActions.add(btnUpdate);

        detailsHeader.add(cnoBox, BorderLayout.WEST);
        detailsHeader.add(detailActions, BorderLayout.EAST);

        // Info Grid
        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 20, 12));
        infoGrid.setOpaque(false);
        infoGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.COLOR_CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 4, 4, 4)
        ));

        lblSender = createDetailLabel("Sender", "—");
        lblReceiver = createDetailLabel("Receiver", "—");
        lblRoute = createDetailLabel("Route", "—");
        lblSpecs = createDetailLabel("Specifications", "—");

        infoGrid.add(lblSender);
        infoGrid.add(lblReceiver);
        infoGrid.add(lblRoute);
        infoGrid.add(lblSpecs);

        detailsCard.add(detailsHeader, BorderLayout.NORTH);
        detailsCard.add(infoGrid, BorderLayout.CENTER);

        // 4. Chronological Timeline Card
        timelineCard = new CardPanel();
        timelineCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTimelineTitle = new JLabel("Audit Tracking History Log");
        lblTimelineTitle.setFont(UIConstants.FONT_TITLE);
        lblTimelineTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        String[] cols = {"Timestamp", "Status", "Checkpoint Location", "Remarks / Activity"};
        modelTimeline = new DefaultTableModel(cols, 0);
        tblTimeline = new ModernTable(modelTimeline);
        tblTimeline.getColumnModel().getColumn(1).setCellRenderer(tblTimeline.getDefaultRenderer(ParcelStatus.class));

        JScrollPane scrollTimeline = new JScrollPane(tblTimeline);
        scrollTimeline.setPreferredSize(new Dimension(0, 220));
        scrollTimeline.setBorder(BorderFactory.createEmptyBorder());

        timelineCard.add(lblTimelineTitle, BorderLayout.NORTH);
        timelineCard.add(scrollTimeline, BorderLayout.CENTER);

        // Assemble Content
        scrollContent.add(searchCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(stepperCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(detailsCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(timelineCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JLabel createDetailLabel(String title, String val) {
        JLabel l = new JLabel("<html><b>" + title + ":</b> " + val + "</html>");
        l.setFont(UIConstants.FONT_BODY);
        l.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        return l;
    }

    public void searchConsignment(String consignmentNo) {
        txtSearch.setText(consignmentNo);
        executeTrack();
    }

    private void executeTrack() {
        String cno = txtSearch.getText().trim();
        if (cno.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Consignment Number.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Optional<Parcel> parcelOpt = parcelService.getParcelByConsignmentNo(cno);
            if (parcelOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No parcel found matching Consignment No: " + cno, "Not Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            currentParcel = parcelOpt.get();
            lblCno.setText(currentParcel.getConsignmentNo());
            badgeStatus.setStatus(currentParcel.getStatus());

            lblSender.setText(String.format("<html><b>Sender:</b> %s (%s) — %s</html>",
                    currentParcel.getSenderName(), currentParcel.getSenderPhone(), currentParcel.getSenderAddress()));

            lblReceiver.setText(String.format("<html><b>Receiver:</b> %s (%s) — %s</html>",
                    currentParcel.getReceiverName(), currentParcel.getReceiverPhone(), currentParcel.getReceiverAddress()));

            lblRoute.setText(String.format("<html><b>Route:</b> %s → %s</html>",
                    currentParcel.getSourceBranchName(), currentParcel.getDestinationBranchName()));

            lblSpecs.setText(String.format("<html><b>Type:</b> %s | <b>Weight:</b> %.2f kg | <b>Charge:</b> %s</html>",
                    currentParcel.getParcelType().getDisplayName(), currentParcel.getWeight(), currentParcel.getFormattedCharge()));

            // Update Stepper
            stepperPanel.setCurrentStatus(currentParcel.getStatus());

            // Load Tracking Events
            List<TrackingHistory> history = trackingService.getTrackingTimeline(currentParcel.getConsignmentNo());
            modelTimeline.setRowCount(0);
            for (TrackingHistory th : history) {
                modelTimeline.addRow(new Object[]{
                    th.getFormattedDate(),
                    th.getStatus(),
                    th.getLocation(),
                    th.getRemarks()
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error tracking parcel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Custom component rendering 5-step visual delivery progress.
     */
    private static class ProgressStepperPanel extends JPanel {
        private ParcelStatus currentStatus = ParcelStatus.BOOKED;

        private final ParcelStatus[] steps = {
            ParcelStatus.BOOKED,
            ParcelStatus.PICKED_UP,
            ParcelStatus.IN_TRANSIT,
            ParcelStatus.OUT_FOR_DELIVERY,
            ParcelStatus.DELIVERED
        };

        public ProgressStepperPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(700, 80));
        }

        public void setCurrentStatus(ParcelStatus status) {
            this.currentStatus = status;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int numSteps = steps.length;
            int stepGap = (width - 120) / (numSteps - 1);
            int startX = 60;
            int centerY = 32;

            int currentOrder = currentStatus.getStepOrder();

            // Draw Background Connecting Line
            g2.setColor(new Color(226, 232, 240));
            g2.setStroke(new java.awt.BasicStroke(4));
            g2.drawLine(startX, centerY, startX + (numSteps - 1) * stepGap, centerY);

            // Draw Completed Active Connecting Line
            if (currentOrder > 1) {
                int activeIndex = Math.min(currentOrder - 1, numSteps - 1);
                g2.setColor(UIConstants.COLOR_PRIMARY);
                g2.drawLine(startX, centerY, startX + activeIndex * stepGap, centerY);
            }

            // Draw Step Nodes & Labels
            for (int i = 0; i < numSteps; i++) {
                int x = startX + i * stepGap;
                ParcelStatus s = steps[i];
                boolean isCompleted = s.getStepOrder() < currentOrder || currentStatus == ParcelStatus.DELIVERED;
                boolean isCurrent = s == currentStatus && currentStatus != ParcelStatus.DELIVERED;

                // Outer circle
                if (isCurrent) {
                    g2.setColor(new Color(191, 219, 254)); // Light ring
                    g2.fillOval(x - 18, centerY - 18, 36, 36);
                    g2.setColor(UIConstants.COLOR_PRIMARY);
                    g2.fillOval(x - 12, centerY - 12, 24, 24);
                } else if (isCompleted) {
                    g2.setColor(UIConstants.COLOR_SUCCESS);
                    g2.fillOval(x - 12, centerY - 12, 24, 24);
                } else {
                    g2.setColor(new Color(226, 232, 240));
                    g2.fillOval(x - 10, centerY - 10, 20, 20);
                }

                // Checkmark or Number inside node
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                if (isCompleted) {
                    g2.setColor(Color.WHITE);
                    g2.drawString("✓", x - 4, centerY + 4);
                } else if (isCurrent) {
                    g2.setColor(Color.WHITE);
                    g2.drawString(String.valueOf(i + 1), x - 4, centerY + 4);
                }

                // Label below node
                g2.setFont(new Font("Segoe UI", isCurrent ? Font.BOLD : Font.PLAIN, 12));
                g2.setColor(isCurrent ? UIConstants.COLOR_PRIMARY_DARK : 
                           (isCompleted ? UIConstants.COLOR_TEXT_PRIMARY : UIConstants.COLOR_TEXT_MUTED));
                String label = s.getDisplayName();
                int labelWidth = g2.getFontMetrics().stringWidth(label);
                g2.drawString(label, x - labelWidth / 2, centerY + 30);
            }

            g2.dispose();
        }
    }
}
