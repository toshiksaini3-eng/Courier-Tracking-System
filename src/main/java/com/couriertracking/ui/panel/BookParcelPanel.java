package com.couriertracking.ui.panel;

import com.couriertracking.model.Branch;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelType;
import com.couriertracking.service.BranchService;
import com.couriertracking.service.DeliveryChargeCalculator;
import com.couriertracking.service.ParcelService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.dialog.BookingSuccessDialog;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * Professional Parcel Booking Form with real-time charge calculation and validation.
 */
public class BookParcelPanel extends JPanel {
    private final ParcelService parcelService;
    private final BranchService branchService;
    private final Consumer<String> onNavigateToTrack;
    private final Runnable onBookingSuccess;

    // Sender Fields
    private JTextField txtSenderName;
    private JTextField txtSenderPhone;
    private JTextField txtSenderAddress;

    // Receiver Fields
    private JTextField txtReceiverName;
    private JTextField txtReceiverPhone;
    private JTextField txtReceiverAddress;

    // Shipment Fields
    private JComboBox<BranchItem> cmbSourceBranch;
    private JComboBox<BranchItem> cmbDestBranch;
    private JRadioButton rbPackage;
    private JRadioButton rbDocument;
    private JTextField txtWeight;
    private JTextField txtDescription;
    private JLabel lblBookingDate;
    private JLabel lblCalculatedCharge;

    private static class BranchItem {
        final Branch branch;

        BranchItem(Branch branch) {
            this.branch = branch;
        }

        @Override
        public String toString() {
            return String.format("%s - %s (%s)", branch.getBranchCode(), branch.getBranchName(), branch.getCity());
        }
    }

    public BookParcelPanel(ParcelService parcelService, BranchService branchService,
                           Consumer<String> onNavigateToTrack, Runnable onBookingSuccess) {
        this.parcelService = parcelService;
        this.branchService = branchService;
        this.onNavigateToTrack = onNavigateToTrack;
        this.onBookingSuccess = onBookingSuccess;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadBranches();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Top Two Cards: Sender & Receiver Details (Side by side)
        JPanel partyGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        partyGrid.setOpaque(false);
        partyGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        // Sender Card
        CardPanel senderCard = new CardPanel();
        senderCard.setLayout(new BorderLayout(0, 14));
        JLabel lblSenderHeader = new JLabel("📤 Sender (Origin) Information");
        lblSenderHeader.setFont(UIConstants.FONT_TITLE);
        lblSenderHeader.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JPanel senderForm = new JPanel(new GridBagLayout());
        senderForm.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        txtSenderName = createTextField("Full name of sender");
        txtSenderPhone = createTextField("10-digit mobile number");
        txtSenderAddress = createTextField("Complete residential / office address");

        addFormRow(senderForm, gbc, 0, "Sender Name *", txtSenderName);
        addFormRow(senderForm, gbc, 1, "Sender Phone *", txtSenderPhone);
        addFormRow(senderForm, gbc, 2, "Sender Address *", txtSenderAddress);

        senderCard.add(lblSenderHeader, BorderLayout.NORTH);
        senderCard.add(senderForm, BorderLayout.CENTER);

        // Receiver Card
        CardPanel receiverCard = new CardPanel();
        receiverCard.setLayout(new BorderLayout(0, 14));
        JLabel lblReceiverHeader = new JLabel("📥 Receiver (Destination) Information");
        lblReceiverHeader.setFont(UIConstants.FONT_TITLE);
        lblReceiverHeader.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JPanel receiverForm = new JPanel(new GridBagLayout());
        receiverForm.setOpaque(false);

        txtReceiverName = createTextField("Full name of recipient");
        txtReceiverPhone = createTextField("10-digit recipient phone");
        txtReceiverAddress = createTextField("Delivery address with city/pincode");

        addFormRow(receiverForm, gbc, 0, "Receiver Name *", txtReceiverName);
        addFormRow(receiverForm, gbc, 1, "Receiver Phone *", txtReceiverPhone);
        addFormRow(receiverForm, gbc, 2, "Receiver Address *", txtReceiverAddress);

        receiverCard.add(lblReceiverHeader, BorderLayout.NORTH);
        receiverCard.add(receiverForm, BorderLayout.CENTER);

        partyGrid.add(senderCard);
        partyGrid.add(receiverCard);

        // 2. Shipment Details Card
        CardPanel shipmentCard = new CardPanel();
        shipmentCard.setLayout(new BorderLayout(0, 16));

        JLabel lblShipmentHeader = new JLabel("📦 Shipment & Routing Specifications");
        lblShipmentHeader.setFont(UIConstants.FONT_TITLE);
        lblShipmentHeader.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JPanel shipmentForm = new JPanel(new GridBagLayout());
        shipmentForm.setOpaque(false);

        cmbSourceBranch = new JComboBox<>();
        cmbDestBranch = new JComboBox<>();
        styleComboBox(cmbSourceBranch);
        styleComboBox(cmbDestBranch);

        // Parcel Type Radio Buttons
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        typePanel.setOpaque(false);
        rbPackage = new JRadioButton("Package (Commercial / Gifts)", true);
        rbDocument = new JRadioButton("Document (Letters / Official Paperwork)");
        rbPackage.setOpaque(false);
        rbDocument.setOpaque(false);
        rbPackage.setFont(UIConstants.FONT_BODY);
        rbDocument.setFont(UIConstants.FONT_BODY);

        ButtonGroup bgType = new ButtonGroup();
        bgType.add(rbPackage);
        bgType.add(rbDocument);

        rbPackage.addActionListener(e -> updateChargePreview());
        rbDocument.addActionListener(e -> updateChargePreview());

        typePanel.add(rbPackage);
        typePanel.add(rbDocument);

        txtWeight = createTextField("e.g. 1.5");
        txtWeight.setText("1.0");
        txtWeight.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                updateChargePreview();
            }
        });

        txtDescription = createTextField("Contents description (e.g., Electronics, Clothes)");

        lblBookingDate = new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + " (Auto)");
        lblBookingDate.setFont(UIConstants.FONT_BODY_BOLD);
        lblBookingDate.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        addFormRow(shipmentForm, gbc, 0, "Source Branch Hub *", cmbSourceBranch);
        addFormRow(shipmentForm, gbc, 1, "Destination Branch *", cmbDestBranch);
        addFormRow(shipmentForm, gbc, 2, "Parcel Type *", typePanel);
        addFormRow(shipmentForm, gbc, 3, "Parcel Weight (kg) *", txtWeight);
        addFormRow(shipmentForm, gbc, 4, "Item Description", txtDescription);
        addFormRow(shipmentForm, gbc, 5, "Booking Date / Time", lblBookingDate);

        shipmentCard.add(lblShipmentHeader, BorderLayout.NORTH);
        shipmentCard.add(shipmentForm, BorderLayout.CENTER);

        // 3. Billing & Pricing Summary Card
        CardPanel chargeCard = new CardPanel();
        chargeCard.setLayout(new BorderLayout(16, 0));
        chargeCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));

        JPanel chargeLeft = new JPanel();
        chargeLeft.setLayout(new BoxLayout(chargeLeft, BoxLayout.Y_AXIS));
        chargeLeft.setOpaque(false);

        JLabel lblChargeTitle = new JLabel("TOTAL ESTIMATED DELIVERY CHARGE");
        lblChargeTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblChargeTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        lblCalculatedCharge = new JLabel("₹ 100.00");
        lblCalculatedCharge.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblCalculatedCharge.setForeground(UIConstants.COLOR_PRIMARY);

        chargeLeft.add(lblChargeTitle);
        chargeLeft.add(Box.createVerticalStrut(2));
        chargeLeft.add(lblCalculatedCharge);

        JPanel chargeButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        chargeButtons.setOpaque(false);

        JButton btnClear = new JButton("Clear Form");
        UIConstants.styleSecondaryButton(btnClear);
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = new JButton("Confirm & Book Consignment ➔");
        UIConstants.stylePrimaryButton(btnSubmit);
        btnSubmit.setPreferredSize(new Dimension(240, 42));
        btnSubmit.addActionListener(e -> submitBooking());

        chargeButtons.add(btnClear);
        chargeButtons.add(btnSubmit);

        chargeCard.add(chargeLeft, BorderLayout.WEST);
        chargeCard.add(chargeButtons, BorderLayout.EAST);

        // Assemble Content
        scrollContent.add(partyGrid);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(shipmentCard);
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(chargeCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        updateChargePreview();
    }

    private JTextField createTextField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(UIConstants.FONT_BODY);
        tf.putClientProperty("JTextField.placeholderText", placeholder);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        return tf;
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(UIConstants.FONT_BODY);
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(0, 36));
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

    public void loadBranches() {
        try {
            List<Branch> branches = branchService.getActiveBranches();
            cmbSourceBranch.removeAllItems();
            cmbDestBranch.removeAllItems();

            for (Branch b : branches) {
                BranchItem item = new BranchItem(b);
                cmbSourceBranch.addItem(item);
                cmbDestBranch.addItem(item);
            }

            if (cmbDestBranch.getItemCount() > 1) {
                cmbDestBranch.setSelectedIndex(1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Could not load branches: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateChargePreview() {
        try {
            double weight = Double.parseDouble(txtWeight.getText().trim());
            ParcelType type = rbDocument.isSelected() ? ParcelType.DOCUMENT : ParcelType.PACKAGE;
            double charge = parcelService.getChargeCalculator().calculateCharge(type, weight);
            lblCalculatedCharge.setText(String.format("₹ %.2f", charge));
        } catch (NumberFormatException ignored) {
            lblCalculatedCharge.setText("₹ 0.00");
        }
    }

    private void submitBooking() {
        try {
            BranchItem sourceItem = (BranchItem) cmbSourceBranch.getSelectedItem();
            BranchItem destItem = (BranchItem) cmbDestBranch.getSelectedItem();

            if (sourceItem == null || destItem == null) {
                JOptionPane.showMessageDialog(this, "Please select both source and destination branches.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double weight;
            try {
                weight = Double.parseDouble(txtWeight.getText().trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric weight (e.g. 1.5).", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ParcelType type = rbDocument.isSelected() ? ParcelType.DOCUMENT : ParcelType.PACKAGE;

            Parcel parcel = new Parcel();
            parcel.setSenderName(txtSenderName.getText().trim());
            parcel.setSenderPhone(txtSenderPhone.getText().trim());
            parcel.setSenderAddress(txtSenderAddress.getText().trim());
            parcel.setReceiverName(txtReceiverName.getText().trim());
            parcel.setReceiverPhone(txtReceiverPhone.getText().trim());
            parcel.setReceiverAddress(txtReceiverAddress.getText().trim());
            parcel.setSourceBranchId(sourceItem.branch.getId());
            parcel.setSourceBranchName(sourceItem.branch.getBranchName());
            parcel.setDestinationBranchId(destItem.branch.getId());
            parcel.setDestinationBranchName(destItem.branch.getBranchName());
            parcel.setParcelType(type);
            parcel.setWeight(weight);
            parcel.setDescription(txtDescription.getText().trim());

            // Save to MySQL
            Parcel savedParcel = parcelService.bookParcel(parcel);

            // Pop up Success Dialog
            BookingSuccessDialog dialog = new BookingSuccessDialog(
                    (Frame) javax.swing.SwingUtilities.getWindowAncestor(this),
                    savedParcel,
                    onNavigateToTrack
            );
            dialog.setVisible(true);

            clearForm();

            if (onBookingSuccess != null) {
                onBookingSuccess.run();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        txtSenderName.setText("");
        txtSenderPhone.setText("");
        txtSenderAddress.setText("");
        txtReceiverName.setText("");
        txtReceiverPhone.setText("");
        txtReceiverAddress.setText("");
        txtDescription.setText("");
        txtWeight.setText("1.0");
        rbPackage.setSelected(true);
        updateChargePreview();
        lblBookingDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + " (Auto)");
    }
}
