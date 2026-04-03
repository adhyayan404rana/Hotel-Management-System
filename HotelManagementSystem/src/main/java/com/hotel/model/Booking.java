package com.hotel.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Booking — Represents a hotel reservation.
 *
 * Implements Serializable (Week 6: serialization/deserialization).
 * Uses LocalDate for date arithmetic.
 * Demonstrates Encapsulation (Week 1) and Wrapper Classes (Week 2).
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class Booking implements Serializable {

    private static final long serialVersionUID = 1002L;

    // ---- Booking status constants ----
    public enum Status {
        ACTIVE, CHECKED_OUT, CANCELLED
    }

    // ---- Private fields (Encapsulation) ----
    private int bookingId;
    private String bookingRef;       // e.g., "GV-2024-001"
    private int customerId;
    private int roomId;
    private String customerName;     // Denormalised for display
    private String roomNumber;       // Denormalised for display
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numGuests;
    private Double totalAmount;      // Double wrapper (Week 2: autoboxing)
    private Status status;
    private String specialRequests;
    private String createdAt;

    // ============================================================
    // CONSTRUCTORS (Overloading — Week 1)
    // ============================================================

    public Booking() {
        this.status = Status.ACTIVE;
        this.numGuests = 1;
        this.totalAmount = 0.0;       // Auto-boxed Double
    }

    /** Constructor for creating a new booking */
    public Booking(String bookingRef, int customerId, int roomId,
                   LocalDate checkIn, LocalDate checkOut, int numGuests) {
        this();
        this.bookingRef = bookingRef;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkInDate = checkIn;
        this.checkOutDate = checkOut;
        this.numGuests = numGuests;
    }

    /** Full constructor for loading from database */
    public Booking(int bookingId, String bookingRef, int customerId, int roomId,
                   String customerName, String roomNumber, String roomType,
                   LocalDate checkInDate, LocalDate checkOutDate, int numGuests,
                   double totalAmount, String statusStr, String specialRequests) {
        this.bookingId = bookingId;
        this.bookingRef = bookingRef;
        this.customerId = customerId;
        this.roomId = roomId;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numGuests = numGuests;
        this.totalAmount = Double.valueOf(totalAmount);  // Explicit boxing (Week 2)
        this.specialRequests = specialRequests;

        try {
            this.status = Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            this.status = Status.ACTIVE;
        }
    }

    // ============================================================
    // BUSINESS LOGIC
    // ============================================================

    /**
     * Calculates the number of nights between check-in and check-out.
     * Returns Integer (wrapper class — Week 2 autoboxing demo).
     */
    public Integer getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return Integer.valueOf((int) days);  // Explicit autoboxing
    }

    /**
     * Generates a unique booking reference.
     * Format: GV-YYYY-NNNNN
     */
    public static String generateBookingRef() {
        int year = LocalDate.now().getYear();
        long timestamp = System.currentTimeMillis() % 100000;
        return String.format("GV-%d-%05d", year, timestamp);
    }

    /** Returns true if this is an active (checked-in) booking */
    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }

    /** Formatted total amount with currency symbol */
    public String getFormattedAmount() {
        if (totalAmount == null) return "₹0.00";
        return String.format("₹%,.2f", totalAmount);
    }

    /** Formatted check-in date string */
    public String getCheckInStr() {
        return checkInDate != null ? checkInDate.toString() : "";
    }

    /** Formatted check-out date string */
    public String getCheckOutStr() {
        return checkOutDate != null ? checkOutDate.toString() : "";
    }

    // ============================================================
    // GETTERS & SETTERS
    // ============================================================

    public int getBookingId()                  { return bookingId; }
    public void setBookingId(int id)           { this.bookingId = id; }

    public String getBookingRef()              { return bookingRef; }
    public void setBookingRef(String r)        { this.bookingRef = r; }

    public int getCustomerId()                 { return customerId; }
    public void setCustomerId(int id)          { this.customerId = id; }

    public int getRoomId()                     { return roomId; }
    public void setRoomId(int id)              { this.roomId = id; }

    public String getCustomerName()            { return customerName; }
    public void setCustomerName(String n)      { this.customerName = n; }

    public String getRoomNumber()              { return roomNumber; }
    public void setRoomNumber(String n)        { this.roomNumber = n; }

    public String getRoomType()                { return roomType; }
    public void setRoomType(String t)          { this.roomType = t; }

    public LocalDate getCheckInDate()          { return checkInDate; }
    public void setCheckInDate(LocalDate d)    { this.checkInDate = d; }

    public LocalDate getCheckOutDate()         { return checkOutDate; }
    public void setCheckOutDate(LocalDate d)   { this.checkOutDate = d; }

    public int getNumGuests()                  { return numGuests; }
    public void setNumGuests(int g)            { this.numGuests = g; }

    public Double getTotalAmount()             { return totalAmount; }
    public void setTotalAmount(Double a)       { this.totalAmount = a; }

    public Status getStatus()                  { return status; }
    public void setStatus(Status s)            { this.status = s; }
    public String getStatusStr()               { return status != null ? status.name() : "ACTIVE"; }

    public String getSpecialRequests()         { return specialRequests; }
    public void setSpecialRequests(String r)   { this.specialRequests = r; }

    public String getCreatedAt()               { return createdAt; }
    public void setCreatedAt(String c)         { this.createdAt = c; }

    @Override
    public String toString() {
        return bookingRef + " | " + customerName + " | Room " + roomNumber +
               " | " + checkInDate + " → " + checkOutDate;
    }
}
