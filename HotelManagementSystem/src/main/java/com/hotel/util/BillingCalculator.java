package com.hotel.util;

import com.hotel.model.BillingItem;
import com.hotel.model.Room;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * BillingCalculator — Generic utility for hotel billing operations.
 *
 * Demonstrates Week 7 concepts:
 *   - Generic class: BillingCalculator<T>
 *   - Generic method: sum()
 *   - Bounded type parameters: <T extends Number>
 *   - Wildcard usage
 *
 * @param <T> The type of item this calculator handles (must be a Number subtype for amounts)
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class BillingCalculator<T extends Number> {

    // ============================================================
    // GENERIC METHOD — Week 7
    // ============================================================

    /**
     * Generic method that sums a list of any Number subtype.
     * Bounded type parameter <N extends Number> ensures only numeric types.
     *
     * @param <N>    any Number subtype (Double, Integer, Float, etc.)
     * @param values list of numeric values to sum
     * @return       sum as a double
     */
    public static <N extends Number> double sum(List<N> values) {
        if (values == null || values.isEmpty()) return 0.0;

        double total = 0.0;
        // Using Iterator explicitly (Week 8: Iterator concept)
        Iterator<N> iterator = values.iterator();
        while (iterator.hasNext()) {
            N value = iterator.next();
            total += value.doubleValue();    // Polymorphic call via Number interface
        }
        return total;
    }

    /**
     * Generic method to calculate total from a list of BillingItems.
     * Uses Iterator (Week 8) to traverse the list.
     *
     * @param items list of billing items
     * @return      total amount
     */
    public static double calculateTotal(List<BillingItem> items) {
        if (items == null) return 0.0;

        double total = 0.0;
        Iterator<BillingItem> it = items.iterator();
        while (it.hasNext()) {
            BillingItem item = it.next();
            if (item.getTotalPrice() != null) {
                total += item.getTotalPrice();   // Unboxing Double to double
            }
        }
        return total;
    }

    /**
     * Calculates a complete hotel bill breakdown.
     *
     * @param room          the room that was occupied
     * @param nights        number of nights stayed (Integer wrapper — autoboxing)
     * @param billingItems  list of additional À La Carte items
     * @return              BillSummary with breakdown
     */
    public static BillSummary computeFinalBill(Room room, Integer nights,
                                               List<BillingItem> billingItems) {
        // Unboxing: Integer nights → int for arithmetic
        double roomCharge = room.getPricePerNight() * nights;
        double serviceCharge = roomCharge * (room.getServiceChargePercent() / 100.0);

        // Sum of all À La Carte charges using our generic sum() method
        List<Double> itemAmounts = new ArrayList<>();
        if (billingItems != null) {
            for (BillingItem item : billingItems) {
                if (item.getTotalPrice() != null) {
                    itemAmounts.add(item.getTotalPrice());
                }
            }
        }
        double aLaCarteTotal = sum(itemAmounts);   // Generic method call

        double gstRate = 0.12;   // 12% GST
        double subtotal = roomCharge + serviceCharge + aLaCarteTotal;
        double gst = subtotal * gstRate;
        double grandTotal = subtotal + gst;

        return new BillSummary(roomCharge, serviceCharge, aLaCarteTotal, gst, grandTotal, nights);
    }

    // ============================================================
    // INNER CLASS: BillSummary (Value Object)
    // ============================================================

    /**
     * BillSummary — Immutable data container for a complete bill breakdown.
     * Returned by computeFinalBill() for display in the checkout UI.
     */
    public static class BillSummary {

        public final double roomCharge;
        public final double serviceCharge;
        public final double aLaCarteTotal;
        public final double gst;
        public final double grandTotal;
        public final int nights;

        public BillSummary(double roomCharge, double serviceCharge,
                           double aLaCarteTotal, double gst,
                           double grandTotal, int nights) {
            this.roomCharge = roomCharge;
            this.serviceCharge = serviceCharge;
            this.aLaCarteTotal = aLaCarteTotal;
            this.gst = gst;
            this.grandTotal = grandTotal;
            this.nights = nights;
        }

        /** Formats the complete bill as a printable string. */
        public String toReceiptString(String bookingRef, String guestName, String roomNumber) {
            return String.format(
                "═══════════════════════════════════════\n" +
                "       GRAND VISTA HOTEL — RECEIPT     \n" +
                "═══════════════════════════════════════\n" +
                "Booking Ref : %s\n" +
                "Guest       : %s\n" +
                "Room        : %s\n" +
                "Nights      : %d\n" +
                "───────────────────────────────────────\n" +
                "Room Charges : ₹%,.2f\n" +
                "Service Chrg : ₹%,.2f\n" +
                "À La Carte   : ₹%,.2f\n" +
                "GST (12%%)    : ₹%,.2f\n" +
                "───────────────────────────────────────\n" +
                "GRAND TOTAL  : ₹%,.2f\n" +
                "═══════════════════════════════════════\n" +
                "     Thank you for choosing Grand Vista!\n" +
                "═══════════════════════════════════════",
                bookingRef, guestName, roomNumber, nights,
                roomCharge, serviceCharge, aLaCarteTotal, gst, grandTotal
            );
        }

        public String getFormattedTotal() {
            return String.format("₹%,.2f", grandTotal);
        }
    }
}
