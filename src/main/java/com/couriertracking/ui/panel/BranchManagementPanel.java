package com.couriertracking.ui.panel;

import com.couriertracking.model.Branch;
import com.couriertracking.service.BranchService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.ModernTable;
import com.couriertracking.ui.dialog.BranchEditDialog;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

/**
 * Branch Network Management Console for managing logistics hubs and stations.
 */
public class BranchManagementPanel extends JPanel {
    private final BranchService branchService;
    private final Runnable onBranchesChanged;

    private ModernTable tblBranches;
    private DefaultTableModel modelBranches;
    private List<Branch> branchList = new ArrayList<>();
    private JLabel lblBranchStats;

    public BranchManagementPanel(BranchService branchService, Runnable onBranchesChanged) {
        this.branchService = branchService;
        this.onBranchesChanged = onBranchesChanged;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadBranches();
    }

    private void initComponents() {
        JPanel container = new JPanel(new BorderLayout(0, 16));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Top Control Bar Card
        CardPanel topCard = new CardPanel();
        topCard.setLayout(new BorderLayout());

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftActions.setOpaque(false);

        JButton btnAdd = new JButton("+ Add New Branch Hub");
        UIConstants.stylePrimaryButton(btnAdd);
        btnAdd.addActionListener(e -> openAddDialog());

        JButton btnRefresh = new JButton("🔄 Refresh");
        UIConstants.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadBranches());

        leftActions.add(btnAdd);
        leftActions.add(btnRefresh);

        lblBranchStats = new JLabel("0 Branches Registered");
        lblBranchStats.setFont(UIConstants.FONT_BODY_BOLD);
        lblBranchStats.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        topCard.add(leftActions, BorderLayout.WEST);
        topCard.add(lblBranchStats, BorderLayout.EAST);

        // Center Table Card
        CardPanel tableCard = new CardPanel();
        tableCard.setLayout(new BorderLayout(0, 12));

        String[] cols = {"Branch Code", "Logistics Hub Name", "City", "State", "Contact Phone", "Address", "Operational Status"};
        modelBranches = new DefaultTableModel(cols, 0);
        tblBranches = new ModernTable(modelBranches);

        tblBranches.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblBranches.getSelectedRow() >= 0) {
                    openEditDialog();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tblBranches);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // Bottom Action Bar
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomActions.setOpaque(false);

        JButton btnEdit = new JButton("Edit Selected");
        UIConstants.styleSecondaryButton(btnEdit);
        btnEdit.addActionListener(e -> openEditDialog());

        JButton btnToggle = new JButton("Toggle Active/Inactive");
        UIConstants.styleSecondaryButton(btnToggle);
        btnToggle.addActionListener(e -> toggleStatus());

        JButton btnDelete = new JButton("Delete Branch");
        UIConstants.styleDangerButton(btnDelete);
        btnDelete.addActionListener(e -> deleteBranch());

        bottomActions.add(btnEdit);
        bottomActions.add(btnToggle);
        bottomActions.add(btnDelete);

        tableCard.add(scroll, BorderLayout.CENTER);
        tableCard.add(bottomActions, BorderLayout.SOUTH);

        container.add(topCard, BorderLayout.NORTH);
        container.add(tableCard, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);
    }

    public void loadBranches() {
        try {
            branchList = branchService.getAllBranches();
            modelBranches.setRowCount(0);

            int activeCount = 0;
            for (Branch b : branchList) {
                if (b.isActive()) activeCount++;
                modelBranches.addRow(new Object[]{
                    b.getBranchCode(),
                    b.getBranchName(),
                    b.getCity(),
                    b.getState(),
                    b.getContactPhone(),
                    b.getAddress(),
                    b.isActive() ? "Active (Operational)" : "Inactive (Closed)"
                });
            }

            lblBranchStats.setText(String.format("Total: %d Hubs | Active: %d | Inactive: %d", 
                    branchList.size(), activeCount, branchList.size() - activeCount));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to load branches: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Branch getSelectedBranch() {
        int row = tblBranches.getSelectedRow();
        if (row < 0 || row >= branchList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a branch hub from the table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return branchList.get(row);
    }

    private void openAddDialog() {
        BranchEditDialog dlg = new BranchEditDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(this), 
                branchService, null, () -> {
            loadBranches();
            if (onBranchesChanged != null) onBranchesChanged.run();
        });
        dlg.setVisible(true);
    }

    private void openEditDialog() {
        Branch b = getSelectedBranch();
        if (b != null) {
            BranchEditDialog dlg = new BranchEditDialog((Frame) javax.swing.SwingUtilities.getWindowAncestor(this), 
                    branchService, b, () -> {
                loadBranches();
                if (onBranchesChanged != null) onBranchesChanged.run();
            });
            dlg.setVisible(true);
        }
    }

    private void toggleStatus() {
        Branch b = getSelectedBranch();
        if (b != null) {
            try {
                b.setActive(!b.isActive());
                branchService.updateBranch(b);
                loadBranches();
                if (onBranchesChanged != null) onBranchesChanged.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteBranch() {
        Branch b = getSelectedBranch();
        if (b != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete branch '" + b.getBranchName() + "' (" + b.getBranchCode() + ")?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    branchService.deleteBranch(b.getId());
                    JOptionPane.showMessageDialog(this, "Branch removed successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    loadBranches();
                    if (onBranchesChanged != null) onBranchesChanged.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Delete", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
