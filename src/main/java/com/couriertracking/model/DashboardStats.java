package com.couriertracking.model;

/**
 * Aggregated metric stats for the executive Dashboard.
 */
public class DashboardStats {
    private int totalParcels;
    private int bookedParcels;
    private int inTransitParcels;
    private int outForDeliveryParcels;
    private int deliveredParcels;
    private int pendingDeliveries;
    private double totalRevenue;

    public DashboardStats() {
    }

    public int getTotalParcels() {
        return totalParcels;
    }

    public void setTotalParcels(int totalParcels) {
        this.totalParcels = totalParcels;
    }

    public int getBookedParcels() {
        return bookedParcels;
    }

    public void setBookedParcels(int bookedParcels) {
        this.bookedParcels = bookedParcels;
    }

    public int getInTransitParcels() {
        return inTransitParcels;
    }

    public void setInTransitParcels(int inTransitParcels) {
        this.inTransitParcels = inTransitParcels;
    }

    public int getOutForDeliveryParcels() {
        return outForDeliveryParcels;
    }

    public void setOutForDeliveryParcels(int outForDeliveryParcels) {
        this.outForDeliveryParcels = outForDeliveryParcels;
    }

    public int getDeliveredParcels() {
        return deliveredParcels;
    }

    public void setDeliveredParcels(int deliveredParcels) {
        this.deliveredParcels = deliveredParcels;
    }

    public int getPendingDeliveries() {
        return pendingDeliveries;
    }

    public void setPendingDeliveries(int pendingDeliveries) {
        this.pendingDeliveries = pendingDeliveries;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getDeliverySuccessRate() {
        if (totalParcels == 0) return 0.0;
        return (deliveredParcels * 100.0) / totalParcels;
    }
}
