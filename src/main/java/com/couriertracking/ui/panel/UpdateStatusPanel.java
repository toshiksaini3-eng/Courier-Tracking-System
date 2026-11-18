package com.couriertracking.ui.panel;

import com.couriertracking.exception.InvalidStatusTransitionException;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.TrackingService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.common.StatusBadge;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Dedicated Status Management Console enforcing strict state machine transitions.
 */
public class UpdateStatusPanel extends JPanel {
    private final ParcelService parcelService;
    private final TrackingService trackingService;
    private final Runnable onStatusUpdated;

    private JTextField txtSearchCno;
    private CardPanel currentInfoCard;
    private JLabel lblCurrentCno;
    private StatusBadge badgeCurrentStatus;
    private JLabel lblRoute;
    private JLabel lblParties;
    private JLabel lblAllowedTransitions;

    private JComboBox<ParcelStatus> cmbNewStatus;
    private JTextField txtLocation;
    private JTextField txtRemarks;

    private ModernTable tblHistory;
    private DefaultTableModel modelHistory;

    private Parcel activeParcel;

    public UpdateStatusPanel(ParcelService parcelService, TrackingService trackingService, Runnable onStatusUpdated) {
        this.parcelService = parcelService;
        this.trackingService = trackingService;
        this.onStatusUpdated = onStatusUpdated;

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

        txtSearchCno = new JTextField(24);
        txtSearchCno.setFont(new Font("Consolas", Font.BOLD, 15));
        txtSearchCno.putClientProperty("JTextField.placeholderText", "Consignment Number (e.g. CRX202611180001)");
        txtSearchCno.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        txtSearchCno.addActionListener(e -> searchAndLoad());

        JButton btnSearch = new JButton("Load Consignment");
        UIConstants.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> searchAndLoad());

        searchLeft.add(lblSearchIcon);
        searchLeft.add(txtSearchCno);
        searchLeft.add(btnSearch);

        searchCard.add(searchLeft, BorderLayout.WEST);

        // 2. Current Parcel Info Card
        currentInfoCard = new CardPanel();
        currentInfoCard.setLayout(new BorderLayout(0, 12));

        JPanel infoHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        infoHeader.setOpaque(false);
        lblCurrentCno = new JLabel("No Consignment Loaded");
        lblCurrentCno.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblCurrentCno.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        badgeCurrentStatus = new StatusBadge(ParcelStatus.BOOKED);
        infoHeader.add(lblCurrentCno);
        infoHeader.add(badgeCurrentStatus);

        JPanel infoGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        infoGrid.setOpaque(false);
        lblRoute = new JLabel("Route: —");
        lblRoute.setFont(UIConstants.FONT_BODY);
        lblParties = new JLabel("Sender / Receiver: —");
        lblParties.setFont(UIConstants.FONT_BODY);

        infoGrid.add(lblRoute);
        infoGrid.add(lblParties);

        lblAllowedTransitions = new JLabel("Valid Next States: Please load a parcel");
        lblAllowedTransitions.setFont(UIConstants.FONT_SMALL);
        lblAllowedTransitions.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        currentInfoCard.add(infoHeader, BorderLayout.NORTH);
        currentInfoCard.add(infoGrid, BorderLayout.CENTER);
        currentInfoCard.add(lblAllowedTransitions, BorderLayout.SOUTH);

        // 3. New Status Update Action Card
        CardPanel actionCard = new CardPanel();
        actionCard.setLayout(new BorderLayout(0, 16));

        JLabel lblActionHeader = new JLabel("🔄 Dispatch & Status Progression Action");
        lblActionHeader.setFont(UIConstants.FONT_TITLE);
        lblActionHeader.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JPanel actionForm = new JPanel(new GridBagLayout());
        actionForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        cmbNewStatus = new JComboBox<>();
        cmbNewStatus.setFont(UIConstants.FONT_BODY_BOLD);
        cmbNewStatus.setPreferredSize(new Dimension(0, 36));

        txtLocation = new JTextField();
        txtLocation.setFont(UIConstants.FONT_BODY);
        txtLocation.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        txtRemarks = new JTextField();
        txtRemarks.setFont(UIConstants.FONT_BODY);
        txtRemarks.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));

        addFormRow(actionForm, gbc, 0, "Select New Status *", cmbNewStatus);
        addFormRow(actionForm, gbc, 1, "Current Location / Checkpoint *", txtLocation);
        addFormRow(actionForm, gbc, 2, "Status Remarks / Details *", txtRemarks);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JButton btnSubmit = new JButton("Save Status Update to Database ➔");
        UIConstants.stylePrimaryButton(btnSubmit);
        btnSubmit.setPreferredSize(new Dimension(280, 42));
        btnSubmit.addActionListener(e -> submitStatusUpdate());
        btnPanel.add(btnSubmit);

        actionCard.add(lblActionHeader, BorderLayout.NORTH);
        actionCard.add(actionForm, BorderLayout.CENTER);
        actionCard.add(btnPanel, BorderLayout.SOUTH);

        // 4. Tracking History Audit Table
        CardPanel historyCard = new CardPanel();
        historyCard.setLayout(new BorderLayout(0, 12));

        JLabel lblHistoryTitle = new JLabel("Consignment Audit Log Timeline");
        lblHistoryTitle.setFont(UIConstants.FONT_TITLE);
        lblHistoryTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        String[] cols = {"Timestamp", "Status", "Location", "Remarks / Description"};
        modelHistory = new DefaultTableModel(cols, 0);
        tblHistory = new ModernTable(modelHistory);
        tblHistory.getColumnModel().getColumn(1).setCellRenderer(tblHistory.getDefaultRenderer(ParcelStatus.class));

        JScrollPane scroll = new JScrollPane(tblHistory);
        scroll.setPreferredSize(new Dimension(0, 200));
        scroll.setBorder(BorderFactory.createEmptyBorder());

        historyCard.add(lblHistoryTitle, BorderLayout.NORTH);
        historyCard.add(scroll, BorderLayout.CENTER);

        // Assemble Content
        scrollContent.add(searchCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(currentInfoCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(actionCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(historyCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.28;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.72;
        panel.add(comp, gbc);
    }

    public void loadConsignment(String consignmentNo) {
        txtSearchCno.setText(consignmentNo);
        searchAndLoad();
    }

    private void searchAndLoad() {
        String cno = txtSearchCno.getText().trim();
        if (cno.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Consignment Number.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Optional<Parcel> opt = parcelService.getParcelByConsignmentNo(cno);
            if (opt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Consignment '" + cno + "' not found.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            activeParcel = opt.get();
            lblCurrentCno.setText(activeParcel.getConsignmentNo());
            badgeCurrentStatus.setStatus(activeParcel.getStatus());

            lblRoute.setText(String.format("Route: %s → %s", 
                    activeParcel.getSourceBranchName(), activeParcel.getDestinationBranchName()));
            lblParties.setText(String.format("Sender: %s | Receiver: %s", 
                    activeParcel.getSenderName(), activeParcel.getReceiverName()));

            // Populate valid next statuses based on State Machine!
            Set<ParcelStatus> validNext = parcelService.getStatusValidator().getValidNextStatuses(activeParcel.getStatus());
            cmbNewStatus.removeAllItems();

            if (validNext.isEmpty()) {
                lblAllowedTransitions.setText("⚠️ Terminal Status: No further status transitions allowed for this consignment.");
                lblAllowedTransitions.setForeground(UIConstants.COLOR_DANGER);
            } else {
                StringBuilder sb = new StringBuilder("Valid Next Transitions: ");
                for (ParcelStatus s : validNext) {
                    cmbNewStatus.addItem(s);
                    sb.append(s.getDisplayName()).append(", ");
                }
                lblAllowedTransitions.setText(sb.substring(0, sb.length() - 2));
                lblAllowedTransitions.setForeground(UIConstants.COLOR_PRIMARY_DARK);
            }

            // Default location & remarks
            txtLocation.setText(activeParcel.getDestinationBranchName() != null ? activeParcel.getDestinationBranchName() : "In Transit Hub");
            txtRemarks.setText("Status updated to " + (cmbNewStatus.getSelectedItem() != null ? cmbNewStatus.getSelectedItem().toString() : ""));

            // Load tracking timeline
            loadTimeline(activeParcel.getConsignmentNo());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTimeline(String cno) {
        try {
            List<TrackingHistory> history = trackingService.getTrackingTimeline(cno);
            modelHistory.setRowCount(0);
            for (TrackingHistory th : history) {
                modelHistory.addRow(new Object[]{
                    th.getFormattedDate(),
                    th.getStatus(),
                    th.getLocation(),
                    th.getRemarks()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void submitStatusUpdate() {
        if (activeParcel == null) {
            JOptionPane.showMessageDialog(this, "Please load a consignment first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ParcelStatus newStatus = (ParcelStatus) cmbNewStatus.getSelectedItem();
        if (newStatus == null) {
            JOptionPane.showMessageDialog(this, "No valid status selected.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String location = txtLocation.getText().trim();
        String remarks = txtRemarks.getText().trim();

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify current checkpoint location.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            parcelService.updateParcelStatus(activeParcel.getId(), activeParcel.getConsignmentNo(), newStatus, location, remarks);

            JOptionPane.showMessageDialog(this, "Status updated to " + newStatus.getDisplayName() + " successfully!",
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);

            // Reload parcel
            searchAndLoad();

            if (onStatusUpdated != null) {
                onStatusUpdated.run();
            }

        } catch (InvalidStatusTransitionException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Status Transition", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
