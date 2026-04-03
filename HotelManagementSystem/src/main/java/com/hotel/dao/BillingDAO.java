package com.hotel.dao;

import com.hotel.model.BillingItem;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BillingDAO — Handles À La Carte billing items and menu retrieval.
 *
 * @author Grand Vista HMS
 */
public class BillingDAO {

    private static final Logger LOGGER = Logger.getLogger(BillingDAO.class.getName());

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ---- MenuItem inner record for DAO use ----
    public record MenuItemRecord(int menuId, String itemName, String category,
                                 String description, double price) {}

    // ============================================================
    // MENU
    // ============================================================

    public List<MenuItemRecord> getAllMenuItems() {
        List<MenuItemRecord> items = new ArrayList<>();
        String sql = "SELECT * FROM alacarte_menu WHERE is_available = 1 ORDER BY category, item_name";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new MenuItemRecord(
                    rs.getInt("menu_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching menu items.", e);
        }
        return items;
    }

    public List<MenuItemRecord> getMenuByCategory(String category) {
        List<MenuItemRecord> items = new ArrayList<>();
        String sql = "SELECT * FROM alacarte_menu WHERE category = ? AND is_available = 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new MenuItemRecord(
                    rs.getInt("menu_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getString("description"),
                    rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching menu by category.", e);
        }
        return items;
    }

    // ============================================================
    // BILLING ITEMS
    // ============================================================

    public int saveBillingItem(BillingItem item) {
        String sql = "INSERT INTO billing_items (booking_id, item_name, category, quantity, " +
                     "unit_price, total_price) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getBookingId());
            ps.setString(2, item.getItemName());
            ps.setString(3, item.getCategoryStr());
            ps.setInt(4, item.getQuantity());
            ps.setDouble(5, item.getUnitPrice());
            ps.setDouble(6, item.getTotalPrice());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save billing item.", e);
        }
        return -1;
    }

    public List<BillingItem> findItemsByBookingId(int bookingId) {
        List<BillingItem> items = new ArrayList<>();
        String sql = "SELECT * FROM billing_items WHERE booking_id = ? ORDER BY ordered_at";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new BillingItem(
                    rs.getInt("item_id"),
                    rs.getInt("booking_id"),
                    rs.getString("item_name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price"),
                    rs.getDouble("total_price"),
                    rs.getString("ordered_at")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching billing items.", e);
        }
        return items;
    }

    public double getTotalAlaCarteByBooking(int bookingId) {
        String sql = "SELECT COALESCE(SUM(total_price), 0) FROM billing_items WHERE booking_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error totalling billing items.", e);
        }
        return 0.0;
    }
}
