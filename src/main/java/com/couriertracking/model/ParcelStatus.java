package com.couriertracking.model;

import java.awt.Color;

/**
 * Enumeration representing all valid states of a parcel.
 * Includes color palettes, step sequences, and human-friendly display names.
 */
public enum ParcelStatus {
    BOOKED("Booked", 1, new Color(59, 130, 246), new Color(239, 246, 255)),            // Blue
    PICKED_UP("Picked Up", 2, new Color(99, 102, 241), new Color(238, 242, 255)),        // Indigo
    IN_TRANSIT("In Transit", 3, new Color(217, 119, 6), new Color(254, 243, 199)),       // Amber / Orange
    ARRIVED_AT_DESTINATION("Arrived at Hub", 4, new Color(2, 132, 199), new Color(224, 242, 254)), // Sky Blue
    OUT_FOR_DELIVERY("Out for Delivery", 5, new Color(139, 92, 246), new Color(245, 243, 255)),  // Purple
    DELIVERED("Delivered", 6, new Color(16, 185, 129), new Color(236, 253, 245)),        // Emerald Green
    DELIVERY_FAILED("Delivery Failed", -1, new Color(239, 68, 68), new Color(254, 242, 242)),   // Red
    RETURNED("Returned to Sender", -2, new Color(107, 114, 128), new Color(243, 244, 246));    // Gray

    private final String displayName;
    private final int stepOrder;
    private final Color primaryColor;
    private final Color backgroundColor;

    ParcelStatus(String displayName, int stepOrder, Color primaryColor, Color backgroundColor) {
        this.displayName = displayName;
        this.stepOrder = stepOrder;
        this.primaryColor = primaryColor;
        this.backgroundColor = backgroundColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public Color getPrimaryColor() {
        return primaryColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == RETURNED;
    }

    public static ParcelStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return BOOKED;
        }
        for (ParcelStatus status : ParcelStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim()) || 
                status.displayName.equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return BOOKED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
