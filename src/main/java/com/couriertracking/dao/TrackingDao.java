package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.TrackingHistory;
import java.util.List;

/**
 * DAO interface for tracking lifecycle logs.
 */
public interface TrackingDao {
    boolean addTrackingEvent(TrackingHistory event) throws DatabaseException;
    List<TrackingHistory> getHistoryByParcelId(int parcelId) throws DatabaseException;
    List<TrackingHistory> getHistoryByConsignmentNo(String consignmentNo) throws DatabaseException;
    List<TrackingHistory> getRecentActivities(int limit) throws DatabaseException;
}
