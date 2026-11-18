package com.couriertracking;

import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.ParcelSimulationService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Headless Automated Verification for Multithreaded Transit Simulation.
 */
public class SimulationTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting Multithreading Simulation Verification...");

        ParcelService parcelService = new ParcelService();
        ParcelSimulationService simService = new ParcelSimulationService(parcelService);

        Parcel parcel = parcelService.getAllParcels().get(0);
        System.out.println("Selected parcel for simulation: " + parcel.getConsignmentNo() + " (Current Status: " + parcel.getStatus() + ")");

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] progressFired = {false};

        simService.startSimulation(parcel, 1, new ParcelSimulationService.SimulationListener() {
            @Override
            public void onSimulationStarted(String consignmentNo) {
                System.out.println("[Callback] Simulation started on worker thread for: " + consignmentNo);
            }

            @Override
            public void onStepProgress(ParcelStatus status, int stepIndex, int totalSteps, String location, String remarks) {
                System.out.println(String.format("[Callback] Progress: Step %d/%d -> %s at %s (%s)",
                        stepIndex, totalSteps, status.name(), location, remarks));
                progressFired[0] = true;
                simService.stopSimulation();
                latch.countDown();
            }

            @Override
            public void onSimulationCompleted(String consignmentNo) {
                System.out.println("[Callback] Simulation completed for: " + consignmentNo);
                latch.countDown();
            }

            @Override
            public void onSimulationStopped(String message) {
                System.out.println("[Callback] Simulation stopped cleanly: " + message);
                latch.countDown();
            }

            @Override
            public void onSimulationError(String error) {
                System.err.println("[Callback] Simulation error: " + error);
                latch.countDown();
            }
        });

        boolean finished = latch.await(10, TimeUnit.SECONDS);
        if (!finished) {
            throw new RuntimeException("Simulation timed out!");
        }

        if (!progressFired[0]) {
            throw new RuntimeException("Progress callback did not fire!");
        }

        System.out.println("MULTITHREADING SIMULATION TEST PASSED SUCCESSFULLY!");
        System.exit(0);
    }
}
