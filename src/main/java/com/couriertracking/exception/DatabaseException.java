package com.couriertracking.exception;

/**
 * Thrown when database operations, connection failures, or SQL executions fail.
 */
public class DatabaseException extends CourierException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
