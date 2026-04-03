package com.hotel.model;

/**
 * StandardRoom — Concrete implementation of Room for standard-tier rooms.
 *
 * Demonstrates Week 1: Inheritance, method overriding (Polymorphism),
 * super keyword usage, constructor chaining.
 *
 * @author Grand Vista HMS
 */
public class StandardRoom extends Room {

    private static final double SERVICE_CHARGE = 10.0;

    // Full constructor — calls parent via super (Week 1: super keyword)
    public StandardRoom(int roomId, String roomNumber, double pricePerNight,
                        int floor, int capacity, String description,
                        String amenities, boolean available) {
        super(roomId, roomNumber, RoomType.STANDARD, pricePerNight,
              floor, capacity, description, amenities, available);
    }

    // Convenience constructor for new rooms (Constructor overloading)
    public StandardRoom(String roomNumber, int floor) {
        super(roomNumber, RoomType.STANDARD, RoomType.STANDARD.getBasePrice(), floor);
    }

    @Override
    public String getRoomCategory() {
        return "Standard Room";
    }

    @Override
    public double getServiceChargePercent() {
        return SERVICE_CHARGE;
    }

    /**
     * Calculates bill: room rate × nights + 10% service charge.
     * Polymorphic override of abstract method.
     */
    @Override
    public double calculateBill(int nights) {
        double base = getPricePerNight() * nights;
        double service = base * (SERVICE_CHARGE / 100.0);
        return base + service;
    }
}


// ============================================================
//  DeluxeRoom — extends Room (Inheritance)
// ============================================================

/**
 * DeluxeRoom — Mid-tier room with higher service charge and a balcony option.
 *
 * Demonstrates: Inheritance from Room, super(), method overriding.
 */
class DeluxeRoom extends Room {

    private static final double SERVICE_CHARGE = 15.0;
    private boolean hasBalcony;

    public DeluxeRoom(int roomId, String roomNumber, double pricePerNight,
                      int floor, int capacity, String description,
                      String amenities, boolean available) {
        super(roomId, roomNumber, RoomType.DELUXE, pricePerNight,
              floor, capacity, description, amenities, available);
        // A DeluxeRoom has a balcony if the amenities string mentions it
        this.hasBalcony = amenities != null && amenities.contains("Balcony");
    }

    public DeluxeRoom(String roomNumber, int floor) {
        super(roomNumber, RoomType.DELUXE, RoomType.DELUXE.getBasePrice(), floor);
        this.hasBalcony = false;
    }

    @Override
    public String getRoomCategory() {
        return hasBalcony ? "Deluxe Room with Balcony" : "Deluxe Room";
    }

    @Override
    public double getServiceChargePercent() {
        return SERVICE_CHARGE;
    }

    @Override
    public double calculateBill(int nights) {
        double base = getPricePerNight() * nights;
        double service = base * (SERVICE_CHARGE / 100.0);
        // Balcony surcharge: ₹500/night extra
        double balconySurcharge = hasBalcony ? 500.0 * nights : 0.0;
        return base + service + balconySurcharge;
    }

    public boolean isHasBalcony() { return hasBalcony; }
}


// ============================================================
//  SuiteRoom — extends Room (Inheritance)
// ============================================================

/**
 * SuiteRoom — Luxury suite with butler service and higher rates.
 *
 * Demonstrates: Multi-level inheritance concepts, overriding, super.
 */
class SuiteRoom extends Room {

    private static final double SERVICE_CHARGE = 18.0;
    private boolean hasButler;

    public SuiteRoom(int roomId, String roomNumber, double pricePerNight,
                     int floor, int capacity, String description,
                     String amenities, boolean available) {
        super(roomId, roomNumber, RoomType.SUITE, pricePerNight,
              floor, capacity, description, amenities, available);
        this.hasButler = amenities != null && amenities.contains("Butler");
    }

    public SuiteRoom(String roomNumber, int floor) {
        super(roomNumber, RoomType.SUITE, RoomType.SUITE.getBasePrice(), floor);
        this.hasButler = true;
    }

    @Override
    public String getRoomCategory() {
        return "Luxury Suite";
    }

    @Override
    public double getServiceChargePercent() {
        return SERVICE_CHARGE;
    }

    @Override
    public double calculateBill(int nights) {
        double base = getPricePerNight() * nights;
        double service = base * (SERVICE_CHARGE / 100.0);
        // Butler service: ₹2000/night
        double butlerCost = hasButler ? 2000.0 * nights : 0.0;
        return base + service + butlerCost;
    }

    public boolean isHasButler() { return hasButler; }
}


// ============================================================
//  PenthouseRoom — extends Room (Inheritance)
// ============================================================

/**
 * PenthouseRoom — The most luxurious option.
 *
 * Demonstrates: Complete polymorphism chain, unique billing logic.
 */
class PenthouseRoom extends Room {

    private static final double SERVICE_CHARGE = 22.0;
    private static final double PRIVATE_CHEF_RATE = 5000.0;  // per night

    public PenthouseRoom(int roomId, String roomNumber, double pricePerNight,
                         int floor, int capacity, String description,
                         String amenities, boolean available) {
        super(roomId, roomNumber, RoomType.PENTHOUSE, pricePerNight,
              floor, capacity, description, amenities, available);
    }

    public PenthouseRoom(String roomNumber) {
        super(roomNumber, RoomType.PENTHOUSE, RoomType.PENTHOUSE.getBasePrice(), 4);
    }

    @Override
    public String getRoomCategory() {
        return "The Grand Penthouse";
    }

    @Override
    public double getServiceChargePercent() {
        return SERVICE_CHARGE;
    }

    @Override
    public double calculateBill(int nights) {
        double base = getPricePerNight() * nights;
        double service = base * (SERVICE_CHARGE / 100.0);
        double chefCost = PRIVATE_CHEF_RATE * nights;
        return base + service + chefCost;
    }
}
