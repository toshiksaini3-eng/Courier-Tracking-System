package com.couriertracking.model;

/**
 * Enumeration representing parcel categories with rate constants.
 */
public enum ParcelType {
    DOCUMENT("Document", 50.0, 20.0), // Base Rs 50 (up to 0.5kg), +Rs 20 per extra kg
    PACKAGE("Package", 100.0, 40.0);   // Base Rs 100 (up to 1.0kg), +Rs 40 per extra kg

    private final String displayName;
    private final double baseRate;
    private final double perKgRate;

    ParcelType(String displayName, double baseRate, double perKgRate) {
        this.displayName = displayName;
        this.baseRate = baseRate;
        this.perKgRate = perKgRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseRate() {
        return baseRate;
    }

    public double getPerKgRate() {
        return perKgRate;
    }

    public static ParcelType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return PACKAGE;
        }
        for (ParcelType type : ParcelType.values()) {
            if (type.name().equalsIgnoreCase(text.trim()) || 
                type.displayName.equalsIgnoreCase(text.trim())) {
                return type;
            }
        }
        return PACKAGE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
