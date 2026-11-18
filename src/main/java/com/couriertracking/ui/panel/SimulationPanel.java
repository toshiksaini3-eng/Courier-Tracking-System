package com.couriertracking.ui.panel;

import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.ParcelType;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.ParcelSimulationService;
import com.couriertracking.ui.common.CardPanel;
import com.couriertracking.ui.common.StatusBadge;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;

/**
 * Multithreaded Background Transit Simulator.
 * Demonstrates Java Multithreading, Thread Safety, ExecutorService, and Swing EDT dispatch.
 */
public class SimulationPanel extends JPanel implements ParcelSimulationService.SimulationListener {
    private final ParcelService parcelService;
    private final ParcelSimulationService simulationService;
    private final Runnable onSimulationStatusUpdated;

    private JComboBox<ParcelItem> cmbParcels;
    private JButton btnStart;
    private JButton btnPause;
    private JButton btnResume;
    private JButton btnStop;
    private JSlider sldSpeed;
    private JProgressBar progressBar;
    private StatusBadge badgeCurrentStatus;
    private JLabel lblCurrentStage;
    private JTextArea txtConsoleLog;

    private static class ParcelItem {
        final Parcel parcel;

        ParcelItem(Parcel parcel) {
            this.parcel = parcel;
        }

        @Override
        public String toString() {
            return String.format("%s - %s -> %s [%s]",
                    parcel.getConsignmentNo(), parcel.getSenderName(),
                    parcel.getReceiverName(), parcel.getStatus().getDisplayName());
        }
    }

    public SimulationPanel(ParcelService parcelService, Runnable onSimulationStatusUpdated) {
        this.parcelService = parcelService;
        this.simulationService = new ParcelSimulationService(parcelService);
        this.onSimulationStatusUpdated = onSimulationStatusUpdated;

        setLayout(new BorderLayout());
        setBackground(UIConstants.COLOR_BACKGROUND);

        initComponents();
        loadParcels();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(UIConstants.COLOR_BACKGROUND);
        scrollContent.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // 1. Header Banner Card (Explaining Multithreading)
        CardPanel headerCard = new CardPanel();
        headerCard.setLayout(new BorderLayout(16, 0));
        headerCard.setBackground(new Color(239, 246, 255));
        headerCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.COLOR_PRIMARY, 1),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));

        JPanel bannerText = new JPanel();
        bannerText.setLayout(new BoxLayout(bannerText, BoxLayout.Y_AXIS));
        bannerText.setOpaque(false);

        JLabel bannerTitle = new JLabel("⚡ Java Multithreading: Automated Parcel Transit Simulation Engine");
        bannerTitle.setFont(UIConstants.FONT_TITLE);
        bannerTitle.setForeground(UIConstants.COLOR_PRIMARY_DARK);

        JLabel bannerDesc = new JLabel("Demonstrates background worker threads, ScheduledExecutorService, and thread-safe Event Dispatch Thread (SwingUtilities.invokeLater) synchronization.");
        bannerDesc.setFont(UIConstants.FONT_SMALL);
        bannerDesc.setForeground(UIConstants.COLOR_TEXT_SECONDARY);

        bannerText.add(bannerTitle);
        bannerText.add(Box.createVerticalStrut(3));
        bannerText.add(bannerDesc);

        headerCard.add(bannerText, BorderLayout.CENTER);

        // 2. Control Panel Card
        CardPanel controlCard = new CardPanel();
        controlCard.setLayout(new BorderLayout(0, 16));

        JPanel selectRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        selectRow.setOpaque(false);

        JLabel lblSelect = new JLabel("Select Parcel:");
        lblSelect.setFont(UIConstants.FONT_BODY_BOLD);

        cmbParcels = new JComboBox<>();
        cmbParcels.setFont(UIConstants.FONT_BODY);
        cmbParcels.setPreferredSize(new Dimension(380, 36));

        JButton btnQuickDemo = new JButton("+ Quick Book Demo Parcel");
        UIConstants.styleSecondaryButton(btnQuickDemo);
        btnQuickDemo.addActionListener(e -> quickBookDemoParcel());

        selectRow.add(lblSelect);
        selectRow.add(cmbParcels);
        selectRow.add(btnQuickDemo);

        // Transit Speed & Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setOpaque(false);

        JLabel lblSpeed = new JLabel("Hop Delay (seconds):");
        lblSpeed.setFont(UIConstants.FONT_BODY_BOLD);

        sldSpeed = new JSlider(1, 5, 2);
        sldSpeed.setMajorTickSpacing(1);
        sldSpeed.setPaintTicks(true);
        sldSpeed.setPaintLabels(true);
        sldSpeed.setOpaque(false);
        sldSpeed.setPreferredSize(new Dimension(160, 45));

        btnStart = new JButton("Start Multithreaded Simulation ⚡");
        UIConstants.stylePrimaryButton(btnStart);
        btnStart.addActionListener(e -> startSimulation());

        btnPause = new JButton("Pause ⏸");
        UIConstants.styleSecondaryButton(btnPause);
        btnPause.setEnabled(false);
        btnPause.addActionListener(e -> pauseSimulation());

        btnResume = new JButton("Resume ▶");
        UIConstants.styleSecondaryButton(btnResume);
        btnResume.setEnabled(false);
        btnResume.addActionListener(e -> resumeSimulation());

        btnStop = new JButton("Stop ⏹");
        UIConstants.styleDangerButton(btnStop);
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> stopSimulation());

        actionRow.add(lblSpeed);
        actionRow.add(sldSpeed);
        actionRow.add(Box.createHorizontalStrut(10));
        actionRow.add(btnStart);
        actionRow.add(btnPause);
        actionRow.add(btnResume);
        actionRow.add(btnStop);

        controlCard.add(selectRow, BorderLayout.NORTH);
        controlCard.add(actionRow, BorderLayout.CENTER);

        // 3. Live Animated Progress & Stage Card
        CardPanel progressCard = new CardPanel();
        progressCard.setLayout(new BorderLayout(0, 14));

        JPanel stageHeader = new JPanel(new BorderLayout());
        stageHeader.setOpaque(false);

        JPanel stageLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        stageLeft.setOpaque(false);
        lblCurrentStage = new JLabel("Current State: Ready to launch simulation");
        lblCurrentStage.setFont(UIConstants.FONT_TITLE);
        badgeCurrentStatus = new StatusBadge(ParcelStatus.BOOKED);
        stageLeft.add(lblCurrentStage);
        stageLeft.add(badgeCurrentStatus);

        stageHeader.add(stageLeft, BorderLayout.WEST);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setFont(UIConstants.FONT_BODY_BOLD);
        progressBar.setPreferredSize(new Dimension(0, 28));

        progressCard.add(stageHeader, BorderLayout.NORTH);
        progressCard.add(progressBar, BorderLayout.CENTER);

        // 4. Live Thread Console Output Area
        CardPanel consoleCard = new CardPanel();
        consoleCard.setLayout(new BorderLayout(0, 10));

        JLabel lblConsoleTitle = new JLabel("Live Background Thread Execution Console Output");
        lblConsoleTitle.setFont(UIConstants.FONT_TITLE);
        lblConsoleTitle.setForeground(UIConstants.COLOR_TEXT_PRIMARY);

        txtConsoleLog = new JTextArea();
        txtConsoleLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtConsoleLog.setBackground(new Color(15, 23, 42)); // Dark Slate Terminal
        txtConsoleLog.setForeground(new Color(52, 211, 153)); // Terminal Green
        txtConsoleLog.setCaretColor(Color.WHITE);
        txtConsoleLog.setEditable(false);
        txtConsoleLog.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        log("Simulation thread system initialized. Ready for task execution.");

        JScrollPane consoleScroll = new JScrollPane(txtConsoleLog);
        consoleScroll.setPreferredSize(new Dimension(0, 240));
        consoleScroll.setBorder(BorderFactory.createEmptyBorder());

        consoleCard.add(lblConsoleTitle, BorderLayout.NORTH);
        consoleCard.add(consoleScroll, BorderLayout.CENTER);

        // Assemble Content
        scrollContent.add(headerCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(controlCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(progressCard);
        scrollContent.add(Box.createVerticalStrut(18));
        scrollContent.add(consoleCard);

        JScrollPane mainScroll = new JScrollPane(scrollContent);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(mainScroll, BorderLayout.CENTER);
    }

    public void loadParcels() {
        try {
            List<Parcel> list = parcelService.getAllParcels();
            cmbParcels.removeAllItems();
            for (Parcel p : list) {
                cmbParcels.addItem(new ParcelItem(p));
            }
        } catch (Exception ignored) {}
    }

    private void quickBookDemoParcel() {
        try {
            Parcel p = new Parcel();
            p.setSenderName("Demo Sender (Viva Hub)");
            p.setSenderPhone("9811002200");
            p.setSenderAddress("Terminal A, Academic Campus");
            p.setReceiverName("Professor Evaluator");
            p.setReceiverPhone("9822003300");
            p.setReceiverAddress("Faculty Office B-Block");
            p.setSourceBranchId(1);
            p.setSourceBranchName("Delhi Central Logistics Hub");
            p.setDestinationBranchId(2);
            p.setDestinationBranchName("Mumbai Port Gateway Hub");
            p.setParcelType(ParcelType.PACKAGE);
            p.setWeight(2.0);
            p.setDescription("Live Multithreading Demonstration Parcel");

            Parcel saved = parcelService.bookParcel(p);
            log("Created demo consignment: " + saved.getConsignmentNo());
            loadParcels();

            // Select newly created parcel
            for (int i = 0; i < cmbParcels.getItemCount(); i++) {
                if (cmbParcels.getItemAt(i).parcel.getConsignmentNo().equals(saved.getConsignmentNo())) {
                    cmbParcels.setSelectedIndex(i);
                    break;
                }
            }

            JOptionPane.showMessageDialog(this, "Created demo parcel: " + saved.getConsignmentNo() + 
                    "\nClick 'Start Multithreaded Simulation' to begin live background movement!", "Demo Parcel Ready", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startSimulation() {
        ParcelItem item = (ParcelItem) cmbParcels.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Please select or create a parcel first.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnResume.setEnabled(false);
        btnStop.setEnabled(true);
        cmbParcels.setEnabled(false);

        int delay = sldSpeed.getValue();
        log(String.format("Starting multithreaded simulation for %s [Delay: %ds per step]...", 
                item.parcel.getConsignmentNo(), delay));

        simulationService.startSimulation(item.parcel, delay, this);
    }

    private void pauseSimulation() {
        simulationService.pauseSimulation();
        btnPause.setEnabled(false);
        btnResume.setEnabled(true);
        log("Simulation thread paused by user. Waiting for resume signal...");
    }

    private void resumeSimulation() {
        simulationService.resumeSimulation();
        btnPause.setEnabled(true);
        btnResume.setEnabled(false);
        log("Simulation thread resumed. Continuing state progression...");
    }

    private void stopSimulation() {
        simulationService.stopSimulation();
        resetControls();
        log("Simulation thread stopped by user.");
    }

    private void resetControls() {
        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnResume.setEnabled(false);
        btnStop.setEnabled(false);
        cmbParcels.setEnabled(true);
    }

    private void log(String msg) {
        String timestamp = new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
        String threadName = Thread.currentThread().getName();
        txtConsoleLog.append(String.format("[%s] [%s] %s\n", timestamp, threadName, msg));
        txtConsoleLog.setCaretPosition(txtConsoleLog.getDocument().getLength());
    }

    // --- SimulationListener Callbacks (Dispatched on Swing EDT) ---

    @Override
    public void onSimulationStarted(String consignmentNo) {
        log("Worker thread successfully spawned for " + consignmentNo);
        progressBar.setValue(10);
    }

    @Override
    public void onStepProgress(ParcelStatus status, int stepIndex, int totalSteps, String location, String remarks) {
        int percent = (stepIndex * 100) / totalSteps;
        progressBar.setValue(percent);
        badgeCurrentStatus.setStatus(status);
        lblCurrentStage.setText("Current State: " + status.getDisplayName() + " at " + location);

        log(String.format(">> State Transition: Step %d/%d -> [%s] Location: %s | Remarks: %s",
                stepIndex, totalSteps, status.name(), location, remarks));

        if (onSimulationStatusUpdated != null) {
            onSimulationStatusUpdated.run();
        }
    }

    @Override
    public void onSimulationCompleted(String consignmentNo) {
        progressBar.setValue(100);
        lblCurrentStage.setText("Delivery Cycle Completed for: " + consignmentNo);
        log("🎉 Delivery simulation completed successfully! Consignment reached terminal status: DELIVERED.");
        resetControls();
        loadParcels();

        if (onSimulationStatusUpdated != null) {
            onSimulationStatusUpdated.run();
        }

        JOptionPane.showMessageDialog(this, "Consignment " + consignmentNo + " has been successfully simulated and delivered!",
                "Simulation Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void onSimulationStopped(String message) {
        log("Simulation thread interrupted: " + message);
        resetControls();
    }

    @Override
    public void onSimulationError(String error) {
        log("Simulation Error: " + error);
        resetControls();
        JOptionPane.showMessageDialog(this, "Simulation error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
