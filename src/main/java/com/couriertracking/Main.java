package com.couriertracking;

import com.couriertracking.service.LocalhostHttpServer;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.TrackingService;
import com.couriertracking.ui.MainFrame;
import com.couriertracking.util.DatabaseInitializer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main Application Entry Point.
 * 1. Initializes Modern FlatLaf Look & Feel.
 * 2. Initializes MySQL / SQLite Database & Seeds initial demo records.
 * 3. Launches Localhost HTTP Server on http://localhost:8080 for web tracking & API.
 * 4. Launches Java Swing Desktop Application Window on Event Dispatch Thread (EDT).
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Initialize Modern FlatLaf Look and Feel
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
        } catch (Throwable t) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // 2. Initialize Database & Seed initial demo records
        try {
            DatabaseInitializer.initialize();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database: " + e.getMessage(), e);
        }

        // 3. Start Localhost HTTP Web & API Server (http://localhost:8080)
        try {
            ParcelService parcelService = new ParcelService();
            TrackingService trackingService = new TrackingService();
            LocalhostHttpServer.startServer(parcelService, trackingService);
        } catch (Throwable t) {
            LOGGER.warning("Could not start localhost web server: " + t.getMessage());
        }

        // 4. Launch Desktop GUI Window on Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to launch GUI: " + e.getMessage(), e);
            }
        });
    }
}
