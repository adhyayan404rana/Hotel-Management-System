package com.hotel.dao;

import com.hotel.model.Customer;
import com.hotel.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CustomerDAO — DAO for Customer CRUD operations.
 *
 * Follows the DAO pattern: isolates all DB logic from the service layer.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class CustomerDAO {

    private static final Logger LOGGER = Logger.getLogger(CustomerDAO.class.getName());

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public int save(Customer customer) {
        String sql = "INSERT INTO customers (full_name, email, phone, id_proof_type, " +
                     "id_proof_number, address, nationality) VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getIdProofType());
            ps.setString(5, customer.getIdProofNumber());
            ps.setString(6, customer.getAddress());
            ps.setString(7, customer.getNationality());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save customer.", e);
        }
        return -1;
    }

    public Optional<Customer> findById(int customerId) {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error finding customer by id.", e);
        }
        return Optional.empty();
    }

    public List<Customer> findAll() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY full_name";

        try (Statement stmt = getConn().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) customers.add(mapRow(rs));

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching all customers.", e);
        }
        return customers;
    }

    public List<Customer> searchByName(String query) {
        List<Customer> results = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE LOWER(full_name) LIKE ? OR phone LIKE ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            String pattern = "%" + query.toLowerCase() + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Search error.", e);
        }
        return results;
    }

    public boolean update(Customer customer) {
        String sql = "UPDATE customers SET full_name=?, email=?, phone=?, id_proof_type=?, " +
                     "id_proof_number=?, address=?, nationality=? WHERE customer_id=?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getIdProofType());
            ps.setString(5, customer.getIdProofNumber());
            ps.setString(6, customer.getAddress());
            ps.setString(7, customer.getNationality());
            ps.setInt(8, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update customer.", e);
            return false;
        }
    }

    public boolean delete(int customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";

        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to delete customer.", e);
            return false;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("id_proof_type"),
            rs.getString("id_proof_number"),
            rs.getString("address"),
            rs.getString("nationality")
        );
    }
}
