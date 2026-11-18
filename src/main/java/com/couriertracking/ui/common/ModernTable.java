package com.couriertracking.ui.common;

import com.couriertracking.model.ParcelStatus;
import com.couriertracking.util.UIConstants;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Modern JTable with alternating row colors, customized headers,
 * clean typography, and embedded StatusBadge renderers.
 */
public class ModernTable extends JTable {

    public ModernTable(DefaultTableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(36);
        setFont(UIConstants.FONT_BODY);
        setShowVerticalLines(false);
        setShowHorizontalLines(true);
        setGridColor(new Color(241, 245, 249));
        setSelectionBackground(new Color(239, 246, 255));
        setSelectionForeground(UIConstants.COLOR_PRIMARY_DARK);
        setFillsViewportHeight(true);

        // Header Styling
        JTableHeader header = getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(UIConstants.COLOR_TEXT_SECONDARY);
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.COLOR_CARD_BORDER));
        header.setReorderingAllowed(false);

        // Cell Alignment & Alternating rows
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(new Color(248, 250, 252));
                    }
                    c.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(239, 246, 255));
                    c.setForeground(UIConstants.COLOR_PRIMARY_DARK);
                }
                return c;
            }
        });

        // Status Badge Renderer for ParcelStatus instances or Status text
        setDefaultRenderer(ParcelStatus.class, new TableCellRenderer() {
            private final StatusBadge badge = new StatusBadge(ParcelStatus.BOOKED);

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof ParcelStatus ps) {
                    badge.setStatus(ps);
                } else if (value != null) {
                    badge.setStatus(ParcelStatus.fromString(value.toString()));
                } else {
                    badge.setStatus(null);
                }
                return badge;
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // read-only cells
    }
}
