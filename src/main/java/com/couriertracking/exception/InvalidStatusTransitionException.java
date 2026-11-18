package com.couriertracking.exception;

import com.couriertracking.model.ParcelStatus;

/**
 * Thrown when an illegal state transition is attempted on a parcel.
 */
public class InvalidStatusTransitionException extends CourierException {
    private final ParcelStatus currentStatus;
    private final ParcelStatus attemptedStatus;

    public InvalidStatusTransitionException(ParcelStatus currentStatus, ParcelStatus attemptedStatus) {
        super(String.format("Invalid status transition from '%s' to '%s'. This state change violates logistics rules.", 
                currentStatus.getDisplayName(), attemptedStatus.getDisplayName()));
        this.currentStatus = currentStatus;
        this.attemptedStatus = attemptedStatus;
    }

    public ParcelStatus getCurrentStatus() {
        return currentStatus;
    }

    public ParcelStatus getAttemptedStatus() {
        return attemptedStatus;
    }
}
