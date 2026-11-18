package com.couriertracking.service;

import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Multithreaded Background Service that simulates realistic parcel movement.
 * Demonstrates Java Multithreading, Concurrency, Thread Safety, and Swing EDT synchronization.
 */
public class ParcelSimulationService {
    private static final Logger LOGGER = Logger.getLogger(ParcelSimulationService.class.getName());

    public interface SimulationListener {
        void onSimulationStarted(String consignmentNo);
        void onStepProgress(ParcelStatus status, int stepIndex, int totalSteps, String location, String remarks);
        void onSimulationCompleted(String consignmentNo);
        void onSimulationStopped(String message);
        void onSimulationError(String error);
    }

    private final ParcelService parcelService;
    private final ExecutorService executor;
    private Future<?> currentTask;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isPaused = new AtomicBoolean(false);
    private final Object pauseLock = new Object();

    public ParcelSimulationService(ParcelService parcelService) {
        this.parcelService = parcelService;
        // Daemon background thread pool so it closes cleanly with the JVM
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "Courier-Simulation-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    public synchronized void startSimulation(Parcel parcel, int delaySeconds, SimulationListener listener) {
        if (isRunning.get()) {
            stopSimulation();
        }

        isRunning.set(true);
        isPaused.set(false);

        if (listener != null) {
            SwingUtilities.invokeLater(() -> listener.onSimulationStarted(parcel.getConsignmentNo()));
        }

        currentTask = executor.submit(() -> {
            try {
                // Progression steps
                ParcelStatus[] simulationPath = new ParcelStatus[] {
                    ParcelStatus.BOOKED,
                    ParcelStatus.PICKED_UP,
                    ParcelStatus.IN_TRANSIT,
                    ParcelStatus.ARRIVED_AT_DESTINATION,
                    ParcelStatus.OUT_FOR_DELIVERY,
                    ParcelStatus.DELIVERED
                };

                String[] locations = new String[] {
                    parcel.getSourceBranchName() != null ? parcel.getSourceBranchName() : "Origin Depot",
                    parcel.getSourceBranchName() != null ? parcel.getSourceBranchName() : "Origin Depot",
                    "Interstate Logistics Corridor (En Route)",
                    parcel.getDestinationBranchName() != null ? parcel.getDestinationBranchName() : "Destination Hub",
                    "Local Delivery Sector - Van 12",
                    parcel.getReceiverAddress()
                };

                String[] remarks = new String[] {
                    "Parcel booked and barcode label attached.",
                    "Dispatched from origin depot via express transit truck.",
                    "Package moving across transit corridor hub.",
                    "Consignment scanned and sorted at destination delivery hub.",
                    "Dispatched with delivery agent for doorstep delivery.",
                    "Successfully delivered. Proof of delivery recorded."
                };

                // Find initial starting step based on parcel's current status
                int startIndex = 0;
                for (int i = 0; i < simulationPath.length; i++) {
                    if (simulationPath[i] == parcel.getStatus()) {
                        startIndex = i + 1;
                        break;
                    }
                }
                if (startIndex >= simulationPath.length) {
                    startIndex = 0; // restart or re-run
                }

                for (int i = startIndex; i < simulationPath.length; i++) {
                    if (!isRunning.get() || Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    // Handle Pause condition
                    synchronized (pauseLock) {
                        while (isPaused.get() && isRunning.get()) {
                            pauseLock.wait();
                        }
                    }

                    ParcelStatus nextStatus = simulationPath[i];
                    String loc = locations[i];
                    String rem = remarks[i];
                    int stepNumber = i + 1;
                    int totalSteps = simulationPath.length;

                    // Update DB on worker thread
                    try {
                        parcelService.updateParcelStatus(parcel.getId(), parcel.getConsignmentNo(), nextStatus, loc, rem);
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error updating status during simulation: " + e.getMessage());
                    }

                    // Notify UI on Swing Event Dispatch Thread (EDT)
                    if (listener != null) {
                        SwingUtilities.invokeLater(() -> 
                            listener.onStepProgress(nextStatus, stepNumber, totalSteps, loc, rem)
                        );
                    }

                    // Sleep for simulated transit time
                    TimeUnit.SECONDS.sleep(delaySeconds);
                }

                if (isRunning.get() && listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onSimulationCompleted(parcel.getConsignmentNo()));
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onSimulationStopped("Simulation cancelled."));
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Simulation failure", e);
                if (listener != null) {
                    SwingUtilities.invokeLater(() -> listener.onSimulationError(e.getMessage()));
                }
            } finally {
                isRunning.set(false);
                isPaused.set(false);
            }
        });
    }

    public void pauseSimulation() {
        isPaused.set(true);
    }

    public void resumeSimulation() {
        synchronized (pauseLock) {
            isPaused.set(false);
            pauseLock.notifyAll();
        }
    }

    public synchronized void stopSimulation() {
        isRunning.set(false);
        resumeSimulation(); // unpause if paused
        if (currentTask != null) {
            currentTask.cancel(true);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isPaused() {
        return isPaused.get();
    }
}
