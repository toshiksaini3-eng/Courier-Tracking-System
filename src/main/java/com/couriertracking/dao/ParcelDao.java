package com.couriertracking.dao;

import com.couriertracking.exception.DatabaseException;
import com.couriertracking.model.DashboardStats;
import com.couriertracking.model.Parcel;
import com.couriertracking.model.ParcelStatus;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Parcel Consignments.
 */
public interface ParcelDao {
    boolean insertParcel(Parcel parcel) throws DatabaseException;
    Optional<Parcel> getParcelById(int id) throws DatabaseException;
    Optional<Parcel> getParcelByConsignmentNo(String consignmentNo) throws DatabaseException;
    List<Parcel> getAllParcels() throws DatabaseException;
    List<Parcel> searchParcels(String query, ParcelStatus statusFilter, Integer branchIdFilter) throws DatabaseException;
    List<Parcel> getRecentParcels(int limit) throws DatabaseException;
    boolean updateParcelStatus(int parcelId, ParcelStatus status) throws DatabaseException;
    boolean deleteParcel(int parcelId) throws DatabaseException;
    DashboardStats getDashboardStatistics() throws DatabaseException;
}
