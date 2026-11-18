package com.couriertracking.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Core domain entity representing a parcel consignment.
 * Demonstrates Encapsulation and Data Integrity.
 */
public class Parcel {
    private int id;
    private String consignmentNo;
    private String senderName;
    private String senderPhone;
    private String senderAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private int sourceBranchId;
    private String sourceBranchName;
    private int destinationBranchId;
    private String destinationBranchName;
    private ParcelType parcelType;
    private double weight; // kg
    private String description;
    private double deliveryCharge;
    private ParcelStatus status;
    private Timestamp bookingDate;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Parcel() {
        this.status = ParcelStatus.BOOKED;
        this.bookingDate = new Timestamp(System.currentTimeMillis());
        this.parcelType = ParcelType.PACKAGE;
    }

    public Parcel(String consignmentNo, String senderName, String senderPhone, String senderAddress,
                  String receiverName, String receiverPhone, String receiverAddress,
                  int sourceBranchId, int destinationBranchId, ParcelType parcelType,
                  double weight, String description, double deliveryCharge) {
        this();
        this.consignmentNo = consignmentNo;
        this.senderName = senderName;
        this.senderPhone = senderPhone;
        this.senderAddress = senderAddress;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.receiverAddress = receiverAddress;
        this.sourceBranchId = sourceBranchId;
        this.destinationBranchId = destinationBranchId;
        this.parcelType = parcelType;
        this.weight = weight;
        this.description = description;
        this.deliveryCharge = deliveryCharge;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConsignmentNo() {
        return consignmentNo;
    }

    public void setConsignmentNo(String consignmentNo) {
        this.consignmentNo = consignmentNo;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }

    public int getSourceBranchId() {
        return sourceBranchId;
    }

    public void setSourceBranchId(int sourceBranchId) {
        this.sourceBranchId = sourceBranchId;
    }

    public String getSourceBranchName() {
        return sourceBranchName;
    }

    public void setSourceBranchName(String sourceBranchName) {
        this.sourceBranchName = sourceBranchName;
    }

    public int getDestinationBranchId() {
        return destinationBranchId;
    }

    public void setDestinationBranchId(int destinationBranchId) {
        this.destinationBranchId = destinationBranchId;
    }

    public String getDestinationBranchName() {
        return destinationBranchName;
    }

    public void setDestinationBranchName(String destinationBranchName) {
        this.destinationBranchName = destinationBranchName;
    }

    public ParcelType getParcelType() {
        return parcelType;
    }

    public void setParcelType(ParcelType parcelType) {
        this.parcelType = parcelType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDeliveryCharge() {
        return deliveryCharge;
    }

    public void setDeliveryCharge(double deliveryCharge) {
        this.deliveryCharge = deliveryCharge;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public void setStatus(ParcelStatus status) {
        this.status = status;
    }

    public Timestamp getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Timestamp bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getFormattedBookingDate() {
        if (bookingDate == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(bookingDate);
    }

    public String getFormattedCharge() {
        return String.format("₹ %.2f", deliveryCharge);
    }
}
