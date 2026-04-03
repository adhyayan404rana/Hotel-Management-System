package com.hotel.model;

/**
 * BillingItem — Represents a single line item on a hotel bill.
 *
 * Used in the À La Carte system and consolidated checkout bill.
 * Demonstrates Encapsulation (Week 1) and Wrapper Classes (Week 2).
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class BillingItem {

    // Item categories (mirrors DB ENUM-like values)
    public enum Category {
        FOOD, BEVERAGE, SPA, LAUNDRY, MISC, ROOM_CHARGE
    }

    private int itemId;
    private int bookingId;
    private String itemName;
    private Category category;
    private Integer quantity;        // Integer wrapper (Week 2: autoboxing)
    private Double unitPrice;        // Double wrapper (Week 2: autoboxing)
    private Double totalPrice;
    private String orderedAt;

    // ============================================================
    // CONSTRUCTORS
    // ============================================================

    public BillingItem() {}

    public BillingItem(int bookingId, String itemName, Category category,
                       int quantity, double unitPrice) {
        this.bookingId = bookingId;
        this.itemName = itemName;
        this.category = category;
        this.quantity = Integer.valueOf(quantity);    // Explicit autoboxing
        this.unitPrice = Double.valueOf(unitPrice);   // Explicit autoboxing
        this.totalPrice = unitPrice * quantity;       // Auto-unboxed for math
    }

    public BillingItem(int itemId, int bookingId, String itemName, String categoryStr,
                       int quantity, double unitPrice, double totalPrice, String orderedAt) {
        this.itemId = itemId;
        this.bookingId = bookingId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.orderedAt = orderedAt;

        try {
            this.category = Category.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.category = Category.MISC;
        }
    }

    // ---- Getters & Setters ----
    public int getItemId()                     { return itemId; }
    public void setItemId(int id)              { this.itemId = id; }

    public int getBookingId()                  { return bookingId; }
    public void setBookingId(int id)           { this.bookingId = id; }

    public String getItemName()                { return itemName; }
    public void setItemName(String n)          { this.itemName = n; }

    public Category getCategory()              { return category; }
    public String getCategoryStr()             { return category != null ? category.name() : "MISC"; }

    public Integer getQuantity()               { return quantity; }
    public void setQuantity(Integer q)         { this.quantity = q; recalcTotal(); }

    public Double getUnitPrice()               { return unitPrice; }
    public void setUnitPrice(Double p)         { this.unitPrice = p; recalcTotal(); }

    public Double getTotalPrice()              { return totalPrice; }
    public void setTotalPrice(Double t)        { this.totalPrice = t; }

    public String getOrderedAt()               { return orderedAt; }
    public void setOrderedAt(String t)         { this.orderedAt = t; }

    private void recalcTotal() {
        if (quantity != null && unitPrice != null) {
            this.totalPrice = unitPrice * quantity;  // Unboxing for arithmetic
        }
    }

    public String getFormattedTotal() {
        return totalPrice != null ? String.format("₹%,.2f", totalPrice) : "₹0.00";
    }

    @Override
    public String toString() {
        return itemName + " x" + quantity + " @ ₹" + unitPrice + " = ₹" + totalPrice;
    }
}


// ============================================================
// MenuItem — Menu item for the À La Carte panel
// ============================================================

/**
 * MenuItem — Represents an item on the hotel's food/service menu.
 * Separate from BillingItem to distinguish menu catalogue from ordered items.
 */
class MenuItem {

    private int menuId;
    private String itemName;
    private String category;
    private String description;
    private double price;
    private boolean available;

    public MenuItem() {}

    public MenuItem(int menuId, String itemName, String category,
                    String description, double price, boolean available) {
        this.menuId = menuId;
        this.itemName = itemName;
        this.category = category;
        this.description = description;
        this.price = price;
        this.available = available;
    }

    public int getMenuId()                     { return menuId; }
    public void setMenuId(int id)              { this.menuId = id; }
    public String getItemName()                { return itemName; }
    public void setItemName(String n)          { this.itemName = n; }
    public String getCategory()                { return category; }
    public void setCategory(String c)          { this.category = c; }
    public String getDescription()             { return description; }
    public void setDescription(String d)       { this.description = d; }
    public double getPrice()                   { return price; }
    public void setPrice(double p)             { this.price = p; }
    public boolean isAvailable()               { return available; }
    public void setAvailable(boolean a)        { this.available = a; }

    public String getFormattedPrice()          { return String.format("₹%.2f", price); }

    @Override
    public String toString() { return itemName + " — " + getFormattedPrice(); }
}
