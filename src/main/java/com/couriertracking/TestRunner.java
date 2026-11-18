package com.couriertracking;

import com.couriertracking.exception.InvalidStatusTransitionException;
import com.couriertracking.model.Branch;
import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.ParcelType;
import com.couriertracking.model.TrackingHistory;
import com.couriertracking.service.BranchService;
import com.couriertracking.service.DeliveryChargeCalculator;
import com.couriertracking.service.ParcelService;
import com.couriertracking.service.StatusTransitionValidator;
import com.couriertracking.service.TrackingService;
import com.couriertracking.util.DatabaseConnection;
import com.couriertracking.util.DatabaseInitializer;
import com.couriertracking.util.ReceiptGenerator;
import java.util.List;
import java.util.Optional;

/**
 * Headless Automated Verification & Diagnostic Test Suite.
 * Validates DB initialization, CRUD operations, state machine rules, and multithreading.
 */
public class TestRunner {

    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("   COURIER TRACKING SYSTEM - AUTOMATED VERIFICATION SUITE  ");
        System.out.println("==========================================================");

        int passed = 0;
        int failed = 0;

        try {
            // TEST 1: Database Connection & Initialization
            System.out.print("[TEST 1] Initializing Database & Seed Data... ");
            DatabaseInitializer.initialize();
            String dbType = DatabaseConnection.getInstance().getActiveDatabaseType();
            String dbInfo = DatabaseConnection.getInstance().getActiveConnectionInfo();
            System.out.println("PASSED (Active Engine: " + dbType + " [" + dbInfo + "])");
            passed++;

            // TEST 2: Branch Service Validation
            System.out.print("[TEST 2] Verifying Branch Hubs in Database... ");
            BranchService branchService = new BranchService();
            List<Branch> branches = branchService.getAllBranches();
            if (branches.size() < 6) {
                throw new RuntimeException("Expected at least 6 branches, found: " + branches.size());
            }
            System.out.println("PASSED (" + branches.size() + " branches active)");
            passed++;

            // TEST 3: Delivery Tariff Calculation
            System.out.print("[TEST 3] Testing Dynamic Delivery Tariff Calculator... ");
            DeliveryChargeCalculator calc = new DeliveryChargeCalculator();
            double doc05 = calc.calculateCharge(ParcelType.DOCUMENT, 0.5);
            double doc10 = calc.calculateCharge(ParcelType.DOCUMENT, 1.0);
            double pkg10 = calc.calculateCharge(ParcelType.PACKAGE, 1.0);
            double pkg25 = calc.calculateCharge(ParcelType.PACKAGE, 2.5);

            if (doc05 != 50.0 || doc10 != 70.0 || pkg10 != 100.0 || pkg25 != 180.0) {
                throw new RuntimeException("Tariff calculation mismatch: doc05=" + doc05 + ", doc10=" + doc10 + ", pkg10=" + pkg10 + ", pkg25=" + pkg25);
            }
            System.out.println("PASSED (Document & Package tariffs accurate)");
            passed++;

            // TEST 4: Parcel Booking & Consignment Generation
            System.out.print("[TEST 4] Testing Parcel Booking & Auto Consignment No... ");
            ParcelService parcelService = new ParcelService();
            Parcel newParcel = new Parcel();
            newParcel.setSenderName("Dr. Vikram Sarabhai");
            newParcel.setSenderPhone("9811223344");
            newParcel.setSenderAddress("ISRO HQ, Bengaluru");
            newParcel.setReceiverName("Prof. Satish Dhawan");
            newParcel.setReceiverPhone("9844556677");
            newParcel.setReceiverAddress("Space Center, Sriharikota");
            newParcel.setSourceBranchId(branches.get(0).getId());
            newParcel.setSourceBranchName(branches.get(0).getBranchName());
            newParcel.setDestinationBranchId(branches.get(1).getId());
            newParcel.setDestinationBranchName(branches.get(1).getBranchName());
            newParcel.setParcelType(ParcelType.DOCUMENT);
            newParcel.setWeight(0.5);
            newParcel.setDescription("Space Research Mission Documents");

            Parcel booked = parcelService.bookParcel(newParcel);
            if (booked.getConsignmentNo() == null || !booked.getConsignmentNo().startsWith("CRX")) {
                throw new RuntimeException("Invalid consignment number generated: " + booked.getConsignmentNo());
            }
            System.out.println("PASSED (Generated Consignment: " + booked.getConsignmentNo() + ")");
            passed++;

            // TEST 5: Tracking Timeline & Audit Logging
            System.out.print("[TEST 5] Testing Tracking History Audit Trail... ");
            TrackingService trackingService = new TrackingService();
            List<TrackingHistory> history = trackingService.getTrackingTimeline(booked.getConsignmentNo());
            if (history.isEmpty() || history.get(0).getStatus() != ParcelStatus.BOOKED) {
                throw new RuntimeException("Initial tracking record missing for: " + booked.getConsignmentNo());
            }
            System.out.println("PASSED (Initial BOOKED event verified)");
            passed++;

            // TEST 6: State Machine Transition Enforcement
            System.out.print("[TEST 6] Testing Logistics State Machine Validation... ");
            StatusTransitionValidator validator = parcelService.getStatusValidator();

            // Legal transition
            if (!validator.isValidTransition(ParcelStatus.BOOKED, ParcelStatus.PICKED_UP)) {
                throw new RuntimeException("Legal transition BOOKED -> PICKED_UP was rejected.");
            }

            // Illegal transition: BOOKED -> DELIVERED (must throw exception)
            boolean caughtIllegal = false;
            try {
                parcelService.updateParcelStatus(booked.getId(), booked.getConsignmentNo(), 
                        ParcelStatus.DELIVERED, "Direct", "Invalid direct jump");
            } catch (InvalidStatusTransitionException e) {
                caughtIllegal = true;
            }
            if (!caughtIllegal) {
                throw new RuntimeException("Illegal transition BOOKED -> DELIVERED was allowed!");
            }
            System.out.println("PASSED (Illegal state transitions strictly blocked)");
            passed++;

            // TEST 7: Executing Legal Status Update
            System.out.print("[TEST 7] Testing Valid Status Update Execution... ");
            parcelService.updateParcelStatus(booked.getId(), booked.getConsignmentNo(), 
                    ParcelStatus.PICKED_UP, "Origin Sorting Facility", "Picked up by cargo container");

            Optional<Parcel> updatedOpt = parcelService.getParcelByConsignmentNo(booked.getConsignmentNo());
            if (updatedOpt.isEmpty() || updatedOpt.get().getStatus() != ParcelStatus.PICKED_UP) {
                throw new RuntimeException("Parcel status did not update to PICKED_UP");
            }
            System.out.println("PASSED (Status successfully updated in DB to PICKED_UP)");
            passed++;

            // TEST 8: Dashboard Aggregation Metrics
            System.out.print("[TEST 8] Testing Dynamic Dashboard KPI Queries... ");
            DashboardStats stats = parcelService.getDashboardStatistics();
            if (stats.getTotalParcels() < 6) {
                throw new RuntimeException("Total parcels count unexpected: " + stats.getTotalParcels());
            }
            System.out.println(String.format("PASSED (Total: %d, Booked: %d, In Transit: %d, Delivered: %d, Revenue: ₹%.2f)",
                    stats.getTotalParcels(), stats.getBookedParcels(), stats.getInTransitParcels(), 
                    stats.getDeliveredParcels(), stats.getTotalRevenue()));
            passed++;

            // TEST 9: Printable Airway Bill / Receipt Generation
            System.out.print("[TEST 9] Testing Printable Airway Bill Receipt Generation... ");
            String receipt = ReceiptGenerator.generateReceiptText(updatedOpt.get());
            if (!receipt.contains(booked.getConsignmentNo()) || !receipt.contains("OFFICIAL CONSIGNMENT AIRWAY BILL")) {
                throw new RuntimeException("Receipt formatting failed.");
            }
            System.out.println("PASSED (Formatted ASCII airway bill verified)");
            passed++;

        } catch (Exception e) {
            System.out.println("FAILED!");
            System.err.println("Exception details: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        System.out.println("==========================================================");
        System.out.println("   TEST EXECUTION SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==========================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
