package com.couriertracking.service;

import com.couriertracking.dao.TrackingDao;
import com.couriertracking.dao.TrackingDaoImpl;
import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.ParcelStatus;
import com.couriertracking.model.TrackingHistory;
import java.sql.Timestamp;
import java.util.List;

/**
 * Service managing lifecycle tracking logs.
 */
public class TrackingService {
    private final TrackingDao trackingDao;

    public TrackingService() {
        this.trackingDao = new TrackingDaoImpl();
    }

    public TrackingService(TrackingDao trackingDao) {
        this.trackingDao = trackingDao;
    }

    public void logTrackingEvent(int parcelId, String consignmentNo, ParcelStatus status, 
                                 String location, String remarks) throws DatabaseException {
        TrackingHistory event = new TrackingHistory();
        event.setParcelId(parcelId);
        event.setConsignmentNo(consignmentNo);
        event.setStatus(status);
        event.setLocation(location != null && !location.trim().isEmpty() ? location.trim() : "Transit Point");
        event.setRemarks(remarks != null && !remarks.trim().isEmpty() ? remarks.trim() : status.getDisplayName());
        event.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        trackingDao.addTrackingEvent(event);
    }

    public List<TrackingHistory> getTrackingTimeline(String consignmentNo) throws DatabaseException {
        return trackingDao.getHistoryByConsignmentNo(consignmentNo);
    }

    public List<TrackingHistory> getTrackingTimeline(int parcelId) throws DatabaseException {
        return trackingDao.getHistoryByParcelId(parcelId);
    }

    public List<TrackingHistory> getRecentActivities(int limit) throws DatabaseException {
        return trackingDao.getRecentActivities(limit);
    }
}
