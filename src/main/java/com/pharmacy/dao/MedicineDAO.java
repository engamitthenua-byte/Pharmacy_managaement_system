package com.pharmacy.dao;

import com.pharmacy.model.Medicine;
import com.pharmacy.model.MedicineBatch;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MedicineDAO — CRUD operations for medicines and medicine_batches tables.
 *
 * Design decisions:
 * - Medicines store general info; pricing & expiry live in medicine_batches.
 * - Total quantity = SUM of non-expired batch quantities.
 * - Low-stock is determined by: total_qty <= min_stock_level.
 */
public class MedicineDAO {

    private static final Logger logger = LoggerFactory.getLogger(MedicineDAO.class);

    // ─────────────────────────────────────────────────────────
    // MEDICINE MASTER CRUD
    // ─────────────────────────────────────────────────────────

    /**
     * Retrieves all medicines with category name, supplier name,
     * and total available (non-expired) quantity.
     */
    public List<Medicine> findAll() {
        String sql = buildMedicineSelectSql("ORDER BY m.name");
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMedicineRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching all medicines", e);
        }
        return list;
    }

    public List<Medicine> findAllPaginated(int offset, int limit) {
        String sql = buildMedicineSelectSql("ORDER BY m.name LIMIT ? OFFSET ?");
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMedicineRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching paginated medicines", e);
        }
        return list;
    }

    public List<Medicine> search(String keyword) {
        String sql = buildMedicineSelectSql("WHERE m.name LIKE ? ORDER BY m.name");
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMedicineRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching medicines: {}", keyword, e);
        }
        return list;
    }

    public List<Medicine> findByCategory(int categoryId) {
        String sql = buildMedicineSelectSql("WHERE m.category_id = ? ORDER BY m.name");
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMedicineRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding medicines by category: {}", categoryId, e);
        }
        return list;
    }

    public List<Medicine> findBySupplier(int supplierId) {
        String sql = buildMedicineSelectSql("WHERE m.supplier_id = ? ORDER BY m.name");
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMedicineRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding medicines by supplier: {}", supplierId, e);
        }
        return list;
    }

    /** Returns medicines where total available stock <= min_stock_level */
    public List<Medicine> findLowStock() {
        String sql =
            "SELECT m.id, m.name, c.name AS category_name, s.name AS supplier_name, " +
            "m.manufacturer, m.description, m.unit, m.min_stock_level, m.created_at, m.updated_at, " +
            "m.category_id, m.supplier_id, " +
            "COALESCE(SUM(CASE WHEN mb.expiry_date >= CURDATE() THEN mb.quantity ELSE 0 END), 0) AS total_qty " +
            "FROM medicines m " +
            "LEFT JOIN categories c ON c.id = m.category_id " +
            "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
            "LEFT JOIN medicine_batches mb ON mb.medicine_id = m.id " +
            "GROUP BY m.id " +
            "HAVING total_qty <= m.min_stock_level " +
            "ORDER BY total_qty ASC";
        List<Medicine> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapMedicineRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching low-stock medicines", e);
        }
        return list;
    }

    public Medicine findById(int id) {
        String sql = buildMedicineSelectSql("WHERE m.id = ?");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMedicineRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding medicine by id: {}", id, e);
        }
        return null;
    }

    public int insert(Medicine m) {
        String sql = "INSERT INTO medicines (name, category_id, supplier_id, manufacturer, description, unit, min_stock_level) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName().trim());
            ps.setInt(2, m.getCategoryId());
            ps.setInt(3, m.getSupplierId());
            ps.setString(4, m.getManufacturer());
            ps.setString(5, m.getDescription());
            ps.setString(6, m.getUnit() != null ? m.getUnit() : "Tablet");
            ps.setInt(7, m.getMinStockLevel());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        logger.info("Medicine inserted with id: {}", id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting medicine: {}", m.getName(), e);
        }
        return -1;
    }

    public boolean update(Medicine m) {
        String sql = "UPDATE medicines SET name=?, category_id=?, supplier_id=?, manufacturer=?, " +
                     "description=?, unit=?, min_stock_level=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getName().trim());
            ps.setInt(2, m.getCategoryId());
            ps.setInt(3, m.getSupplierId());
            ps.setString(4, m.getManufacturer());
            ps.setString(5, m.getDescription());
            ps.setString(6, m.getUnit());
            ps.setInt(7, m.getMinStockLevel());
            ps.setInt(8, m.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating medicine id: {}", m.getId(), e);
            return false;
        }
    }

    /**
     * @return "ok", "has_sales", "has_purchases", or "error"
     */
    public String delete(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            // Check for sale_items
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM sale_items WHERE medicine_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "has_sales";
                }
            }
            // Check for purchase_items
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT COUNT(*) FROM purchase_items WHERE medicine_id = ?")) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return "has_purchases";
                }
            }
            // Delete batches first (no foreign key cascade on medicine delete)
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM medicine_batches WHERE medicine_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM medicines WHERE id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
                logger.info("Medicine {} deleted.", id);
                return "ok";
            }
        } catch (SQLException e) {
            logger.error("Error deleting medicine id: {}", id, e);
            return "error";
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM medicines";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting medicines", e);
        }
        return 0;
    }

    public int countLowStock() {
        String sql =
            "SELECT COUNT(*) FROM (" +
            "  SELECT m.id FROM medicines m " +
            "  LEFT JOIN medicine_batches mb ON mb.medicine_id = m.id AND mb.expiry_date >= CURDATE() " +
            "  GROUP BY m.id, m.min_stock_level " +
            "  HAVING COALESCE(SUM(mb.quantity), 0) <= m.min_stock_level" +
            ") AS low";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting low-stock medicines", e);
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
    // BATCH OPERATIONS
    // ─────────────────────────────────────────────────────────

    public List<MedicineBatch> findBatchesByMedicine(int medicineId) {
        String sql = "SELECT mb.*, m.name AS medicine_name, m.min_stock_level, " +
                     "c.name AS category_name, s.name AS supplier_name " +
                     "FROM medicine_batches mb " +
                     "JOIN medicines m ON m.id = mb.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "WHERE mb.medicine_id = ? ORDER BY mb.expiry_date ASC";
        List<MedicineBatch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBatchRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching batches for medicine: {}", medicineId, e);
        }
        return list;
    }

    /** Returns only non-expired batches for a medicine, ordered by expiry (FEFO). */
    public List<MedicineBatch> findAvailableBatches(int medicineId) {
        String sql = "SELECT mb.*, m.name AS medicine_name, m.min_stock_level, " +
                     "c.name AS category_name, s.name AS supplier_name " +
                     "FROM medicine_batches mb " +
                     "JOIN medicines m ON m.id = mb.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "WHERE mb.medicine_id = ? AND mb.expiry_date >= CURDATE() AND mb.quantity > 0 " +
                     "ORDER BY mb.expiry_date ASC";
        List<MedicineBatch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBatchRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching available batches for medicine: {}", medicineId, e);
        }
        return list;
    }

    public List<MedicineBatch> findExpiredBatches() {
        String sql = "SELECT mb.*, m.name AS medicine_name, m.min_stock_level, " +
                     "c.name AS category_name, s.name AS supplier_name " +
                     "FROM medicine_batches mb " +
                     "JOIN medicines m ON m.id = mb.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "WHERE mb.expiry_date < CURDATE() AND mb.quantity > 0 " +
                     "ORDER BY mb.expiry_date ASC";
        List<MedicineBatch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBatchRow(rs));
        } catch (SQLException e) {
            logger.error("Error fetching expired batches", e);
        }
        return list;
    }

    public List<MedicineBatch> findNearExpiryBatches(int warningDays) {
        String sql = "SELECT mb.*, m.name AS medicine_name, m.min_stock_level, " +
                     "c.name AS category_name, s.name AS supplier_name " +
                     "FROM medicine_batches mb " +
                     "JOIN medicines m ON m.id = mb.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "WHERE mb.expiry_date >= CURDATE() AND mb.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "AND mb.quantity > 0 ORDER BY mb.expiry_date ASC";
        List<MedicineBatch> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, warningDays);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapBatchRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching near-expiry batches", e);
        }
        return list;
    }

    /** Finds a batch by its ID. */
    public MedicineBatch findBatchById(int batchId) {
        String sql = "SELECT mb.*, m.name AS medicine_name, m.min_stock_level, " +
                     "c.name AS category_name, s.name AS supplier_name " +
                     "FROM medicine_batches mb " +
                     "JOIN medicines m ON m.id = mb.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "WHERE mb.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapBatchRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding batch by id: {}", batchId, e);
        }
        return null;
    }

    /**
     * Inserts a new batch. Returns generated ID or -1.
     * This method is called within a transaction from PurchaseDAO.
     */
    public int insertBatch(MedicineBatch batch, Connection conn) throws SQLException {
        String sql = "INSERT INTO medicine_batches " +
                     "(medicine_id, batch_number, purchase_price, selling_price, quantity, manufacturing_date, expiry_date) " +
                     "VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, batch.getMedicineId());
            ps.setString(2, batch.getBatchNumber());
            ps.setBigDecimal(3, batch.getPurchasePrice());
            ps.setBigDecimal(4, batch.getSellingPrice());
            ps.setInt(5, batch.getQuantity());
            if (batch.getManufacturingDate() != null) {
                ps.setDate(6, Date.valueOf(batch.getManufacturingDate()));
            } else {
                ps.setNull(6, Types.DATE);
            }
            ps.setDate(7, Date.valueOf(batch.getExpiryDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Increases batch quantity by given amount.
     * Called within a transaction from PurchaseDAO.
     */
    public void increaseBatchQuantity(int batchId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE medicine_batches SET quantity = quantity + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, batchId);
            ps.executeUpdate();
        }
    }

    /**
     * Decreases batch quantity. Prevents negative stock.
     * Called within a transaction from SalesDAO.
     */
    public void decreaseBatchQuantity(int batchId, int quantity, Connection conn) throws SQLException {
        String sql = "UPDATE medicine_batches SET quantity = quantity - ? " +
                     "WHERE id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, batchId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Insufficient stock for batch id: " + batchId +
                                       ". Requested: " + quantity);
            }
        }
    }

    /** Checks if a batch number already exists for a medicine (excluding a given batch ID). */
    public boolean batchNumberExists(int medicineId, String batchNumber, int excludeBatchId) {
        String sql = "SELECT COUNT(*) FROM medicine_batches WHERE medicine_id=? AND batch_number=? AND id!=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, batchNumber);
            ps.setInt(3, excludeBatchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking batch number existence", e);
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────
    // Helper SQL builder
    // ─────────────────────────────────────────────────────────

    private String buildMedicineSelectSql(String conditionAndOrder) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT m.id, m.name, m.category_id, m.supplier_id, m.manufacturer, ")
          .append("m.description, m.unit, m.min_stock_level, m.created_at, m.updated_at, ")
          .append("c.name AS category_name, s.name AS supplier_name, ")
          .append("COALESCE(SUM(CASE WHEN mb.expiry_date >= CURDATE() THEN mb.quantity ELSE 0 END), 0) AS total_qty ")
          .append("FROM medicines m ")
          .append("LEFT JOIN categories c ON c.id = m.category_id ")
          .append("LEFT JOIN suppliers s ON s.id = m.supplier_id ")
          .append("LEFT JOIN medicine_batches mb ON mb.medicine_id = m.id ");

        if (conditionAndOrder == null || conditionAndOrder.trim().isEmpty()) {
            sb.append("GROUP BY m.id ORDER BY m.name");
            return sb.toString();
        }

        String clause = conditionAndOrder.trim();
        String upper = clause.toUpperCase();

        if (upper.startsWith("WHERE")) {
            int orderIndex = upper.indexOf("ORDER BY");
            if (orderIndex != -1) {
                String wherePart = clause.substring(0, orderIndex).trim();
                String orderPart = clause.substring(orderIndex).trim();
                sb.append(" ").append(wherePart).append(" GROUP BY m.id ").append(orderPart);
            } else {
                sb.append(" ").append(clause).append(" GROUP BY m.id");
            }
        } else if (upper.startsWith("ORDER BY")) {
            sb.append(" GROUP BY m.id ").append(clause);
        } else {
            sb.append(" ").append(clause).append(" GROUP BY m.id");
        }

        return sb.toString();
    }

    private Medicine mapMedicineRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setCategoryId(rs.getInt("category_id"));
        m.setSupplierId(rs.getInt("supplier_id"));
        m.setManufacturer(rs.getString("manufacturer"));
        m.setDescription(rs.getString("description"));
        m.setUnit(rs.getString("unit"));
        m.setMinStockLevel(rs.getInt("min_stock_level"));
        m.setCategoryName(rs.getString("category_name"));
        m.setSupplierName(rs.getString("supplier_name"));
        m.setTotalQuantity(rs.getInt("total_qty"));
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) m.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) m.setUpdatedAt(updated.toLocalDateTime());
        return m;
    }

    private MedicineBatch mapBatchRow(ResultSet rs) throws SQLException {
        MedicineBatch b = new MedicineBatch();
        b.setId(rs.getInt("id"));
        b.setMedicineId(rs.getInt("medicine_id"));
        b.setBatchNumber(rs.getString("batch_number"));
        b.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        b.setSellingPrice(rs.getBigDecimal("selling_price"));
        b.setQuantity(rs.getInt("quantity"));
        Date mfg = rs.getDate("manufacturing_date");
        if (mfg != null) b.setManufacturingDate(mfg.toLocalDate());
        Date exp = rs.getDate("expiry_date");
        if (exp != null) b.setExpiryDate(exp.toLocalDate()); // triggers expired/nearExpiry calculation
        b.setMedicineName(rs.getString("medicine_name"));
        b.setMinStockLevel(rs.getInt("min_stock_level"));
        try { b.setCategoryName(rs.getString("category_name")); } catch (Exception ignored) {}
        try { b.setSupplierName(rs.getString("supplier_name")); } catch (Exception ignored) {}
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) b.setCreatedAt(created.toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) b.setUpdatedAt(updated.toLocalDateTime());
        return b;
    }
}
