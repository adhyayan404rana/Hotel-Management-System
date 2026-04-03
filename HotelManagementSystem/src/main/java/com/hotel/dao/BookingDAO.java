package com.hotel.dao;

import com.hotel.model.Booking;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BookingDAO — DAO for Booking CRUD operations.
 *
 * Uses JOIN queries to fetch denormalised data (customer name, room number)
 * for direct display in TableView without extra round trips.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class BookingDAO {

    private static final Logger LOGGER = Logger.getLogger(BookingDAO.class.getName());

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ---- SELECT with JOIN (used throughout) ----
    private static final String SELECT_WITH_JOIN =
        "SELECT b.*, c.full_name AS customer_name, " +
        "r.room_number, r.room_type " +
        "FROM bookings b " +
        "JOIN customers c ON b.customer_id = c.customer_id " +
        "JOIN rooms r ON b.room_id = r.room_id ";

    // ============================================================
    // CREATE
    // ============================================================

    public int save(Booking booking) {
        String sql = "INSERT INTO bookings (booking_ref, customer_id, room_id, check_in_date, " +
                     "check_out_date, num_guests, total_amount, status, special_requests) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getBookingRef());
            ps.setInt(2, booking.getCustomerId());
            ps.setInt(3, booking.getRoomId());
            ps.setString(4, booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : null);
            ps.setString(5, booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : null);
            ps.setInt(6, booking.getNumGuests());
            ps.setDouble(7, booking.getTotalAmount() != null ? booking.getTotalAmount() : 0.0);
            ps.setString(8, booking.getStatusStr());
            ps.setString(9, booking.getSpecialRequests());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save booking: " + booking.getBookingRef(), e);
        }
        return -1;
    }

    // ============================================================
    // READ
    // ============================================================

    public Optional<Booking> findById(int bookingId) {
        String sql = SELECT_WITH_JOIN + "WHERE b.booking_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding booking by id.", e);
        }
        return Optional.empty();
    }

    public Optional<Booking> findByRef(String bookingRef) {
        String sql = SELECT_WITH_JOIN + "WHERE b.booking_ref = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, bookingRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding booking by ref.", e);
        }
        return Optional.empty();
    }

    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + "ORDER BY b.created_at DESC";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching all bookings.", e);
        }
        return list;
    }

    public List<Booking> findByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + "WHERE b.status = ? ORDER BY b.check_in_date DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding bookings by status.", e);
        }
        return list;
    }

    public List<Booking> findByCustomerId(int customerId) {
        List<Booking> list = new ArrayList<>();
        String sql = SELECT_WITH_JOIN + "WHERE b.customer_id = ? ORDER BY b.created_at DESC";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding bookings by customer.", e);
        }
        return list;
    }

    // ============================================================
    // UPDATE
    // ============================================================

    public boolean updateStatus(int bookingId, Booking.Status newStatus) {
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update booking status.", e);
            return false;
        }
    }

    public boolean updateTotalAmount(int bookingId, double amount) {
        String sql = "UPDATE bookings SET total_amount = ? WHERE booking_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update booking amount.", e);
            return false;
        }
    }

    // ============================================================
    // MAPPER
    // ============================================================

    private Booking mapRow(ResultSet rs) throws SQLException {
        LocalDate checkIn  = parseDate(rs.getString("check_in_date"));
        LocalDate checkOut = parseDate(rs.getString("check_out_date"));

        return new Booking(
            rs.getInt("booking_id"),
            rs.getString("booking_ref"),
            rs.getInt("customer_id"),
            rs.getInt("room_id"),
            rs.getString("customer_name"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            checkIn,
            checkOut,
            rs.getInt("num_guests"),
            rs.getDouble("total_amount"),
            rs.getString("status"),
            rs.getString("special_requests")
        );
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }
}
