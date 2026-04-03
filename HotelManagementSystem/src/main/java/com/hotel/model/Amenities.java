package com.hotel.model;

import java.util.List;

/**
 * Amenities — Interface defining the contract for hotel room amenities.
 *
 * Demonstrates Week 1 concept: Interface design and polymorphism.
 * Any class implementing this must provide amenity-related behaviour.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public interface Amenities {

    /**
     * Returns a list of all amenities available in this room.
     * e.g., ["WiFi", "AC", "Mini-Bar", "Jacuzzi"]
     */
    List<String> getAmenities();

    /**
     * Checks if a specific amenity is present in this room.
     *
     * @param amenityName name of the amenity to check
     * @return true if available, false otherwise
     */
    boolean hasAmenity(String amenityName);

    /**
     * Returns the amenity description as a formatted string for display.
     * Default method — implementing classes may override for custom formatting.
     */
    default String getAmenitiesDisplay() {
        List<String> list = getAmenities();
        if (list == null || list.isEmpty()) return "No amenities listed";
        return String.join(" • ", list);
    }

    /**
     * Returns the total count of amenities.
     */
    default int getAmenityCount() {
        List<String> list = getAmenities();
        return (list == null) ? 0 : list.size();
    }
}
