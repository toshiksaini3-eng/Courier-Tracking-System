package com.couriertracking.service;

import com.couriertracking.model.ParcelType;

/**
 * Service to compute shipping and delivery tariffs based on weight, parcel type,
 * and logistics business rules. Demonstrates Polymorphism and Strategy design.
 */
public class DeliveryChargeCalculator {

    /**
     * Calculates total delivery charge.
     * 
     * Rule Set:
     * - Document:
     *   Base fee: ₹50.00 (covers up to 0.5 kg)
     *   Additional weight: ₹20.00 per additional 0.5 kg fraction
     * 
     * - Package:
     *   Base fee: ₹100.00 (covers up to 1.0 kg)
     *   Additional weight: ₹40.00 per additional 1.0 kg fraction
     */
    public double calculateCharge(ParcelType type, double weightKg) {
        if (weightKg <= 0) {
            return 0.0;
        }

        if (type == ParcelType.DOCUMENT) {
            double baseRate = 50.0;
            if (weightKg <= 0.5) {
                return baseRate;
            }
            double extraWeight = weightKg - 0.5;
            int additionalUnits = (int) Math.ceil(extraWeight / 0.5);
            return baseRate + (additionalUnits * 20.0);
        } else {
            double baseRate = 100.0;
            if (weightKg <= 1.0) {
                return baseRate;
            }
            double extraWeight = weightKg - 1.0;
            int additionalUnits = (int) Math.ceil(extraWeight / 1.0);
            return baseRate + (additionalUnits * 40.0);
        }
    }
}
