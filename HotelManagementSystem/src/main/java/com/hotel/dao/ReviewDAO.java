package com.hotel.dao;

import com.hotel.model.Review;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ReviewDAO — Data access for guest reviews/feedback.
 *
 * @author Grand Vista HMS
 */
public class ReviewDAO {

    private static final Logger LOGGER = Logger.getLogger(ReviewDAO.class.getName());

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int save(Review review) {
        String sql = "INSERT INTO reviews (booking_id, customer_id, rating, title, comments, category) " +
                     "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getBookingId());
            ps.setInt(2, review.getCustomerId());
            ps.setInt(3, review.getRating());
            ps.setString(4, review.getTitle());
            ps.setString(5, review.getComments());
            ps.setString(6, review.getCategory() != null ? review.getCategory() : "GENERAL");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save review.", e);
        }
        return -1;
    }

    public List<Review> findAll() {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, c.full_name AS customer_name, b.booking_ref " +
                     "FROM reviews r " +
                     "JOIN customers c ON r.customer_id = c.customer_id " +
                     "JOIN bookings b ON r.booking_id = b.booking_id " +
                     "ORDER BY r.created_at DESC";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Review rev = new Review();
                rev.setReviewId(rs.getInt("review_id"));
                rev.setBookingId(rs.getInt("booking_id"));
                rev.setCustomerId(rs.getInt("customer_id"));
                rev.setCustomerName(rs.getString("customer_name"));
                rev.setBookingRef(rs.getString("booking_ref"));
                rev.setRating(rs.getInt("rating"));
                rev.setTitle(rs.getString("title"));
                rev.setComments(rs.getString("comments"));
                rev.setCategory(rs.getString("category"));
                rev.setCreatedAt(rs.getString("created_at"));
                list.add(rev);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching reviews.", e);
        }
        return list;
    }

    public double getAverageRating() {
        String sql = "SELECT AVG(rating) FROM reviews";
        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error computing average rating.", e);
        }
        return 0.0;
    }
}
