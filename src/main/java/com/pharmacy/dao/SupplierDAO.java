package com.pharmacy.dao;

import com.pharmacy.model.Supplier;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SupplierDAO — CRUD operations for the suppliers table.
 */
public class SupplierDAO {

    private static final Logger logger = LoggerFactory.getLogger(SupplierDAO.class);

    public List<Supplier> findAll() {
        String sql = "SELECT s.id, s.name, s.contact_person, s.phone, s.email, s.address, " +
                     "s.created_at, s.updated_at, COUNT(p.id) AS purchase_count " +
                     "FROM suppliers s LEFT JOIN purchases p ON p.supplier_id = s.id " +
                     "GROUP BY s.id ORDER BY s.name";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching all suppliers", e);
        }
        return list;
    }

    public List<Supplier> search(String keyword) {
        String sql = "SELECT s.id, s.name, s.contact_person, s.phone, s.email, s.address, " +
                     "s.created_at, s.updated_at, COUNT(p.id) AS purchase_count " +
                     "FROM suppliers s LEFT JOIN purchases p ON p.supplier_id = s.id " +
                     "WHERE s.name LIKE ? OR s.contact_person LIKE ? " +
                     "GROUP BY s.id ORDER BY s.name";
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching suppliers: {}", keyword, e);
        }
        return list;
    }

    public Supplier findById(int id) {
        String sql = "SELECT s.id, s.name, s.contact_person, s.phone, s.email, s.address, " +
                     "s.created_at, s.updated_at, COUNT(p.id) AS purchase_count " +
                     "FROM suppliers s LEFT JOIN purchases p ON p.supplier_id = s.id " +
                     "WHERE s.id = ? GROUP BY s.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding supplier by id: {}", id, e);
        }
        return null;
    }

    public int insert(Supplier s) {
        String sql = "INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName().trim());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        logger.info("Supplier inserted with id: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting supplier: {}", s.getName(), e);
        }
        return -1;
    }

    public boolean update(Supplier s) {
        String sql = "UPDATE suppliers SET name=?, contact_person=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName().trim());
            ps.setString(2, s.getContactPerson());
            ps.setString(3, s.getPhone());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getAddress());
            ps.setInt(6, s.getId());
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating supplier id: {}", s.getId(), e);
            return false;
        }
    }

    /**
     * Deletes a supplier.
     * @return "ok", "has_purchases", or "error"
     */
    public String delete(int id) {
        String checkSql = "SELECT COUNT(*) FROM purchases WHERE supplier_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return "has_purchases";
            }
            // Also check medicines
            String checkMed = "SELECT COUNT(*) FROM medicines WHERE supplier_id = ?";
            try (PreparedStatement checkMedPs = conn.prepareStatement(checkMed)) {
                checkMedPs.setInt(1, id);
                try (ResultSet rs = checkMedPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "has_medicines";
                }
            }
            String deleteSql = "DELETE FROM suppliers WHERE id = ?";
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setInt(1, id);
                delPs.executeUpdate();
                return "ok";
            }
        } catch (SQLException e) {
            logger.error("Error deleting supplier id: {}", id, e);
            return "error";
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM suppliers";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting suppliers", e);
        }
        return 0;
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setName(rs.getString("name"));
        s.setContactPerson(rs.getString("contact_person"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setPurchaseCount(rs.getInt("purchase_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());
        return s;
    }
}
