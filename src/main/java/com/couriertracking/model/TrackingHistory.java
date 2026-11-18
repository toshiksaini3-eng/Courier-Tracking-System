package com.couriertracking.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Model representing an audit event in a parcel's delivery lifecycle.
 */
public class TrackingHistory {
    private int id;
    private int parcelId;
    private String consignmentNo;
    private ParcelStatus status;
    private String location;
    private String remarks;
    private Timestamp updatedAt;

    public TrackingHistory() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    public TrackingHistory(int id, int parcelId, String consignmentNo, ParcelStatus status, 
                           String location, String remarks, Timestamp updatedAt) {
        this.id = id;
        this.parcelId = parcelId;
        this.consignmentNo = consignmentNo;
        this.status = status;
        this.location = location;
        this.remarks = remarks;
        this.updatedAt = updatedAt != null ? updatedAt : new Timestamp(System.currentTimeMillis());
    }

    public TrackingHistory(int parcelId, String consignmentNo, ParcelStatus status, 
                           String location, String remarks) {
        this(0, parcelId, consignmentNo, status, location, remarks, new Timestamp(System.currentTimeMillis()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParcelId() {
        return parcelId;
    }

    public void setParcelId(int parcelId) {
        this.parcelId = parcelId;
    }

    public String getConsignmentNo() {
        return consignmentNo;
    }

    public void setConsignmentNo(String consignmentNo) {
        this.consignmentNo = consignmentNo;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedDate() {
        if (updatedAt == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(updatedAt);
    }
}
