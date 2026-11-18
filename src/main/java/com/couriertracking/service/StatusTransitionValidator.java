package com.couriertracking.service;

import com.couriertracking.exception.InvalidStatusTransitionException;
import com.couriertracking.model.ParcelStatus;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State Machine Validator enforcing valid parcel status progression.
 * Prevents illogical transitions (e.g., DELIVERED -> BOOKED).
 */
public class StatusTransitionValidator {

    private static final Map<ParcelStatus, Set<ParcelStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(ParcelStatus.class);

    static {
        // BOOKED can go to PICKED_UP or DELIVERY_FAILED (cancellation)
        ALLOWED_TRANSITIONS.put(ParcelStatus.BOOKED, 
                EnumSet.of(ParcelStatus.PICKED_UP, ParcelStatus.DELIVERY_FAILED));

        // PICKED_UP can go to IN_TRANSIT or DELIVERY_FAILED
        ALLOWED_TRANSITIONS.put(ParcelStatus.PICKED_UP, 
                EnumSet.of(ParcelStatus.IN_TRANSIT, ParcelStatus.DELIVERY_FAILED));

        // IN_TRANSIT can go to ARRIVED_AT_DESTINATION or DELIVERY_FAILED
        ALLOWED_TRANSITIONS.put(ParcelStatus.IN_TRANSIT, 
                EnumSet.of(ParcelStatus.ARRIVED_AT_DESTINATION, ParcelStatus.DELIVERY_FAILED));

        // ARRIVED_AT_DESTINATION can go to OUT_FOR_DELIVERY or rerouted IN_TRANSIT
        ALLOWED_TRANSITIONS.put(ParcelStatus.ARRIVED_AT_DESTINATION, 
                EnumSet.of(ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.IN_TRANSIT, ParcelStatus.DELIVERY_FAILED));

        // OUT_FOR_DELIVERY can go to DELIVERED or DELIVERY_FAILED (failed attempt)
        ALLOWED_TRANSITIONS.put(ParcelStatus.OUT_FOR_DELIVERY, 
                EnumSet.of(ParcelStatus.DELIVERED, ParcelStatus.DELIVERY_FAILED));

        // DELIVERY_FAILED can be re-attempted (OUT_FOR_DELIVERY) or RETURNED
        ALLOWED_TRANSITIONS.put(ParcelStatus.DELIVERY_FAILED, 
                EnumSet.of(ParcelStatus.OUT_FOR_DELIVERY, ParcelStatus.RETURNED));

        // DELIVERED & RETURNED are terminal
        ALLOWED_TRANSITIONS.put(ParcelStatus.DELIVERED, Collections.emptySet());
        ALLOWED_TRANSITIONS.put(ParcelStatus.RETURNED, Collections.emptySet());
    }

    public Set<ParcelStatus> getValidNextStatuses(ParcelStatus current) {
        if (current == null) {
            return Collections.emptySet();
        }
        Set<ParcelStatus> valid = ALLOWED_TRANSITIONS.get(current);
        return valid != null ? valid : Collections.emptySet();
    }

    public boolean isValidTransition(ParcelStatus current, ParcelStatus target) {
        if (current == null || target == null) {
            return false;
        }
        if (current == target) {
            return true; // Allowing reaffirming remarks/location for the same status
        }
        Set<ParcelStatus> valid = getValidNextStatuses(current);
        return valid.contains(target);
    }

    public void validateTransition(ParcelStatus current, ParcelStatus target) throws InvalidStatusTransitionException {
        if (!isValidTransition(current, target)) {
            throw new InvalidStatusTransitionException(current, target);
        }
    }
}
