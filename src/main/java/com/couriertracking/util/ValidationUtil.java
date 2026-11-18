package com.couriertracking.util;

import com.couriertracking.exception.ValidationException;
import java.util.regex.Pattern;

/**
 * Utility for strict user input validation.
 * Ensures data integrity before any database interaction.
 */
public final class ValidationUtil {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,12}$");
    private static final Pattern CONSIGNMENT_PATTERN = Pattern.compile("^CRX[0-9]{12}$");

    private ValidationUtil() {}

    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required and cannot be empty.");
        }
    }

    public static void validatePhone(String phone, String fieldName) throws ValidationException {
        validateNotEmpty(phone, fieldName);
        String cleaned = phone.replaceAll("[^0-9]", "");
        if (!PHONE_PATTERN.matcher(cleaned).matches()) {
            throw new ValidationException(fieldName + " must be a valid 10-digit mobile number.");
        }
    }

    public static void validateWeight(double weight) throws ValidationException {
        if (weight <= 0.0) {
            throw new ValidationException("Parcel weight must be greater than 0 kg.");
        }
        if (weight > 1000.0) {
            throw new ValidationException("Parcel weight exceeds commercial limit (1000 kg).");
        }
    }

    public static void validateConsignmentNo(String consignmentNo) throws ValidationException {
        validateNotEmpty(consignmentNo, "Consignment Number");
        if (consignmentNo.trim().length() < 5) {
            throw new ValidationException("Invalid Consignment Number format.");
        }
    }
}
