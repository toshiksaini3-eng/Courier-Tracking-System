package com.couriertracking.exception;

/**
 * Thrown when user inputs fail business validation rules.
 */
public class ValidationException extends CourierException {
    public ValidationException(String message) {
        super(message);
    }
}
