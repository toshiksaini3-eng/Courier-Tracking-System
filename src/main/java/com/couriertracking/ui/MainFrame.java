package com.couriertracking.ui;

import com.couriertracking.service.BranchService;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.TrackingService;
import com.couriertracking.ui.dialog.DbConfigDialog;
import com.couriertracking.ui.panel.AboutPanel;
import com.couriertracking.ui.panel.BookParcelPanel;
import com.couriertracking.ui.panel.BranchManagementPanel;
import com.couriertracking.ui.panel.DashboardPanel;
import com.couriertracking.ui.panel.DeliveryReportsPanel;
import com.couriertracking.ui.panel.HeaderPanel;
import com.couriertracking.ui.panel.ManageParcelsPanel;
import com.couriertracking.ui.panel.SidebarPanel;
import com.couriertracking.ui.panel.SidebarPanel.NavItem;
import com.couriertracking.ui.panel.SimulationPanel;
import com.couriertracking.ui.panel.TrackParcelPanel;
import com.couriertracking.ui.panel.UpdateStatusPanel;
import com.couriertracking.util.UIConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main Application Window integrating Sidebar navigation, Header, and CardLayout views.
 * Launches maximized with professional logistics SaaS layout.
 */
public class MainFrame extends JFrame {

    private final ParcelService parcelService;
    private final BranchService branchService;
    private final TrackingService trackingService;

    private SidebarPanel sidebarPanel;
    private HeaderPanel headerPanel;
    private JPanel contentContainer;
    private CardLayout cardLayout;

    // Feature Panels
    private DashboardPanel dashboardPanel;
    private BookParcelPanel bookParcelPanel;
    private TrackParcelPanel trackParcelPanel;
    private ManageParcelsPanel manageParcelsPanel;
    private UpdateStatusPanel updateStatusPanel;
    private BranchManagementPanel branchManagementPanel;
    private DeliveryReportsPanel deliveryReportsPanel;
    private SimulationPanel simulationPanel;
    private AboutPanel aboutPanel;

    public MainFrame() {
        super("Courier Tracking System - Professional Logistics Management");

        // Services Initialization
        this.parcelService = new ParcelService();
        this.branchService = new BranchService();
        this.trackingService = new TrackingService();

        initWindow();
        initComponents();
    }

    private void initWindow() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmAndExit();
            }
        });

        setMinimumSize(new Dimension(1100, 720));
        // Open in maximized state as requested
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
    }

    private void initComponents() {
        // 1. Sidebar Panel
        sidebarPanel = new SidebarPanel(this::handleNavigation, this::openDbConfig);

        // 2. Center Content Area (Header + CardLayout Views)
        JPanel centerWrapper = new JPanel(new BorderLayout());

        // Header
        headerPanel = new HeaderPanel(this::navigateToTrackConsignment);

        // CardLayout Panel
        cardLayout = new CardLayout();
        contentContainer = new JPanel(cardLayout);
        contentContainer.setBackground(UIConstants.COLOR_BACKGROUND);

        // Instantiate Feature Panels with inter-module wiring
        dashboardPanel = new DashboardPanel(parcelService, trackingService, 
                this::navigateToTrackConsignment, () -> navigateTo(NavItem.BOOK_PARCEL));

        bookParcelPanel = new BookParcelPanel(parcelService, branchService,
                this::navigateToTrackConsignment, this::refreshAllData);

        trackParcelPanel = new TrackParcelPanel(parcelService, trackingService,
                this::navigateToUpdateConsignment);

        manageParcelsPanel = new ManageParcelsPanel(parcelService, branchService,
                this::navigateToTrackConsignment, this::navigateToUpdateConsignment);

        updateStatusPanel = new UpdateStatusPanel(parcelService, trackingService, this::refreshAllData);

        branchManagementPanel = new BranchManagementPanel(branchService, this::onBranchesChanged);

        deliveryReportsPanel = new DeliveryReportsPanel(parcelService);

        simulationPanel = new SimulationPanel(parcelService, this::refreshAllData);

        aboutPanel = new AboutPanel();

        // Add to CardLayout
        contentContainer.add(dashboardPanel, NavItem.DASHBOARD.name());
        contentContainer.add(bookParcelPanel, NavItem.BOOK_PARCEL.name());
        contentContainer.add(trackParcelPanel, NavItem.TRACK_PARCEL.name());
        contentContainer.add(manageParcelsPanel, NavItem.MANAGE_PARCELS.name());
        contentContainer.add(updateStatusPanel, NavItem.UPDATE_STATUS.name());
        contentContainer.add(branchManagementPanel, NavItem.BRANCH_MANAGEMENT.name());
        contentContainer.add(deliveryReportsPanel, NavItem.DELIVERY_REPORTS.name());
        contentContainer.add(simulationPanel, NavItem.SIMULATION.name());
        contentContainer.add(aboutPanel, NavItem.ABOUT_PROJECT.name());

        centerWrapper.add(headerPanel, BorderLayout.NORTH);
        centerWrapper.add(contentContainer, BorderLayout.CENTER);

        add(sidebarPanel, BorderLayout.WEST);
        add(centerWrapper, BorderLayout.CENTER);

        // Start on Dashboard
        navigateTo(NavItem.DASHBOARD);
    }

    private void handleNavigation(NavItem item) {
        if (item == NavItem.EXIT) {
            confirmAndExit();
            return;
        }

        cardLayout.show(contentContainer, item.name());

        // Update Header Titles
        switch (item) {
            case DASHBOARD -> {
                headerPanel.setHeader("Operations Dashboard", "Real-time logistics analytics and parcel management");
                dashboardPanel.loadDashboardData();
            }
            case BOOK_PARCEL -> {
                headerPanel.setHeader("Book Consignment", "Register new domestic shipping orders with real-time tariff calculation");
                bookParcelPanel.loadBranches();
            }
            case TRACK_PARCEL -> headerPanel.setHeader("Live Shipment Tracking", "Track consignment status, milestones, and audit history");
            case MANAGE_PARCELS -> {
                headerPanel.setHeader("Consignment Directory", "Search, filter, export, and manage registered shipments");
                manageParcelsPanel.loadParcels();
            }
            case UPDATE_STATUS -> headerPanel.setHeader("Status Progression Engine", "Dispatch checkpoints and enforce logistics state machine rules");
            case BRANCH_MANAGEMENT -> {
                headerPanel.setHeader("Branch Network Management", "Configure hub locations, sorting facilities, and distribution centers");
                branchManagementPanel.loadBranches();
            }
            case DELIVERY_REPORTS -> {
                headerPanel.setHeader("Executive Delivery Reports", "Consolidated performance metrics, revenue audit, and success analytics");
                deliveryReportsPanel.loadReportData();
            }
            case SIMULATION -> {
                headerPanel.setHeader("Multithreaded Simulation", "Automated background thread parcel lifecycle demonstration");
                simulationPanel.loadParcels();
            }
            case ABOUT_PROJECT -> headerPanel.setHeader("Project Documentation", "B.Tech OOP Concepts, Design Patterns, and Architecture");
            default -> {}
        }
    }

    public void navigateTo(NavItem item) {
        sidebarPanel.setActiveItem(item);
        handleNavigation(item);
    }

    public void navigateToTrackConsignment(String consignmentNo) {
        navigateTo(NavItem.TRACK_PARCEL);
        trackParcelPanel.searchConsignment(consignmentNo);
    }

    public void navigateToUpdateConsignment(String consignmentNo) {
        navigateTo(NavItem.UPDATE_STATUS);
        updateStatusPanel.loadConsignment(consignmentNo);
    }

    private void refreshAllData() {
        dashboardPanel.loadDashboardData();
        manageParcelsPanel.loadParcels();
        deliveryReportsPanel.loadReportData();
        simulationPanel.loadParcels();
    }

    private void onBranchesChanged() {
        bookParcelPanel.loadBranches();
        manageParcelsPanel.loadBranches();
    }

    private void openDbConfig() {
        DbConfigDialog dlg = new DbConfigDialog(this, () -> {
            sidebarPanel.updateDbStatus();
            refreshAllData();
            onBranchesChanged();
        });
        dlg.setVisible(true);
    }

    private void confirmAndExit() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit Courier Tracking System?",
                "Confirm Exit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
}
