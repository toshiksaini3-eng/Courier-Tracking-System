package com.couriertracking.ui.dialog;

import com.couriertracking.model.Parcel;
import com.couriertracking.util.ReceiptGenerator;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Modal dialog for viewing, saving, and printing the official Airway Bill receipt.
 */
public class ReceiptViewDialog extends JDialog {
    private final Parcel parcel;
    private JTextArea txtReceipt;

    public ReceiptViewDialog(Frame owner, Parcel parcel) {
        super(owner, "Consignment Airway Bill - " + parcel.getConsignmentNo(), true);
        this.parcel = parcel;

        setSize(620, 560);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        txtReceipt = new JTextArea();
        txtReceipt.setFont(UIConstants.FONT_CODE);
        txtReceipt.setEditable(false);
        txtReceipt.setBackground(new Color(254, 254, 255));
        txtReceipt.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        txtReceipt.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        txtReceipt.setText(ReceiptGenerator.generateReceiptText(parcel));
        txtReceipt.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtReceipt);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Buttons
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonBar.setBackground(new Color(248, 250, 252));
        buttonBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.COLOR_CARD_BORDER));

        JButton btnCopy = new JButton("Copy Receipt");
        UIConstants.styleSecondaryButton(btnCopy);
        btnCopy.addActionListener(e -> {
            StringSelection sel = new StringSelection(txtReceipt.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            JOptionPane.showMessageDialog(this, "Receipt copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnSave = new JButton("Save as File (.txt)");
        UIConstants.styleSecondaryButton(btnSave);
        btnSave.addActionListener(e -> saveToFile());

        JButton btnPrint = new JButton("Print Airway Bill");
        UIConstants.stylePrimaryButton(btnPrint);
        btnPrint.addActionListener(e -> {
            try {
                boolean complete = txtReceipt.print();
                if (complete) {
                    JOptionPane.showMessageDialog(this, "Printing completed.", "Print", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error printing: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnClose = new JButton("Close");
        UIConstants.styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        buttonBar.add(btnCopy);
        buttonBar.add(btnSave);
        buttonBar.add(btnPrint);
        buttonBar.add(btnClose);

        add(scroll, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private void saveToFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(parcel.getConsignmentNo() + "_Receipt.txt"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            try (FileWriter fw = new FileWriter(chooser.getSelectedFile())) {
                fw.write(txtReceipt.getText());
                JOptionPane.showMessageDialog(this, "Receipt successfully saved to:\n" + chooser.getSelectedFile().getAbsolutePath(),
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
