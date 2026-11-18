package com.couriertracking.ui.panel;

import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.ParcelType;
import com.couriertracking.service.ParcelService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.common.StatCard;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/**
 * Logistics Delivery Reports & Performance Analytics Console.
 */
public class DeliveryReportsPanel extends JPanel {
    private final ParcelService parcelService;

    private JComboBox<String> cmbDateFilter;
    private StatCard cardRevenue;
    private StatCard cardDelivered;
    private StatCard cardSuccessRate;
    private StatCard cardAvgWeight;

    private ModernTable tblReport;
    private DefaultTableModel modelReport;
    private List<Parcel> filteredList = new ArrayList<>();

    public DeliveryReportsPanel(ParcelService parcelService) {
        this.parcelService = parcelService;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadReportData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Filter & Controls Card
        CardPanel topCard = new CardPanel();
        topCard.setLayout(new BorderLayout());

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterLeft.setOpaque(false);

        JLabel lblFilter = new JLabel("Time Period:");
        lblFilter.setFont(UIConstants.FONT_BODY_BOLD);

        cmbDateFilter = new JComboBox<>(new String[]{"All Time", "Today", "Last 7 Days", "This Month"});
        cmbDateFilter.setFont(UIConstants.FONT_BODY);
        cmbDateFilter.addActionListener(e -> loadReportData());

        JButton btnRefresh = new JButton("🔄 Generate Report");
        UIConstants.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadReportData());

        filterLeft.add(lblFilter);
        filterLeft.add(cmbDateFilter);
        filterLeft.add(btnRefresh);

        JPanel filterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterRight.setOpaque(false);

        JButton btnExport = new JButton("Export Report (CSV)");
        UIConstants.styleSecondaryButton(btnExport);
        btnExport.addActionListener(e -> exportCsv());

        JButton btnPrint = new JButton("Print Report Summary");
        UIConstants.stylePrimaryButton(btnPrint);
        btnPrint.addActionListener(e -> printReport());

        filterRight.add(btnExport);
        filterRight.add(btnPrint);

        topCard.add(filterLeft, BorderLayout.WEST);
        topCard.add(filterRight, BorderLayout.EAST);

        // 2. Report KPI Cards
        JPanel kpiGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        cardRevenue = new StatCard("Total Revenue", "₹ 0.00", "Billed deliveries", UIConstants.COLOR_PRIMARY);
        cardDelivered = new StatCard("Delivered Parcels", "0", "Completed shipments", UIConstants.COLOR_SUCCESS);
        cardSuccessRate = new StatCard("Delivery Success Rate", "0%", "Delivered vs Total", UIConstants.COLOR_INFO);
        cardAvgWeight = new StatCard("Average Weight", "0.0 kg", "Weight per consignment", UIConstants.COLOR_WARNING);

        kpiGrid.add(cardRevenue);
        kpiGrid.add(cardDelivered);
        kpiGrid.add(cardSuccessRate);
        kpiGrid.add(cardAvgWeight);

        // 3. Detailed Parcels Table
        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        JLabel lblTableTitle = new JLabel("Consignment Audit Breakdown");
        lblTableTitle.setFont(UIConstants.FONT_TITLE);
        lblTableTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        String[] cols = {"Consignment No", "Sender", "Receiver", "Origin Hub", "Dest Hub", "Type", "Weight (kg)", "Charge", "Status", "Booked Date"};
        modelReport = new DefaultTableModel(cols, 0);
        tblReport = new ModernTable(modelReport);
        tblReport.getColumnModel().getColumn(8).setCellRenderer(tblReport.getDefaultRenderer(ParcelStatus.class));

        JScrollPane scrollTable = new JScrollPane(tblReport);
        scrollTable.setPreferredSize(new Dimension(0, 360));
        scrollTable.setBorder(BorderFactory.createEmptyBorder());

        tableCard.add(lblTableTitle, BorderLayout.NORTH);
        tableCard.add(scrollTable, BorderLayout.CENTER);

        // Assemble Content
        scrollContent.add(topCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(kpiGrid);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    public void loadReportData() {
        try {
            List<Parcel> all = parcelService.getAllParcels();
            String period = (String) cmbDateFilter.getSelectedItem();

            long now = System.currentTimeMillis();
            long oneDay = 24L * 60 * 60 * 1000;

            filteredList = all.stream().filter(p -> {
                if (p.getBookingDate() == null || "All Time".equals(period)) {
                    return true;
                }
                long diff = now - p.getBookingDate().getTime();
                if ("Today".equals(period)) {
                    return diff <= oneDay;
                } else if ("Last 7 Days".equals(period)) {
                    return diff <= (7L * oneDay);
                } else if ("This Month".equals(period)) {
                    return diff <= (30L * oneDay);
                }
                return true;
            }).collect(Collectors.toList());

            // Compute KPIs
            double totalRev = 0;
            int deliveredCount = 0;
            double totalWeight = 0;

            modelReport.setRowCount(0);
            for (Parcel p : filteredList) {
                totalRev += p.getDeliveryCharge();
                totalWeight += p.getWeight();
                if (p.getStatus() == ParcelStatus.DELIVERED) {
                    deliveredCount++;
                }

                modelReport.addRow(new Object[]{
                    p.getConsignmentNo(),
                    p.getSenderName(),
                    p.getReceiverName(),
                    p.getSourceBranchName(),
                    p.getDestinationBranchName(),
                    p.getParcelType().getDisplayName(),
                    String.format("%.2f", p.getWeight()),
                    p.getFormattedCharge(),
                    p.getStatus(),
                    p.getFormattedBookingDate()
                });
            }

            cardRevenue.setValue(String.format("₹ %.2f", totalRev));
            cardDelivered.setValue(String.valueOf(deliveredCount));

            double rate = filteredList.isEmpty() ? 0.0 : (deliveredCount * 100.0) / filteredList.size();
            cardSuccessRate.setValue(String.format("%.1f %%", rate));

            double avgWeight = filteredList.isEmpty() ? 0.0 : totalWeight / filteredList.size();
            cardAvgWeight.setValue(String.format("%.2f kg", avgWeight));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load report data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportCsv() {
        if (filteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to export.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Delivery_Performance_Report.csv"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write("ConsignmentNo,Sender,Receiver,SourceBranch,DestinationBranch,Type,WeightKg,Charge,Status,BookingDate\n");
                for (Parcel p : filteredList) {
                    fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"\n",
                            p.getConsignmentNo(), p.getSenderName(), p.getReceiverName(),
                            p.getSourceBranchName(), p.getDestinationBranchName(),
                            p.getParcelType().name(), p.getWeight(), p.getDeliveryCharge(),
                            p.getStatus().name(), p.getFormattedBookingDate()
                    ));
                }
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("============================================================\n");
        sb.append("                 CRX COURIER EXPRESS                      \n");
        sb.append("              EXECUTIVE DELIVERY REPORT                   \n");
        sb.append("============================================================\n");
        sb.append(" Period Filter : ").append(cmbDateFilter.getSelectedItem()).append("\n");
        sb.append(" Generated On  : ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        sb.append("------------------------------------------------------------\n");
        sb.append(" SUMMARY PERFORMANCE METRICS:\n");
        sb.append(String.format("   Total Consignments Processed : %d\n", filteredList.size()));
        sb.append(String.format("   Total Revenue Billed         : %s\n", cardRevenue.getName()));
        sb.append(String.format("   Successfully Delivered       : %d\n", filteredList.stream().filter(p -> p.getStatus() == ParcelStatus.DELIVERED).count()));
        sb.append(String.format("   Delivery Success Rate        : %s\n", cardSuccessRate.getName()));
        sb.append("============================================================\n");
        sb.append(" CONSIGNMENT AUDIT RECORDS:\n");
        for (Parcel p : filteredList) {
            sb.append(String.format(" [%s] %s -> %s | %s | %s | %s\n",
                    p.getConsignmentNo(), p.getSourceBranchName(), p.getDestinationBranchName(),
                    p.getParcelType().name(), p.getFormattedCharge(), p.getStatus().getDisplayName()));
        }
        sb.append("============================================================\n");

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(UIConstants.FONT_CODE);
        try {
            area.print();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Print error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
