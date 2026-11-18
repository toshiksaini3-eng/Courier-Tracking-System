package com.couriertracking.exception;

/**
 * Base checked exception for the Courier Tracking System.
 * Demonstrates Java Exception Handling hierarchy.
 */
public class CourierException extends Exception {
    public CourierException(String message) {
        super(message);
    }

    public CourierException(String message, Throwable cause) {
        super(message, cause);
    }
}
