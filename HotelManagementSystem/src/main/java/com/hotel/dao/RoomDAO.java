package com.hotel.dao;

import com.hotel.model.Room;
import com.hotel.model.RoomFactory;
import com.hotel.model.RoomType;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RoomDAO — Data Access Object for Room CRUD operations.
 *
 * Implements the DAO pattern: all DB interaction for rooms lives here.
 * Uses JDBC with prepared statements to prevent SQL injection.
 *
 * Demonstrates Week 8: ArrayList and Collections usage.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class RoomDAO {

    private static final Logger LOGGER = Logger.getLogger(RoomDAO.class.getName());

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ============================================================
    // CREATE
    // ============================================================

    /**
     * Inserts a new room record into the database.
     *
     * @param room the Room object to persist
     * @return the generated room_id, or -1 on failure
     */
    public int save(Room room) {
        String sql = "INSERT INTO rooms (room_number, room_type, price_per_night, floor, " +
                     "capacity, description, amenities, is_available) VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight());
            ps.setInt(4, room.getFloor());
            ps.setInt(5, room.getCapacity());
            ps.setString(6, room.getDescription());
            ps.setString(7, room.getAmenitiesRaw());
            ps.setInt(8, room.isAvailable() ? 1 : 0);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save room: " + room.getRoomNumber(), e);
        }
        return -1;
    }

    // ============================================================
    // READ
    // ============================================================

    /**
     * Finds a room by its primary key.
     *
     * @param roomId primary key
     * @return Optional<Room> — present if found
     */
    public Optional<Room> findById(int roomId) {
        String sql = "SELECT * FROM rooms WHERE room_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding room by id: " + roomId, e);
        }
        return Optional.empty();
    }

    /**
     * Returns all rooms in the system.
     * Week 8: Returns ArrayList<Room>
     */
    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();   // Week 8: ArrayList
        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching all rooms.", e);
        }
        return rooms;
    }

    /**
     * Returns only available rooms.
     * Week 8: ArrayList filtered from query
     */
    public List<Room> findAvailable() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE is_available = 1 ORDER BY room_type, room_number";

        try (PreparedStatement ps = getConn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRow(rs));
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching available rooms.", e);
        }
        return rooms;
    }

    /**
     * Returns rooms filtered by type.
     *
     * @param type RoomType enum to filter by
     * @return filtered ArrayList of rooms
     */
    public List<Room> findByType(RoomType type) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE room_type = ? ORDER BY room_number";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching rooms by type.", e);
        }
        return rooms;
    }

    // ============================================================
    // UPDATE
    // ============================================================

    /**
     * Updates a room's availability status.
     *
     * @param roomId      the room to update
     * @param isAvailable true = available, false = occupied
     */
    public boolean updateAvailability(int roomId, boolean isAvailable) {
        String sql = "UPDATE rooms SET is_available = ? WHERE room_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, isAvailable ? 1 : 0);
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update room availability.", e);
            return false;
        }
    }

    /**
     * Updates all fields of an existing room.
     */
    public boolean update(Room room) {
        String sql = "UPDATE rooms SET room_number=?, room_type=?, price_per_night=?, " +
                     "floor=?, capacity=?, description=?, amenities=?, is_available=? " +
                     "WHERE room_id=?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight());
            ps.setInt(4, room.getFloor());
            ps.setInt(5, room.getCapacity());
            ps.setString(6, room.getDescription());
            ps.setString(7, room.getAmenitiesRaw());
            ps.setInt(8, room.isAvailable() ? 1 : 0);
            ps.setInt(9, room.getRoomId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update room.", e);
            return false;
        }
    }

    // ============================================================
    // DELETE
    // ============================================================

    /**
     * Deletes a room by ID (only allowed if room has no active bookings).
     */
    public boolean delete(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to delete room " + roomId, e);
            return false;
        }
    }

    // ============================================================
    // HELPER
    // ============================================================

    /**
     * Maps a ResultSet row to a Room object using the RoomFactory.
     * Demonstrates Polymorphism: correct subclass is instantiated at runtime.
     */
    private Room mapRow(ResultSet rs) throws SQLException {
        return RoomFactory.create(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getDouble("price_per_night"),
            rs.getInt("floor"),
            rs.getInt("capacity"),
            rs.getString("description"),
            rs.getString("amenities"),
            rs.getInt("is_available") == 1
        );
    }
}
