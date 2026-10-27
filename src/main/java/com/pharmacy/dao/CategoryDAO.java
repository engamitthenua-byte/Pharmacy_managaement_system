package com.pharmacy.dao;

import com.pharmacy.model.Category;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO — CRUD operations for the categories table.
 */
public class CategoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);

    /** Returns all categories with their medicine count. */
    public List<Category> findAll() {
        String sql = "SELECT c.id, c.name, c.description, c.created_at, c.updated_at, " +
                     "COUNT(m.id) AS medicine_count " +
                     "FROM categories c LEFT JOIN medicines m ON m.category_id = c.id " +
                     "GROUP BY c.id ORDER BY c.name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching all categories", e);
        }
        return list;
    }

    /** Searches categories by name (partial match). */
    public List<Category> search(String keyword) {
        String sql = "SELECT c.id, c.name, c.description, c.created_at, c.updated_at, " +
                     "COUNT(m.id) AS medicine_count " +
                     "FROM categories c LEFT JOIN medicines m ON m.category_id = c.id " +
                     "WHERE c.name LIKE ? GROUP BY c.id ORDER BY c.name";
        List<Category> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching categories with keyword: {}", keyword, e);
        }
        return list;
    }

    /** Finds a single category by ID. */
    public Category findById(int id) {
        String sql = "SELECT c.id, c.name, c.description, c.created_at, c.updated_at, " +
                     "COUNT(m.id) AS medicine_count " +
                     "FROM categories c LEFT JOIN medicines m ON m.category_id = c.id " +
                     "WHERE c.id = ? GROUP BY c.id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding category by id: {}", id, e);
        }
        return null;
    }

    /**
     * Inserts a new category.
     * @return the generated ID, or -1 on failure
     */
    public int insert(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName().trim());
            ps.setString(2, category.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        logger.info("Category inserted with id: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("Duplicate category name: {}", category.getName());
        } catch (SQLException e) {
            logger.error("Error inserting category: {}", category.getName(), e);
        }
        return -1;
    }

    /**
     * Updates an existing category.
     * @return true if updated, false on duplicate name or DB error
     */
    public boolean update(Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName().trim());
            ps.setString(2, category.getDescription());
            ps.setInt(3, category.getId());
            int rows = ps.executeUpdate();
            logger.info("Category {} updated, rows: {}", category.getId(), rows);
            return rows > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            logger.warn("Duplicate category name on update: {}", category.getName());
            return false;
        } catch (SQLException e) {
            logger.error("Error updating category id: {}", category.getId(), e);
            return false;
        }
    }

    /**
     * Deletes a category only if no medicines reference it.
     * @return "ok" on success, "has_medicines" if medicines exist, "error" on DB failure
     */
    public String delete(int id) {
        // Check if any medicines use this category
        String checkSql = "SELECT COUNT(*) FROM medicines WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
            checkPs.setInt(1, id);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    logger.warn("Cannot delete category id: {} — has associated medicines", id);
                    return "has_medicines";
                }
            }

            String deleteSql = "DELETE FROM categories WHERE id = ?";
            try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                delPs.setInt(1, id);
                delPs.executeUpdate();
                logger.info("Category {} deleted.", id);
                return "ok";
            }
        } catch (SQLException e) {
            logger.error("Error deleting category id: {}", id, e);
            return "error";
        }
    }

    /** Checks if a category name already exists (for validation). Excludes the given excludeId. */
    public boolean nameExists(String name, int excludeId) {
        String sql = "SELECT COUNT(*) FROM categories WHERE name = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking category name existence", e);
        }
        return false;
    }

    /** Returns total count of categories. */
    public int count() {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting categories", e);
        }
        return 0;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setDescription(rs.getString("description"));
        c.setMedicineCount(rs.getInt("medicine_count"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) c.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) c.setUpdatedAt(updated.toLocalDateTime());
        return c;
    }
}
