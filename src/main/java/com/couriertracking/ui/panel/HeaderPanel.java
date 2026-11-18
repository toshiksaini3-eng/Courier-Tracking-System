package com.couriertracking.ui.panel;

import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Modern Header Navigation Bar with title, quick search, and live date/time.
 */
public class HeaderPanel extends JPanel {
    private final JLabel lblTitle;
    private final JLabel lblSubtitle;
    private final JTextField txtQuickSearch;
    private final Consumer<String> onQuickSearch;

    public HeaderPanel(Consumer<String> onQuickSearch) {
        this.onQuickSearch = onQuickSearch;

        setLayout(new BorderLayout(20, 0));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.COLOR_CARD_BORDER),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));
        setPreferredSize(new Dimension(0, 72));

        // Left Titles
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new javax.swing.BoxLayout(leftPanel, javax.swing.BoxLayout.Y_AXIS));
        leftPanel.setOpaque(false);

        lblTitle = new JLabel("Operations Dashboard");
        lblTitle.setFont(UIConstants.FONT_HEADER_LARGE);
        lblTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        lblSubtitle = new JLabel("Real-time logistics analytics and parcel management");
        lblSubtitle.setFont(UIConstants.FONT_SMALL);
        lblSubtitle.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        leftPanel.add(lblTitle);
        leftPanel.add(javax.swing.Box.createVerticalStrut(2));
        leftPanel.add(lblSubtitle);

        // Right Actions (Quick Search & Date)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightPanel.setOpaque(false);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
        JLabel lblDate = new JLabel("📅 " + sdf.format(new Date()));
        lblDate.setFont(UIConstants.FONT_BODY);
        lblDate.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        txtQuickSearch = new JTextField(14);
        txtQuickSearch.setFont(UIConstants.FONT_BODY);
        txtQuickSearch.putClientProperty("JTextField.placeholderText", "Quick Track (CRX...)");
        txtQuickSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        txtQuickSearch.addActionListener(e -> executeSearch());

        JButton btnSearch = new JButton("Track");
        UIConstants.stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> executeSearch());

        rightPanel.add(lblDate);
        rightPanel.add(javax.swing.Box.createHorizontalStrut(8));
        rightPanel.add(txtQuickSearch);
        rightPanel.add(btnSearch);

        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.EAST);
    }

    private void executeSearch() {
        String query = txtQuickSearch.getText().trim();
        if (!query.isEmpty() && onQuickSearch != null) {
            onQuickSearch.accept(query);
            txtQuickSearch.setText("");
        }
    }

    public void setHeader(String title, String subtitle) {
        lblTitle.setText(title);
        lblSubtitle.setText(subtitle);
    }
}
