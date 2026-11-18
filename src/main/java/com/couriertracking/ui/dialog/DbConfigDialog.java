package com.couriertracking.ui.dialog;

import com.couriertracking.util.DatabaseConnection;
import com.couriertracking.util.DatabaseInitializer;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Dialog to view and configure the database connection (MySQL 8 / Local Storage).
 */
public class DbConfigDialog extends JDialog {
    private final Runnable onReconnect;

    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDatabase;
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public DbConfigDialog(Frame owner, Runnable onReconnect) {
        super(owner, "Database Connectivity & Settings", true);
        this.onReconnect = onReconnect;

        setSize(460, 420);
        setLocationRelativeTo(owner);
        setResizable(false);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        DatabaseConnection db = DatabaseConnection.getInstance();

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Current status indicator banner
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(new Color(239, 246, 255));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        lblStatus = new JLabel("● Engine: " + db.getActiveDatabaseType() + " (" + db.getActiveConnectionInfo() + ")");
        lblStatus.setFont(UIConstants.FONT_BODY_BOLD);
        lblStatus.setForeground(UIConstants.COLOR_PRIMARY_DARK);
        banner.add(lblStatus, BorderLayout.CENTER);

        gbc.gridwidth = 2;
        form.add(banner, gbc);
        gbc.gridwidth = 1;
        gbc.gridy++;

        txtHost = new JTextField(db.getDbHost());
        txtPort = new JTextField(String.valueOf(db.getDbPort()));
        txtDatabase = new JTextField(db.getDbName());
        txtUser = new JTextField(db.getDbUser());
        txtPassword = new JPasswordField();

        addFormField(form, gbc, "MySQL Host:", txtHost);
        addFormField(form, gbc, "Port:", txtPort);
        addFormField(form, gbc, "Database Name:", txtDatabase);
        addFormField(form, gbc, "User:", txtUser);
        addFormField(form, gbc, "Password:", txtPassword);

        // Button Bar
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonBar.setBackground(new Color(248, 250, 252));
        buttonBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.COLOR_CARD_BORDER));

        JButton btnTest = new JButton("Connect to MySQL");
        UIConstants.stylePrimaryButton(btnTest);
        btnTest.addActionListener(e -> testAndApply());

        JButton btnClose = new JButton("Close");
        UIConstants.styleSecondaryButton(btnClose);
        btnClose.addActionListener(e -> dispose());

        buttonBar.add(btnClose);
        buttonBar.add(btnTest);

        add(form, BorderLayout.CENTER);
        add(buttonBar, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY_BOLD);
        lbl.setForeground(UIConstants.COLOR_TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        field.setFont(UIConstants.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void testAndApply() {
        try {
            String host = txtHost.getText().trim();
            int port = Integer.parseInt(txtPort.getText().trim());
            String dbName = txtDatabase.getText().trim();
            String user = txtUser.getText().trim();
            String pass = new String(txtPassword.getPassword());

            DatabaseConnection.getInstance().updateCredentials(host, port, dbName, user, pass);
            DatabaseInitializer.initialize();

            lblStatus.setText("● Engine: " + DatabaseConnection.getInstance().getActiveDatabaseType() + 
                    " (" + DatabaseConnection.getInstance().getActiveConnectionInfo() + ")");

            JOptionPane.showMessageDialog(this, "Connected successfully to " + DatabaseConnection.getInstance().getActiveDatabaseType() + "!",
                    "Connection Established", JOptionPane.INFORMATION_MESSAGE);

            dispose();
            if (onReconnect != null) {
                onReconnect.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
