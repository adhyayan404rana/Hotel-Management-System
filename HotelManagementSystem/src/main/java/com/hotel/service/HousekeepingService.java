package com.hotel.service;

import com.hotel.util.FileBackupUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * HousekeepingService — Multithreaded room cleaning simulation.
 *
 * Demonstrates Week 3 concepts:
 *   - Thread and Runnable interface usage
 *   - Thread.sleep() for simulating work duration
 *   - Thread.yield() to give other threads CPU time
 *   - Thread.join() for waiting on completion
 *
 * Demonstrates Week 4 concepts:
 *   - Synchronization (synchronized block) to prevent race conditions
 *   - wait() and notify() for inter-thread communication
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class HousekeepingService {

    private static final Logger LOGGER = Logger.getLogger(HousekeepingService.class.getName());

    // Shared status property — observed by JavaFX UI
    private final StringProperty statusLog = new SimpleStringProperty("");

    // Tracks rooms currently being cleaned (synchronized access)
    private final List<String> roomsBeingCleaned = new ArrayList<>();

    // Monitor object for wait/notify (Week 4)
    private final Object cleaningMonitor = new Object();

    // Flag to stop the service gracefully
    private final AtomicBoolean serviceStopped = new AtomicBoolean(false);

    // ============================================================
    // WEEK 3: Thread subclass — Room Cleaning Thread
    // ============================================================

    /**
     * RoomCleaningThread — extends Thread (Week 3 concept).
     * Simulates the physical cleaning of a single hotel room.
     */
    private class RoomCleaningThread extends Thread {

        private final String roomNumber;
        private final Runnable onComplete;

        public RoomCleaningThread(String roomNumber, Runnable onComplete) {
            super("Cleaning-Thread-Room-" + roomNumber);  // Named thread
            this.roomNumber = roomNumber;
            this.onComplete = onComplete;
        }

        @Override
        public void run() {
            try {
                appendLog("🧹 Housekeeping started for Room " + roomNumber + "...");

                // Week 4: Synchronized block to prevent two threads cleaning same room
                synchronized (cleaningMonitor) {
                    if (roomsBeingCleaned.contains(roomNumber)) {
                        appendLog("⚠️ Room " + roomNumber + " is already being cleaned.");
                        return;
                    }
                    roomsBeingCleaned.add(roomNumber);
                }

                // Simulate cleaning steps with sleep() — Week 3 concept
                simulateCleaningSteps(roomNumber);

                // Week 4: Synchronized removal from tracking list + notify
                synchronized (cleaningMonitor) {
                    roomsBeingCleaned.remove(roomNumber);
                    cleaningMonitor.notifyAll();  // Week 4: notify waiting threads
                }

                appendLog("✅ Room " + roomNumber + " is now sparkling clean!");
                FileBackupUtil.writeAuditLog("HOUSEKEEPING", "Room " + roomNumber + " cleaned successfully.");

                // Callback on JavaFX thread
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                appendLog("❌ Cleaning interrupted for Room " + roomNumber);
            }
        }

        private void simulateCleaningSteps(String room) throws InterruptedException {
            String[] steps = {
                "Vacuuming floors",
                "Changing bed linen",
                "Sanitising bathroom",
                "Restocking mini-bar",
                "Final inspection"
            };

            for (String step : steps) {
                if (serviceStopped.get()) break;
                appendLog("   [Room " + room + "] " + step + "...");
                Thread.sleep(600);         // Week 3: sleep() — simulates step duration
                Thread.yield();            // Week 3: yield() — cooperate with other threads
            }
        }
    }

    // ============================================================
    // WEEK 3: Runnable interface — Booking Processing Background Task
    // ============================================================

    /**
     * BookingProcessorRunnable — implements Runnable (Week 3 concept).
     * Simulates background processing of a new booking (e.g., confirmation email, etc.)
     */
    public static class BookingProcessorRunnable implements Runnable {

        private final String bookingRef;
        private final String guestName;
        private final Runnable onDone;

        public BookingProcessorRunnable(String bookingRef, String guestName, Runnable onDone) {
            this.bookingRef = bookingRef;
            this.guestName = guestName;
            this.onDone = onDone;
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Processing booking " + bookingRef + " for " + guestName);

                // Simulate async processing steps
                Thread.sleep(300);   // Simulate DB write confirmation
                Thread.sleep(200);   // Simulate email dispatch
                Thread.sleep(100);   // Simulate welcome pack generation

                FileBackupUtil.writeAuditLog("BOOKING_PROCESSED",
                        "Ref: " + bookingRef + " | Guest: " + guestName);

                if (onDone != null) {
                    Platform.runLater(onDone);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.warning("Booking processing interrupted for " + bookingRef);
            }
        }
    }

    // ============================================================
    // PUBLIC API
    // ============================================================

    /**
     * Starts cleaning a single room in a dedicated thread.
     *
     * @param roomNumber room to clean
     * @param onComplete callback invoked on completion (on JavaFX thread)
     */
    public void cleanRoom(String roomNumber, Runnable onComplete) {
        RoomCleaningThread cleaningThread = new RoomCleaningThread(roomNumber, onComplete);
        cleaningThread.setDaemon(true);   // Daemon thread — won't block JVM shutdown
        cleaningThread.start();           // Week 3: start() launches the thread
    }

    /**
     * Cleans multiple rooms concurrently — one thread per room.
     * Demonstrates parallel Thread usage and join() (Week 3).
     *
     * @param roomNumbers list of room numbers to clean
     * @param onAllDone   callback invoked when ALL rooms are cleaned
     */
    public void cleanRoomsBatch(List<String> roomNumbers, Runnable onAllDone) {
        if (roomNumbers == null || roomNumbers.isEmpty()) {
            if (onAllDone != null) Platform.runLater(onAllDone);
            return;
        }

        // Launch all cleaning threads
        List<RoomCleaningThread> threads = new ArrayList<>();
        for (String room : roomNumbers) {
            RoomCleaningThread t = new RoomCleaningThread(room, null);
            t.setDaemon(true);
            threads.add(t);
            t.start();   // Each room cleaned in its own thread (Week 3)
        }

        // Monitor thread: waits for all cleaning threads via join() (Week 3)
        Thread monitor = new Thread(() -> {
            for (RoomCleaningThread t : threads) {
                try {
                    t.join();   // Week 3: join() — wait for this thread to finish
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            appendLog("🏨 All " + roomNumbers.size() + " rooms have been cleaned.");
            if (onAllDone != null) Platform.runLater(onAllDone);
        }, "Batch-Cleaning-Monitor");

        monitor.setDaemon(true);
        monitor.start();
    }

    /**
     * Processes a booking asynchronously using a Runnable + Thread.
     * Demonstrates Week 3: Runnable interface usage.
     *
     * @param bookingRef booking reference number
     * @param guestName  guest's name
     * @param onDone     callback when processing is complete
     */
    public void processBookingAsync(String bookingRef, String guestName, Runnable onDone) {
        Runnable processor = new BookingProcessorRunnable(bookingRef, guestName, onDone);
        Thread processingThread = new Thread(processor, "Booking-Processor");
        processingThread.setDaemon(true);
        processingThread.start();
    }

    /**
     * Week 4: Demonstrates wait() — a caller thread waits until a specific
     * room is no longer being cleaned.
     *
     * @param roomNumber the room to wait for
     */
    public void waitForRoomAvailability(String roomNumber) {
        synchronized (cleaningMonitor) {
            while (roomsBeingCleaned.contains(roomNumber)) {
                try {
                    cleaningMonitor.wait(1000);  // Week 4: wait() — release lock and wait
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Stops all housekeeping activity gracefully.
     */
    public void shutdown() {
        serviceStopped.set(true);
        synchronized (cleaningMonitor) {
            cleaningMonitor.notifyAll();  // Wake any waiting threads
        }
    }

    /** Appends a message to the status log (thread-safe via Platform.runLater). */
    private void appendLog(String message) {
        Platform.runLater(() -> {
            String current = statusLog.get();
            statusLog.set(current + message + "\n");
        });
    }

    public StringProperty statusLogProperty() { return statusLog; }
    public void clearLog() { Platform.runLater(() -> statusLog.set("")); }

    public boolean isRoomBeingCleaned(String roomNumber) {
        synchronized (cleaningMonitor) {
            return roomsBeingCleaned.contains(roomNumber);
        }
    }
}
