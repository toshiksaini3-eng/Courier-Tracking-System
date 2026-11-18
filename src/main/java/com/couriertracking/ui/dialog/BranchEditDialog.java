package com.couriertracking.ui.dialog;

import com.couriertracking.model.Branch;
import com.couriertracking.service.BranchService;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Modal dialog for creating or updating logistics branch locations.
 */
public class BranchEditDialog extends JDialog {
    private final BranchService branchService;
    private final Branch branchToEdit;
    private final Runnable onSuccess;

    private JTextField txtCode;
    private JTextField txtName;
    private JTextField txtCity;
    private JTextField txtState;
    private JTextField txtPhone;
    private JTextField txtAddress;
    private JCheckBox chkActive;

    public BranchEditDialog(Frame owner, BranchService branchService, Branch branchToEdit, Runnable onSuccess) {
        super(owner, branchToEdit == null ? "Add New Branch Hub" : "Edit Branch Hub", true);
        this.branchService = branchService;
        this.branchToEdit = branchToEdit;
        this.onSuccess = onSuccess;

        setSize(480, 440);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;

        txtCode = new JTextField();
        txtName = new JTextField();
        txtCity = new JTextField();
        txtState = new JTextField();
        txtPhone = new JTextField();
        txtAddress = new JTextField();
        chkActive = new JCheckBox("Active Branch (Operational)");
        chkActive.setSelected(true);
        chkActive.setOpaque(false);

        if (branchToEdit != null) {
            txtCode.setText(branchToEdit.getBranchCode());
            txtCode.setEditable(false);
            txtName.setText(branchToEdit.getBranchName());
            txtCity.setText(branchToEdit.getCity());
            txtState.setText(branchToEdit.getState());
            txtPhone.setText(branchToEdit.getContactPhone());
            txtAddress.setText(branchToEdit.getAddress());
            chkActive.setSelected(branchToEdit.isActive());
        }

        addFormField(form, gbc, "Branch Code (e.g. BR-DEL-01):", txtCode);
        addFormField(form, gbc, "Branch Name / Logistics Hub:", txtName);
        addFormField(form, gbc, "City:", txtCity);
        addFormField(form, gbc, "State:", txtState);
        addFormField(form, gbc, "Contact Phone:", txtPhone);
        addFormField(form, gbc, "Full Address:", txtAddress);

        gbc.gridx = 1;
        gbc.gridy++;
        form.add(chkActive, gbc);

        // Buttons
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonBar.setBackground(new Color(248, 250, 252));
        buttonBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.COLOR_CARD_BORDER));

        JButton btnSave = new JButton(branchToEdit == null ? "Create Branch" : "Update Branch");
        UIConstants.stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> saveBranch());

        JButton btnCancel = new JButton("Cancel");
        UIConstants.styleSecondaryButton(btnCancel);
        btnCancel.addActionListener(e -> dispose());

        buttonBar.add(btnCancel);
        buttonBar.add(btnSave);

        add(form, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setFont(UIConstants.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void saveBranch() {
        try {
            Branch b = branchToEdit != null ? branchToEdit : new Branch();
            b.setBranchCode(txtCode.getText().trim());
            b.setBranchName(txtName.getText().trim());
            b.setCity(txtCity.getText().trim());
            b.setState(txtState.getText().trim());
            b.setContactPhone(txtPhone.getText().trim());
            b.setAddress(txtAddress.getText().trim());
            b.setActive(chkActive.isSelected());

            if (branchToEdit == null) {
                branchService.createBranch(b);
                JOptionPane.showMessageDialog(this, "Branch created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                branchService.updateBranch(b);
                JOptionPane.showMessageDialog(this, "Branch updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            dispose();
            if (onSuccess != null) {
                onSuccess.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation / Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
