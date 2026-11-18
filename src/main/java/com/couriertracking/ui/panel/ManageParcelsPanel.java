package com.couriertracking.ui.panel;

import com.couriertracking.model.Branch;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.service.BranchService;
import com.couriertracking.service.ParcelService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.dialog.ReceiptViewDialog;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Complete Shipment Management Console with filtering, CSV export, and receipt viewing.
 */
public class ManageParcelsPanel extends JPanel {
    private final ParcelService parcelService;
    private final BranchService branchService;
    private final Consumer<String> onNavigateToTrack;
    private final Consumer<String> onNavigateToUpdate;

    private JTextField txtSearch;
    private JComboBox<String> cmbStatusFilter;
    private JComboBox<BranchFilterItem> cmbBranchFilter;
    private JLabel lblCount;

    private ModernTable tblParcels;
    private DefaultTableModel modelParcels;
    private List<Parcel> displayedParcels = new ArrayList<>();

    private static class BranchFilterItem {
        final Integer id;
        final String label;

        BranchFilterItem(Integer id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public ManageParcelsPanel(ParcelService parcelService, BranchService branchService,
                              Consumer<String> onNavigateToTrack, Consumer<String> onNavigateToUpdate) {
        this.parcelService = parcelService;
        this.branchService = branchService;
        this.onNavigateToTrack = onNavigateToTrack;
        this.onNavigateToUpdate = onNavigateToUpdate;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadBranches();
        loadParcels();
    }

    private void initComponents() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Filter Header Card
        CardPanel filterCard = new CardPanel();
        filterCard.setLayout(new BorderLayout(16, 0));

        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterControls.setOpaque(false);

        txtSearch = new JTextField(16);
        txtSearch.setFont(UIConstants.FONT_BODY);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search CNo, Sender, Receiver...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        txtSearch.addActionListener(e -> loadParcels());

        cmbStatusFilter = new JComboBox<>();
        cmbStatusFilter.setFont(UIConstants.FONT_BODY);
        cmbStatusFilter.addItem("All Statuses");
        for (ParcelStatus s : ParcelStatus.values()) {
            cmbStatusFilter.addItem(s.name());
        }
        cmbStatusFilter.addActionListener(e -> loadParcels());

        cmbBranchFilter = new JComboBox<>();
        cmbBranchFilter.setFont(UIConstants.FONT_BODY);
        cmbBranchFilter.addItem(new BranchFilterItem(null, "All Branches"));
        cmbBranchFilter.addActionListener(e -> loadParcels());

        JButton btnSearch = new JButton("Filter");
        UIConstants.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> loadParcels());

        JButton btnReset = new JButton("Reset");
        UIConstants.styleSecondaryButton(btnReset);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatusFilter.setSelectedIndex(0);
            cmbBranchFilter.setSelectedIndex(0);
            loadParcels();
        });

        filterControls.add(new JLabel("🔍"));
        filterControls.add(txtSearch);
        filterControls.add(cmbStatusFilter);
        filterControls.add(cmbBranchFilter);
        filterControls.add(btnSearch);
        filterControls.add(btnReset);

        lblCount = new JLabel("0 Consignments");
        lblCount.setFont(UIConstants.FONT_BODY_BOLD);
        lblCount.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        filterCard.add(filterControls, BorderLayout.WEST);
        filterCard.add(lblCount, BorderLayout.EAST);

        // 2. Center Table Card
        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] cols = {"Consignment No", "Sender", "Receiver", "Route (From → To)", "Type", "Weight", "Charge", "Status", "Booked At"};
        modelParcels = new DefaultTableModel(cols, 0);
        tblParcels = new ModernTable(modelParcels);
        tblParcels.getColumnModel().getColumn(7).setCellRenderer(tblParcels.getDefaultRenderer(ParcelStatus.class));

        tblParcels.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblParcels.getSelectedRow() >= 0) {
                    viewSelectedReceipt();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblParcels);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // 3. Bottom Action Bar
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBar.setOpaque(false);

        JButton btnAirwayBill = new JButton("View Airway Bill");
        UIConstants.styleSecondaryButton(btnAirwayBill);
        btnAirwayBill.addActionListener(e -> viewSelectedReceipt());

        JButton btnTrack = new JButton("Track Live ➔");
        UIConstants.styleSecondaryButton(btnTrack);
        btnTrack.addActionListener(e -> {
            Parcel p = getSelectedParcel();
            if (p != null && onNavigateToTrack != null) {
                onNavigateToTrack.accept(p.getConsignmentNo());
            }
        });

        JButton btnUpdate = new JButton("Update Status");
        UIConstants.stylePrimaryButton(btnUpdate);
        btnUpdate.addActionListener(e -> {
            Parcel p = getSelectedParcel();
            if (p != null && onNavigateToUpdate != null) {
                onNavigateToUpdate.accept(p.getConsignmentNo());
            }
        });

        JButton btnExport = new JButton("Export to CSV");
        UIConstants.styleSecondaryButton(btnExport);
        btnExport.addActionListener(e -> exportToCsv());

        JButton btnDelete = new JButton("Delete");
        UIConstants.styleDangerButton(btnDelete);
        btnDelete.addActionListener(e -> deleteSelectedParcel());

        actionBar.add(btnAirwayBill);
        actionBar.add(btnTrack);
        actionBar.add(btnUpdate);
        actionBar.add(btnExport);
        actionBar.add(btnDelete);

        tableCard.add(scroll, BorderLayout.CENTER);
        tableCard.add(actionBar, BorderLayout.SOUTH);

        container.add(filterCard, BorderLayout.NORTH);
        container.add(tableCard, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }

    public void loadBranches() {
        try {
            List<Branch> branches = branchService.getAllBranches();
            cmbBranchFilter.removeAllItems();
            cmbBranchFilter.addItem(new BranchFilterItem(null, "All Branches"));
            for (Branch b : branches) {
                cmbBranchFilter.addItem(new BranchFilterItem(b.getId(), b.getBranchName() + " (" + b.getCity() + ")"));
            }
        } catch (Exception ignored) {}
    }

    public void loadParcels() {
        try {
            String query = txtSearch.getText().trim();
            String statusStr = (String) cmbStatusFilter.getSelectedItem();
            ParcelStatus statusFilter = ("All Statuses".equals(statusStr) || statusStr == null) ? null : ParcelStatus.fromString(statusStr);

            BranchFilterItem branchItem = (BranchFilterItem) cmbBranchFilter.getSelectedItem();
            Integer branchId = (branchItem != null && branchItem.id != null) ? branchItem.id : null;

            displayedParcels = parcelService.searchParcels(query, statusFilter, branchId);
            modelParcels.setRowCount(0);

            for (Parcel p : displayedParcels) {
                modelParcels.addRow(new Object[]{
                    p.getConsignmentNo(),
                    p.getSenderName(),
                    p.getReceiverName(),
                    p.getSourceBranchName() + " → " + p.getDestinationBranchName(),
                    p.getParcelType().getDisplayName(),
                    String.format("%.2f kg", p.getWeight()),
                    p.getFormattedCharge(),
                    p.getStatus(),
                    p.getFormattedBookingDate()
                });
            }

            lblCount.setText(displayedParcels.size() + " Consignments Found");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error fetching parcels: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Parcel getSelectedParcel() {
        int row = tblParcels.getSelectedRow();
        if (row < 0 || row >= displayedParcels.size()) {
            JOptionPane.showMessageDialog(this, "Please select a parcel consignment from the table first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return displayedParcels.get(row);
    }

    private void viewSelectedReceipt() {
        Parcel p = getSelectedParcel();
        if (p != null) {
            ReceiptViewDialog dlg = new ReceiptViewDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(this), p);
            dlg.setVisible(true);
        }
    }

    private void deleteSelectedParcel() {
        Parcel p = getSelectedParcel();
        if (p != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete consignment " + p.getConsignmentNo() + "?\nThis will remove tracking logs permanently.",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    parcelService.deleteParcel(p.getId());
                    JOptionPane.showMessageDialog(this, "Consignment deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadParcels();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting parcel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exportToCsv() {
        if (displayedParcels.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Courier_Parcels_Export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write("ConsignmentNo,SenderName,SenderPhone,SenderAddress,ReceiverName,ReceiverPhone,ReceiverAddress,SourceBranch,DestinationBranch,ParcelType,WeightKg,Charge,Status,BookingDate\n");
                for (Parcel p : displayedParcels) {
                    fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"\n",
                            p.getConsignmentNo(),
                            p.getSenderName().replace("\"", "\"\""),
                            p.getSenderPhone(),
                            p.getSenderAddress().replace("\"", "\"\""),
                            p.getReceiverName().replace("\"", "\"\""),
                            p.getReceiverPhone(),
                            p.getReceiverAddress().replace("\"", "\"\""),
                            p.getSourceBranchName(),
                            p.getDestinationBranchName(),
                            p.getParcelType().name(),
                            p.getWeight(),
                            p.getDeliveryCharge(),
                            p.getStatus().name(),
                            p.getFormattedBookingDate()
                    ));
                }
                JOptionPane.showMessageDialog(this, "Exported " + displayedParcels.size() + " records to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
