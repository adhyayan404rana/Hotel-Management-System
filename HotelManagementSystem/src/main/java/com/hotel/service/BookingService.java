package com.hotel.service;

import com.hotel.dao.BookingDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Booking;
import com.hotel.model.Room;
import com.hotel.util.BillingCalculator;
import com.hotel.util.FileBackupUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BookingService — Core business logic layer for hotel reservations.
 *
 * Demonstrates Week 4 concepts:
 *   - Synchronization (synchronized method) to prevent double booking
 *   - wait() / notify() for room availability coordination
 *
 * Demonstrates Week 8 concepts:
 *   - HashMap for fast room-to-booking lookup
 *   - ArrayList for ordered booking history
 *   - Iterator for traversal
 *   - Collections.sort() for sorting
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class BookingService {

    private static final Logger LOGGER = Logger.getLogger(BookingService.class.getName());

    private final BookingDAO bookingDAO;
    private final RoomDAO roomDAO;
    private final HousekeepingService housekeepingService;

    // Week 8: HashMap — fast O(1) lookup of which room is occupied by which booking
    // Key: roomId, Value: active Booking object
    private final HashMap<Integer, Booking> roomBookingMap = new HashMap<>();

    // Week 8: ArrayList — ordered list of all bookings in this session
    private final ArrayList<Booking> sessionBookings = new ArrayList<>();

    // Monitor for synchronization (Week 4)
    private final Object bookingLock = new Object();

    public BookingService(BookingDAO bookingDAO, RoomDAO roomDAO,
                          HousekeepingService housekeepingService) {
        this.bookingDAO = bookingDAO;
        this.roomDAO = roomDAO;
        this.housekeepingService = housekeepingService;
        loadActiveBookingsIntoMap();
    }

    /**
     * Loads all ACTIVE bookings from DB into the in-memory HashMap.
     * Uses Iterator (Week 8) to populate the map.
     */
    private void loadActiveBookingsIntoMap() {
        List<Booking> active = bookingDAO.findByStatus("ACTIVE");
        Iterator<Booking> iterator = active.iterator();   // Week 8: explicit Iterator
        while (iterator.hasNext()) {
            Booking b = iterator.next();
            roomBookingMap.put(b.getRoomId(), b);
            sessionBookings.add(b);
        }
        LOGGER.info("Loaded " + sessionBookings.size() + " active bookings into memory.");
    }

    // ============================================================
    // WEEK 4: SYNCHRONIZED METHOD — Prevents Double Booking
    // ============================================================

    /**
     * Books a room for a customer.
     *
     * This method is SYNCHRONIZED to prevent two threads from booking
     * the same room simultaneously — a critical race condition in hotels.
     *
     * Week 4: The entire method is a critical section.
     *
     * @param customerId    customer making the booking
     * @param roomId        room to be booked
     * @param checkIn       check-in date
     * @param checkOut      check-out date
     * @param numGuests     number of guests
     * @param specialReqs   special requests
     * @return              the created Booking, or null if room is not available
     * @throws IllegalStateException if room is already booked
     */
    public synchronized Booking bookRoom(int customerId, int roomId,
                                         LocalDate checkIn, LocalDate checkOut,
                                         int numGuests, String specialReqs) {

        // Week 4: Check availability inside synchronized block — CRITICAL SECTION
        synchronized (bookingLock) {

            // Double-check: Is this room already in our in-memory map?
            if (roomBookingMap.containsKey(roomId)) {
                Booking existing = roomBookingMap.get(roomId);
                LOGGER.warning("Double-booking attempt blocked! Room " + roomId +
                               " is already booked under " + existing.getBookingRef());
                throw new IllegalStateException(
                    "Room is already occupied. Booking Ref: " + existing.getBookingRef());
            }

            // Also verify availability in the database (persistence layer check)
            Optional<Room> roomOpt = roomDAO.findById(roomId);
            if (roomOpt.isEmpty() || !roomOpt.get().isAvailable()) {
                throw new IllegalStateException("Room is not available for booking.");
            }

            Room room = roomOpt.get();

            // --- Create the booking ---
            String bookingRef = Booking.generateBookingRef();
            Booking newBooking = new Booking(bookingRef, customerId, roomId,
                                             checkIn, checkOut, numGuests);
            newBooking.setSpecialRequests(specialReqs);

            // Calculate total amount using generic BillingCalculator (Week 7)
            int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
            BillingCalculator.BillSummary summary =
                    BillingCalculator.computeFinalBill(room, nights, new ArrayList<>());
            newBooking.setTotalAmount(summary.grandTotal);

            // Persist to DB
            int bookingId = bookingDAO.save(newBooking);
            if (bookingId <= 0) {
                throw new RuntimeException("Failed to save booking to database.");
            }
            newBooking.setBookingId(bookingId);

            // Mark room as occupied in DB
            roomDAO.updateAvailability(roomId, false);

            // Update in-memory map (Week 8: HashMap put)
            roomBookingMap.put(roomId, newBooking);
            sessionBookings.add(newBooking);

            // Week 4: Notify any threads waiting on this lock
            bookingLock.notifyAll();

            // Log the action (Week 5: File writing)
            FileBackupUtil.writeAuditLog("BOOK_ROOM",
                    "Ref=" + bookingRef + " | Room=" + room.getRoomNumber() +
                    " | Customer=" + customerId + " | Nights=" + nights);

            // Background booking processing (Week 3: Runnable/Thread)
            housekeepingService.processBookingAsync(bookingRef, "Customer#" + customerId, null);

            return newBooking;
        }
    }

    /**
     * Processes checkout for a booking.
     *
     * Synchronized to prevent concurrent modification of the booking map.
     *
     * @param bookingId ID of the booking to check out
     * @return          the updated Booking with CHECKED_OUT status
     */
    public synchronized Booking checkout(int bookingId) {
        synchronized (bookingLock) {

            Optional<Booking> opt = bookingDAO.findById(bookingId);
            if (opt.isEmpty()) {
                throw new IllegalArgumentException("Booking #" + bookingId + " not found.");
            }

            Booking booking = opt.get();
            if (!booking.isActive()) {
                throw new IllegalStateException("Booking is not in ACTIVE state.");
            }

            // Update status in DB
            bookingDAO.updateStatus(bookingId, Booking.Status.CHECKED_OUT);
            booking.setStatus(Booking.Status.CHECKED_OUT);

            // Free the room in DB
            roomDAO.updateAvailability(booking.getRoomId(), true);

            // Remove from in-memory map (Week 8: HashMap remove)
            roomBookingMap.remove(booking.getRoomId());

            // Update sessionBookings list
            sessionBookings.removeIf(b -> b.getBookingId() == bookingId);

            // Trigger housekeeping for the vacated room
            housekeepingService.cleanRoom(booking.getRoomNumber(), null);

            // Week 4: Notify waiting threads that a room is now free
            bookingLock.notifyAll();

            FileBackupUtil.writeAuditLog("CHECKOUT",
                    "Ref=" + booking.getBookingRef() + " | Room=" + booking.getRoomNumber());

            return booking;
        }
    }

    /**
     * Retrieves all bookings, sorted by check-in date descending.
     * Demonstrates Week 8: Collections.sort() with Comparator.
     *
     * @return sorted list of all bookings
     */
    public List<Booking> getAllBookingsSorted() {
        List<Booking> all = bookingDAO.findAll();

        // Week 8: Collections.sort() with lambda Comparator
        Collections.sort(all, (b1, b2) -> {
            if (b1.getCheckInDate() == null) return 1;
            if (b2.getCheckInDate() == null) return -1;
            return b2.getCheckInDate().compareTo(b1.getCheckInDate());
        });

        return all;
    }

    /**
     * Finds active booking for a specific room using the HashMap.
     * O(1) lookup — Week 8: HashMap.get()
     *
     * @param roomId the room to look up
     * @return Optional containing the booking if found
     */
    public Optional<Booking> getActiveBookingForRoom(int roomId) {
        return Optional.ofNullable(roomBookingMap.get(roomId));
    }

    /**
     * Returns all currently active bookings.
     * Week 8: HashMap.values() + ArrayList construction
     */
    public List<Booking> getActiveBookings() {
        return new ArrayList<>(roomBookingMap.values());
    }

    /** Returns the in-session booking list for quick access */
    public List<Booking> getSessionBookings() {
        return Collections.unmodifiableList(sessionBookings);
    }

    /** Refreshes the in-memory map from DB */
    public void refresh() {
        synchronized (bookingLock) {
            roomBookingMap.clear();
            sessionBookings.clear();
            loadActiveBookingsIntoMap();
        }
    }
}
