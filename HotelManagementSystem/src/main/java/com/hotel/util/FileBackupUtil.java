package com.hotel.util;

import com.hotel.model.Booking;
import com.hotel.model.Customer;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileBackupUtil — Handles file-based backup and serialization operations.
 *
 * Demonstrates Week 5 concepts:
 *   - FileReader / FileWriter for text-based backup
 *   - BufferedReader / BufferedWriter for efficient I/O
 *
 * Demonstrates Week 6 concepts:
 *   - ObjectOutputStream / ObjectInputStream for serialization
 *   - RandomAccessFile for direct record lookup
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class FileBackupUtil {

    private static final Logger LOGGER = Logger.getLogger(FileBackupUtil.class.getName());
    private static final String BACKUP_DIR = "backups/";
    private static final String BOOKING_SERIAL_FILE = BACKUP_DIR + "bookings.ser";
    private static final String CUSTOMER_SERIAL_FILE = BACKUP_DIR + "customers.ser";
    private static final String AUDIT_LOG_FILE = BACKUP_DIR + "audit.log";

    // Ensure backup directory exists
    static {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            LOGGER.warning("Could not create backup directory: " + e.getMessage());
        }
    }

    // ============================================================
    // WEEK 5: FILE HANDLING — FileWriter / FileReader
    // ============================================================

    /**
     * Writes a plain-text audit log entry using FileWriter + BufferedWriter.
     * Demonstrates Week 5: FileWriter (append mode) and BufferedWriter.
     *
     * @param action  action performed (e.g., "BOOKING", "CHECKOUT")
     * @param details details of the action
     */
    public static void writeAuditLog(String action, String details) {
        // FileWriter in append mode (true) — Week 5 concept
        try (FileWriter fw = new FileWriter(AUDIT_LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            bw.write(String.format("[%s] [%s] %s%n", timestamp, action, details));
            bw.flush();

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to write audit log.", e);
        }
    }

    /**
     * Reads all audit log entries using FileReader + BufferedReader.
     * Demonstrates Week 5: FileReader and BufferedReader.
     *
     * @return List of log entries as strings
     */
    public static List<String> readAuditLog() {
        List<String> entries = new ArrayList<>();
        File logFile = new File(AUDIT_LOG_FILE);

        if (!logFile.exists()) return entries;

        // FileReader + BufferedReader — Week 5 concept
        try (FileReader fr = new FileReader(logFile);
             BufferedReader br = new BufferedReader(fr)) {

            String line;
            while ((line = br.readLine()) != null) {
                entries.add(line);
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to read audit log.", e);
        }
        return entries;
    }

    /**
     * Exports all bookings to a human-readable CSV text file.
     * Demonstrates Week 5: FileWriter for structured text export.
     *
     * @param bookings list of bookings to export
     */
    public static void exportBookingsToCsv(List<Booking> bookings) {
        String fileName = BACKUP_DIR + "bookings_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        try (FileWriter fw = new FileWriter(fileName);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // CSV header
            bw.write("BookingRef,CustomerName,RoomNumber,CheckIn,CheckOut,Nights,TotalAmount,Status");
            bw.newLine();

            for (Booking b : bookings) {
                bw.write(String.format("%s,%s,%s,%s,%s,%d,%.2f,%s",
                        b.getBookingRef(),
                        b.getCustomerName(),
                        b.getRoomNumber(),
                        b.getCheckInStr(),
                        b.getCheckOutStr(),
                        b.getNumberOfNights(),
                        b.getTotalAmount() != null ? b.getTotalAmount() : 0.0,
                        b.getStatusStr()
                ));
                bw.newLine();
            }

            LOGGER.info("Bookings exported to: " + fileName);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "CSV export failed.", e);
        }
    }

    // ============================================================
    // WEEK 6: SERIALIZATION — ObjectOutputStream / ObjectInputStream
    // ============================================================

    /**
     * Serializes a list of Booking objects to a binary file.
     * Demonstrates Week 6: ObjectOutputStream and Serializable.
     *
     * @param bookings list of Booking objects to serialize
     */
    public static void serializeBookings(List<Booking> bookings) {
        try (FileOutputStream fos = new FileOutputStream(BOOKING_SERIAL_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(new ArrayList<>(bookings));  // Serialize the list
            LOGGER.info("Bookings serialized successfully to " + BOOKING_SERIAL_FILE);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Serialization failed.", e);
        }
    }

    /**
     * Deserializes booking objects from the binary file.
     * Demonstrates Week 6: ObjectInputStream and deserialization.
     *
     * @return list of deserialized Booking objects, or empty list if file absent
     */
    @SuppressWarnings("unchecked")
    public static List<Booking> deserializeBookings() {
        File file = new File(BOOKING_SERIAL_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof List) {
                LOGGER.info("Bookings deserialized successfully.");
                return (List<Booking>) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Deserialization failed.", e);
        }
        return new ArrayList<>();
    }

    /**
     * Serializes customer data to a binary file.
     */
    public static void serializeCustomers(List<Customer> customers) {
        try (FileOutputStream fos = new FileOutputStream(CUSTOMER_SERIAL_FILE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(new ArrayList<>(customers));
            LOGGER.info("Customers serialized successfully.");

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Customer serialization failed.", e);
        }
    }

    /**
     * Deserializes customer objects from the binary file.
     */
    @SuppressWarnings("unchecked")
    public static List<Customer> deserializeCustomers() {
        File file = new File(CUSTOMER_SERIAL_FILE);
        if (!file.exists()) return new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            Object obj = ois.readObject();
            if (obj instanceof List) {
                return (List<Customer>) obj;
            }

        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Customer deserialization failed.", e);
        }
        return new ArrayList<>();
    }

    // ============================================================
    // WEEK 6: RandomAccessFile — Direct record lookup
    // ============================================================

    /**
     * Writes a fixed-size room status record using RandomAccessFile.
     * Demonstrates Week 6: RandomAccessFile with seek() for direct access.
     *
     * Record format: roomNumber (10 chars) + availability (1 char) = 11 bytes per record.
     *
     * @param roomNumber   room number string (e.g., "201")
     * @param isAvailable  current availability
     * @param recordIndex  position in the file (0-based)
     */
    public static void writeRoomStatusRecord(String roomNumber, boolean isAvailable, int recordIndex) {
        String rafFile = BACKUP_DIR + "room_status.dat";
        int recordSize = 12;  // 10 chars for room number + 1 for status + 1 newline padding

        try (RandomAccessFile raf = new RandomAccessFile(rafFile, "rw")) {
            // Seek to the exact position of this record
            raf.seek((long) recordIndex * recordSize);

            // Write fixed-width room number (padded to 10 chars)
            String paddedRoom = String.format("%-10s", roomNumber);
            raf.writeBytes(paddedRoom);

            // Write availability as a single character
            raf.writeChar(isAvailable ? 'Y' : 'N');

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "RandomAccessFile write failed.", e);
        }
    }

    /**
     * Reads a specific room's status directly by record index.
     * Demonstrates Week 6: RandomAccessFile seek() for O(1) lookup.
     *
     * @param recordIndex  0-based index of the room record
     * @return             'Y' if available, 'N' if occupied, '?' on error
     */
    public static char readRoomStatus(int recordIndex) {
        String rafFile = BACKUP_DIR + "room_status.dat";
        int recordSize = 12;

        try (RandomAccessFile raf = new RandomAccessFile(rafFile, "r")) {
            raf.seek((long) recordIndex * recordSize + 10);  // Skip past room number
            return raf.readChar();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "RandomAccessFile read failed.", e);
            return '?';
        }
    }
}
