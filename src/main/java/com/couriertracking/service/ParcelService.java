package com.couriertracking.service;

import com.couriertracking.dao.ParcelDao;
import com.couriertracking.dao.ParcelDaoImpl;
import com.couriertracking.exception.DatabaseException;
import com.couriertracking.exception.InvalidStatusTransitionException;
import com.couriertracking.exception.ValidationException;
import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.util.ConsignmentGenerator;
import com.couriertracking.util.ValidationUtil;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Core Service orchestrating Parcel booking, status progression, and queries.
 */
public class ParcelService {
    private final ParcelDao parcelDao;
    private final TrackingService trackingService;
    private final DeliveryChargeCalculator chargeCalculator;
    private final StatusTransitionValidator statusValidator;

    public ParcelService() {
        this.parcelDao = new ParcelDaoImpl();
        this.trackingService = new TrackingService();
        this.chargeCalculator = new DeliveryChargeCalculator();
        this.statusValidator = new StatusTransitionValidator();
    }

    public ParcelService(ParcelDao parcelDao, TrackingService trackingService, 
                         DeliveryChargeCalculator chargeCalculator, StatusTransitionValidator statusValidator) {
        this.parcelDao = parcelDao;
        this.trackingService = trackingService;
        this.chargeCalculator = chargeCalculator;
        this.statusValidator = statusValidator;
    }

    public Parcel bookParcel(Parcel parcel) throws ValidationException, DatabaseException {
        // 1. Validation
        ValidationUtil.validateNotEmpty(parcel.getSenderName(), "Sender Name");
        ValidationUtil.validatePhone(parcel.getSenderPhone(), "Sender Phone");
        ValidationUtil.validateNotEmpty(parcel.getSenderAddress(), "Sender Address");

        ValidationUtil.validateNotEmpty(parcel.getReceiverName(), "Receiver Name");
        ValidationUtil.validatePhone(parcel.getReceiverPhone(), "Receiver Phone");
        ValidationUtil.validateNotEmpty(parcel.getReceiverAddress(), "Receiver Address");

        if (parcel.getSourceBranchId() <= 0) {
            throw new ValidationException("Source branch must be selected.");
        }
        if (parcel.getDestinationBranchId() <= 0) {
            throw new ValidationException("Destination branch must be selected.");
        }
        if (parcel.getSourceBranchId() == parcel.getDestinationBranchId()) {
            throw new ValidationException("Destination branch must be different from Source branch for inter-city dispatch.");
        }

        ValidationUtil.validateWeight(parcel.getWeight());

        // 2. Calculate Delivery Charges
        double charge = chargeCalculator.calculateCharge(parcel.getParcelType(), parcel.getWeight());
        parcel.setDeliveryCharge(charge);

        // 3. Unique Consignment Number Generation
        String consignmentNo;
        int maxAttempts = 5;
        do {
            consignmentNo = ConsignmentGenerator.generate();
            maxAttempts--;
        } while (parcelDao.getParcelByConsignmentNo(consignmentNo).isPresent() && maxAttempts > 0);

        parcel.setConsignmentNo(consignmentNo);
        parcel.setStatus(ParcelStatus.BOOKED);
        parcel.setBookingDate(new Timestamp(System.currentTimeMillis()));

        // 4. Save to Database
        boolean inserted = parcelDao.insertParcel(parcel);
        if (!inserted) {
            throw new DatabaseException("Could not persist parcel booking into database.");
        }

        // 5. Create Initial Tracking Event
        String originLocation = parcel.getSourceBranchName() != null ? parcel.getSourceBranchName() : "Origin Station";
        trackingService.logTrackingEvent(parcel.getId(), parcel.getConsignmentNo(), ParcelStatus.BOOKED, 
                originLocation, "Consignment booked successfully. Airway bill generated.");

        return parcel;
    }

    public void updateParcelStatus(int parcelId, String consignmentNo, ParcelStatus newStatus, 
                                   String location, String remarks) 
            throws DatabaseException, InvalidStatusTransitionException, ValidationException {
        
        Optional<Parcel> parcelOpt = parcelDao.getParcelById(parcelId);
        if (parcelOpt.isEmpty()) {
            throw new ValidationException("Parcel not found for ID: " + parcelId);
        }

        Parcel currentParcel = parcelOpt.get();
        ParcelStatus currentStatus = currentParcel.getStatus();

        // Validate state machine transition
        statusValidator.validateTransition(currentStatus, newStatus);

        // Update parcels table
        parcelDao.updateParcelStatus(parcelId, newStatus);

        // Record tracking history audit log
        trackingService.logTrackingEvent(parcelId, consignmentNo, newStatus, location, remarks);
    }

    public Optional<Parcel> getParcelByConsignmentNo(String consignmentNo) throws DatabaseException {
        if (consignmentNo == null || consignmentNo.trim().isEmpty()) {
            return Optional.empty();
        }
        return parcelDao.getParcelByConsignmentNo(consignmentNo.trim());
    }

    public Optional<Parcel> getParcelById(int id) throws DatabaseException {
        return parcelDao.getParcelById(id);
    }

    public List<Parcel> getAllParcels() throws DatabaseException {
        return parcelDao.getAllParcels();
    }

    public List<Parcel> searchParcels(String query, ParcelStatus statusFilter, Integer branchIdFilter) throws DatabaseException {
        return parcelDao.searchParcels(query, statusFilter, branchIdFilter);
    }

    public List<Parcel> getRecentParcels(int limit) throws DatabaseException {
        return parcelDao.getRecentParcels(limit);
    }

    public DashboardStats getDashboardStatistics() throws DatabaseException {
        return parcelDao.getDashboardStatistics();
    }

    public boolean deleteParcel(int parcelId) throws DatabaseException {
        return parcelDao.deleteParcel(parcelId);
    }

    public StatusTransitionValidator getStatusValidator() {
        return statusValidator;
    }

    public DeliveryChargeCalculator getChargeCalculator() {
        return chargeCalculator;
    }
}
