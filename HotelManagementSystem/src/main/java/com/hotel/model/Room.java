package com.hotel.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Room — Abstract base class for all hotel rooms.
 *
 * Demonstrates Week 1 concepts:
 *   - Abstraction (abstract class with abstract methods)
 *   - Encapsulation (private fields + getters/setters)
 *   - Implements Amenities interface (polymorphism via interface)
 *   - Constructor overloading
 *   - 'this' keyword usage
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public abstract class Room implements Amenities {

    // ---- Encapsulation: all fields are private ----
    private int roomId;
    private String roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private int floor;
    private int capacity;
    private String description;
    private List<String> amenitiesList;  // Week 8: ArrayList instead of array
    private boolean available;
    private String createdAt;

    // ============================================================
    // CONSTRUCTOR OVERLOADING (Week 1)
    // ============================================================

    /**
     * Minimal constructor — for creating a basic room stub.
     */
    public Room(String roomNumber, RoomType roomType) {
        // 'this' keyword refers to the current object
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = roomType.getBasePrice();
        this.floor = 1;
        this.capacity = 2;
        this.available = true;
        this.amenitiesList = new ArrayList<>();
    }

    /**
     * Full constructor — used when loading from database.
     *
     * 'this' keyword disambiguation between field and parameter.
     */
    public Room(int roomId, String roomNumber, RoomType roomType,
                double pricePerNight, int floor, int capacity,
                String description, String amenitiesRaw, boolean available) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.pricePerNight = pricePerNight;
        this.floor = floor;
        this.capacity = capacity;
        this.description = description;
        this.available = available;
        this.amenitiesList = parseAmenities(amenitiesRaw);
    }

    /**
     * Constructor with chaining — delegates to full constructor via this().
     * Constructor overloading demo (Week 1).
     */
    public Room(String roomNumber, RoomType roomType, double pricePerNight, int floor) {
        this(0, roomNumber, roomType, pricePerNight, floor, 2, "", "", true);
    }

    // ============================================================
    // ABSTRACT METHODS — Subclasses MUST implement these
    // ============================================================

    /**
     * Returns a human-readable room category label.
     * e.g., "Standard Room", "Deluxe Balcony Suite"
     */
    public abstract String getRoomCategory();

    /**
     * Returns the service charge percentage for this room type.
     * Overridden by each subclass (Polymorphism — Week 1).
     */
    public abstract double getServiceChargePercent();

    /**
     * Calculates the total bill for the given number of nights.
     * Polymorphism: each subclass may apply its own pricing logic.
     */
    public abstract double calculateBill(int nights);

    // ============================================================
    // AMENITIES INTERFACE IMPLEMENTATION
    // ============================================================

    @Override
    public List<String> getAmenities() {
        return new ArrayList<>(amenitiesList);  // Defensive copy
    }

    @Override
    public boolean hasAmenity(String amenityName) {
        if (amenityName == null) return false;
        return amenitiesList.stream()
                .anyMatch(a -> a.equalsIgnoreCase(amenityName.trim()));
    }

    // ============================================================
    // BUSINESS LOGIC
    // ============================================================

    /**
     * Parses a comma-separated amenities string from DB into a List.
     * e.g., "WiFi,AC,TV,Mini-Bar" → ["WiFi", "AC", "TV", "Mini-Bar"]
     */
    private List<String> parseAmenities(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    /**
     * Converts amenities list back to a comma-separated string for storage.
     */
    public String getAmenitiesRaw() {
        return String.join(",", amenitiesList);
    }

    /**
     * Adds an amenity to this room's list (if not already present).
     */
    public void addAmenity(String amenity) {
        if (amenity != null && !amenity.trim().isEmpty() && !hasAmenity(amenity)) {
            this.amenitiesList.add(amenity.trim());
        }
    }

    /**
     * Returns a formatted display string for room cards in the UI.
     */
    public String getDisplaySummary() {
        return String.format("%s %s | Floor %d | Capacity: %d | ₹%.0f/night | %s",
                roomType.getIcon(), roomNumber, floor, capacity, pricePerNight,
                available ? "✅ Available" : "🔴 Occupied");
    }

    // ============================================================
    // GETTERS AND SETTERS (Encapsulation — Week 1)
    // ============================================================

    public int getRoomId()                   { return roomId; }
    public void setRoomId(int roomId)        { this.roomId = roomId; }

    public String getRoomNumber()            { return roomNumber; }
    public void setRoomNumber(String n)      { this.roomNumber = n; }

    public RoomType getRoomType()            { return roomType; }
    public void setRoomType(RoomType t)      { this.roomType = t; }

    public double getPricePerNight()         { return pricePerNight; }
    public void setPricePerNight(double p)   { this.pricePerNight = p; }

    public int getFloor()                    { return floor; }
    public void setFloor(int f)              { this.floor = f; }

    public int getCapacity()                 { return capacity; }
    public void setCapacity(int c)           { this.capacity = c; }

    public String getDescription()           { return description; }
    public void setDescription(String d)     { this.description = d; }

    public boolean isAvailable()             { return available; }
    public void setAvailable(boolean avail)  { this.available = avail; }

    public String getCreatedAt()             { return createdAt; }
    public void setCreatedAt(String c)       { this.createdAt = c; }

    // Convenience for TableView binding
    public String getAvailabilityStatus()    { return available ? "Available" : "Occupied"; }
    public String getRoomTypeName()          { return roomType.getDisplayName(); }

    @Override
    public String toString() {
        return "Room[" + roomNumber + " | " + roomType.getDisplayName() +
               " | ₹" + pricePerNight + " | " + (available ? "Free" : "Occupied") + "]";
    }
}
