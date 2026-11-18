package com.couriertracking.ui.dialog;

import com.couriertracking.model.Parcel;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Confirmation dialog shown after a parcel is booked successfully.
 * Displays generated consignment number, copy shortcut, and navigation options.
 */
public class BookingSuccessDialog extends JDialog {
    private final Parcel parcel;
    private final Consumer<String> trackCallback;

    public BookingSuccessDialog(Frame owner, Parcel parcel, Consumer<String> trackCallback) {
        super(owner, "Consignment Booked Successfully", true);
        this.parcel = parcel;
        this.trackCallback = trackCallback;

        setSize(520, 380);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        JPanel main = new JPanel();
        main.setLayout(new javax.swing.BoxLayout(main, javax.swing.BoxLayout.Y_AXIS));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Success Header
        JLabel icon = new JLabel("✓", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI", Font.BOLD, 42));
        icon.setForeground(UIConstants.COLOR_SUCCESS);
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Consignment Booked Successfully!", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_HEADER_MEDIUM);
        title.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Your shipment is registered and ready for dispatch.", SwingConstants.CENTER);
        subtitle.setFont(UIConstants.FONT_BODY);
        subtitle.setForeground(UIConstants.COLOR_TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Consignment Number Display Box
        JPanel cnoBox = new JPanel(new BorderLayout(10, 5));
        cnoBox.setBackground(new Color(239, 246, 255));
        cnoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        cnoBox.setMaximumSize(new Dimension(460, 75));

        JLabel cnoLabel = new JLabel("CONSIGNMENT NUMBER", SwingConstants.CENTER);
        cnoLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        cnoLabel.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JLabel cnoValue = new JLabel(parcel.getConsignmentNo(), SwingConstants.CENTER);
        cnoValue.setFont(new Font("Consolas", Font.BOLD, 22));
        cnoValue.setForeground(UIConstants.COLOR_PRIMARY);

        cnoBox.add(cnoLabel, BorderLayout.NORTH);
        cnoBox.add(cnoValue, BorderLayout.CENTER);

        // Details summary
        JLabel details = new JLabel(String.format("Route: %s → %s | Weight: %.2f kg | Charge: %s",
                parcel.getSourceBranchName(), parcel.getDestinationBranchName(),
                parcel.getWeight(), parcel.getFormattedCharge()), SwingConstants.CENTER);
        details.setFont(UIConstants.FONT_SMALL);
        details.setForeground(UIConstants.COLOR_TEXT_MUTED);
        details.setAlignmentX(CENTER_ALIGNMENT);

        main.add(icon);
        main.add(javax.swing.Box.createVerticalStrut(8));
        main.add(title);
        main.add(javax.swing.Box.createVerticalStrut(4));
        main.add(subtitle);
        main.add(javax.swing.Box.createVerticalStrut(16));
        main.add(cnoBox);
        main.add(javax.swing.Box.createVerticalStrut(10));
        main.add(details);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        buttonPanel.setBackground(new Color(248, 250, 252));
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.COLOR_CARD_BORDER));

        JButton btnCopy = new JButton("Copy Consignment No");
        UIConstants.styleSecondaryButton(btnCopy);
        btnCopy.addActionListener(e -> {
            StringSelection selection = new StringSelection(parcel.getConsignmentNo());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            btnCopy.setText("Copied to Clipboard! ✓");
        });

        JButton btnReceipt = new JButton("View Airway Bill");
        UIConstants.styleSecondaryButton(btnReceipt);
        btnReceipt.addActionListener(e -> {
            ReceiptViewDialog receiptDialog = new ReceiptViewDialog((Frame) getOwner(), parcel);
            receiptDialog.setVisible(true);
        });

        JButton btnTrack = new JButton("Track Now →");
        UIConstants.stylePrimaryButton(btnTrack);
        btnTrack.addActionListener(e -> {
            dispose();
            if (trackCallback != null) {
                trackCallback.accept(parcel.getConsignmentNo());
            }
        });

        JButton btnClose = new JButton("Close");
        UIConstants.styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnCopy);
        buttonPanel.add(btnReceipt);
        buttonPanel.add(btnTrack);
        buttonPanel.add(btnClose);

        add(main, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
