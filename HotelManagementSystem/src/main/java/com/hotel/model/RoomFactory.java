package com.hotel.model;

/**
 * RoomFactory — Factory class to instantiate the correct Room subclass.
 *
 * Decouples object creation from business logic.
 * Used by DAOs when loading rooms from the database.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class RoomFactory {

    // Prevent instantiation — utility class
    private RoomFactory() {}

    /**
     * Creates the appropriate Room subclass based on the room type string.
     * Demonstrates Polymorphism: same interface, different runtime types.
     *
     * @param roomId         database primary key
     * @param roomNumber     room number (e.g., "201")
     * @param roomTypeStr    type string from DB (e.g., "DELUXE")
     * @param pricePerNight  nightly rate
     * @param floor          floor number
     * @param capacity       max guests
     * @param description    room description
     * @param amenities      comma-separated amenities string
     * @param available      availability flag
     * @return               correct Room subclass instance
     */
    public static Room create(int roomId, String roomNumber, String roomTypeStr,
                              double pricePerNight, int floor, int capacity,
                              String description, String amenities, boolean available) {

        RoomType type = RoomType.fromString(roomTypeStr);

        return switch (type) {
            case STANDARD  -> new StandardRoom(roomId, roomNumber, pricePerNight,
                                               floor, capacity, description, amenities, available);
            case DELUXE    -> new DeluxeRoom(roomId, roomNumber, pricePerNight,
                                             floor, capacity, description, amenities, available);
            case SUITE     -> new SuiteRoom(roomId, roomNumber, pricePerNight,
                                             floor, capacity, description, amenities, available);
            case PENTHOUSE -> new PenthouseRoom(roomId, roomNumber, pricePerNight,
                                                floor, capacity, description, amenities, available);
        };
    }
}
