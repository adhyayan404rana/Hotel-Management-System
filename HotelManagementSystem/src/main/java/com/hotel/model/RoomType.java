package com.hotel.model;

/**
 * RoomType — Enum representing all available room categories.
 *
 * Demonstrates Week 2 concept: Enum with fields, constructors, and methods.
 * Each type carries its base price, service charge rate, and a display label.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public enum RoomType {

    // Each constant has: displayName, basePrice, serviceChargePercent, icon
    STANDARD("Standard Room", 3500.00, 10.0, "🛏"),
    DELUXE("Deluxe Room", 6500.00, 15.0, "🌟"),
    SUITE("Suite", 12000.00, 18.0, "👑"),
    PENTHOUSE("Penthouse", 25000.00, 22.0, "🏰");

    // ---- Fields (Encapsulation: private with getters) ----
    private final String displayName;
    private final double basePrice;
    private final double serviceChargePercent;
    private final String icon;

    // ---- Enum Constructor ----
    RoomType(String displayName, double basePrice, double serviceChargePercent, String icon) {
        this.displayName = displayName;
        this.basePrice = basePrice;
        this.serviceChargePercent = serviceChargePercent;
        this.icon = icon;
    }

    // ---- Getters ----
    public String getDisplayName()         { return displayName; }
    public double getBasePrice()           { return basePrice; }
    public double getServiceChargePercent(){ return serviceChargePercent; }
    public String getIcon()                { return icon; }

    /**
     * Calculates total bill for a given number of nights using Autoboxing (Week 2).
     * Uses Double wrapper class intentionally for autoboxing demonstration.
     *
     * @param nights number of nights (Integer wrapper — autoboxing)
     * @return total amount including service charge
     */
    public Double calculateTotalBill(Integer nights) {
        // Autoboxing: Integer nights auto-unboxed to int for arithmetic
        double roomCost = basePrice * nights;          // unboxing happens here
        double serviceCharge = roomCost * (serviceChargePercent / 100.0);
        // Result autoboxed into Double (wrapper class)
        return Double.valueOf(roomCost + serviceCharge);
    }

    /**
     * Returns per-night rate with service charge included.
     */
    public double getPriceWithService() {
        return basePrice + (basePrice * serviceChargePercent / 100.0);
    }

    /**
     * Parses a string back to RoomType safely (avoids IllegalArgumentException).
     */
    public static RoomType fromString(String value) {
        if (value == null) return STANDARD;
        try {
            return RoomType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return STANDARD;
        }
    }

    @Override
    public String toString() {
        return icon + " " + displayName;
    }
}
