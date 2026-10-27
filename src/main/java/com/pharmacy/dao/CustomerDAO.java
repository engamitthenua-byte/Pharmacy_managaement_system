package com.pharmacy.dao;

import com.pharmacy.model.Customer;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CustomerDAO — CRUD operations for the customers table.
 */
public class CustomerDAO {

    private static final Logger logger = LoggerFactory.getLogger(CustomerDAO.class);

    public List<Customer> findAll() {
        String sql = "SELECT c.id, c.name, c.phone, c.email, c.address, " +
                     "c.created_at, c.updated_at, COUNT(s.id) AS sales_count " +
                     "FROM customers c LEFT JOIN sales s ON s.customer_id = c.id " +
                     "GROUP BY c.id ORDER BY c.name";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching all customers", e);
        }
        return list;
    }

    public List<Customer> search(String keyword) {
        String sql = "SELECT c.id, c.name, c.phone, c.email, c.address, " +
                     "c.created_at, c.updated_at, COUNT(s.id) AS sales_count " +
                     "FROM customers c LEFT JOIN sales s ON s.customer_id = c.id " +
                     "WHERE c.name LIKE ? OR c.phone LIKE ? " +
                     "GROUP BY c.id ORDER BY c.name";
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching customers: {}", keyword, e);
        }
        return list;
    }

    public Customer findById(int id) {
        String sql = "SELECT c.id, c.name, c.phone, c.email, c.address, " +
                     "c.created_at, c.updated_at, COUNT(s.id) AS sales_count " +
                     "FROM customers c LEFT JOIN sales s ON s.customer_id = c.id " +
                     "WHERE c.id = ? GROUP BY c.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding customer by id: {}", id, e);
        }
        return null;
    }

    public int insert(Customer c) {
        String sql = "INSERT INTO customers (name, phone, email, address) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        logger.info("Customer inserted with id: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting customer: {}", c.getName(), e);
        }
        return -1;
    }

    public boolean update(Customer c) {
        String sql = "UPDATE customers SET name=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getName().trim());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setInt(5, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating customer id: {}", c.getId(), e);
            return false;
        }
    }

    /**
     * @return "ok", "has_sales", or "error"
     */
    public String delete(int id) {
        String checkSql = "SELECT COUNT(*) FROM sales WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return "has_sales";
            }
            String deleteSql = "DELETE FROM customers WHERE id = ?";
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setInt(1, id);
                delPs.executeUpdate();
                return "ok";
            }
        } catch (SQLException e) {
            logger.error("Error deleting customer id: {}", id, e);
            return "error";
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting customers", e);
        }
        return 0;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getString("address"));
        c.setSalesCount(rs.getInt("sales_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());
        return c;
    }
}
