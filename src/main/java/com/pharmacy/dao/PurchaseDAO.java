package com.pharmacy.dao;

import com.pharmacy.model.*;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseDAO — handles purchase invoice creation and retrieval.
 *
 * The savePurchase() method runs everything in a single database transaction:
 *  1. Insert purchase header
 *  2. For each item:
 *     a. Insert/update medicine_batch
 *     b. Insert purchase_item row
 *     c. Record inventory_transaction
 *  3. Commit — or rollback on any failure
 */
public class PurchaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseDAO.class);

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Saves a complete purchase (header + items + stock updates) atomically.
     *
     * @return generated purchase ID on success, -1 on failure/rollback
     */
    public int savePurchase(Purchase purchase) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // Start transaction

            // ── Step 1: Insert purchase header ──────────────────────────────
            String insertPurchase =
                "INSERT INTO purchases (invoice_number, supplier_id, total_amount, notes, purchase_date) " +
                "VALUES (?, ?, ?, ?, ?)";
            int purchaseId;
            try (PreparedStatement ps = conn.prepareStatement(insertPurchase, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, purchase.getInvoiceNumber());
                ps.setInt(2, purchase.getSupplierId());
                ps.setBigDecimal(3, purchase.getTotalAmount());
                ps.setString(4, purchase.getNotes());
                ps.setDate(5, Date.valueOf(purchase.getPurchaseDate()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Failed to get purchase ID");
                    purchaseId = keys.getInt(1);
                }
            }

            // ── Step 2: Process each line item ──────────────────────────────
            for (PurchaseItem item : purchase.getItems()) {

                // 2a. Check if this batch number already exists for the medicine
                MedicineBatch existingBatch = findBatchByNumber(
                    item.getMedicineId(), item.getBatchNumber(), conn);

                int batchId;
                if (existingBatch != null) {
                    // Batch already exists — increase its quantity
                    batchId = existingBatch.getId();
                    medicineDAO.increaseBatchQuantity(batchId, item.getQuantity(), conn);
                } else {
                    // Create a new batch record
                    MedicineBatch newBatch = new MedicineBatch();
                    newBatch.setMedicineId(item.getMedicineId());
                    newBatch.setBatchNumber(item.getBatchNumber());
                    newBatch.setPurchasePrice(item.getPurchasePrice());
                    newBatch.setSellingPrice(item.getSellingPrice());
                    newBatch.setQuantity(item.getQuantity());
                    newBatch.setManufacturingDate(item.getManufacturingDate());
                    newBatch.setExpiryDate(item.getExpiryDate());
                    batchId = medicineDAO.insertBatch(newBatch, conn);
                    if (batchId == -1) throw new SQLException("Failed to insert batch for medicine id: " + item.getMedicineId());
                }
                item.setBatchId(batchId);

                // 2b. Insert purchase_item row
                String insertItem =
                    "INSERT INTO purchase_items (purchase_id, medicine_id, batch_id, quantity, purchase_price, total_price) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setInt(1, purchaseId);
                    ps.setInt(2, item.getMedicineId());
                    ps.setInt(3, batchId);
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, item.getPurchasePrice());
                    ps.setBigDecimal(6, item.getTotalPrice());
                    ps.executeUpdate();
                }

                // 2c. Record inventory transaction
                InventoryTransaction txn = new InventoryTransaction();
                txn.setMedicineId(item.getMedicineId());
                txn.setBatchId(batchId);
                txn.setTransactionType(InventoryTransaction.TransactionType.PURCHASE);
                txn.setQuantityChange(item.getQuantity());    // positive = stock in
                txn.setReferenceNumber(purchase.getInvoiceNumber());
                txn.setReason("Purchase from supplier via invoice: " + purchase.getInvoiceNumber());
                inventoryDAO.recordTransaction(txn, conn);
            }

            conn.commit();   // ── All operations succeeded — commit transaction ──
            logger.info("Purchase saved successfully. ID: {}, Invoice: {}",
                        purchaseId, purchase.getInvoiceNumber());
            return purchaseId;

        } catch (SQLException e) {
            logger.error("Error saving purchase — rolling back transaction", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException re) {
                    logger.error("Rollback failed!", re);
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warn("Error closing connection after purchase", e);
                }
            }
        }
    }

    /** Finds all purchases, most recent first, with supplier names. */
    public List<Purchase> findAll(int limit, int offset) {
        String sql = "SELECT p.id, p.invoice_number, p.supplier_id, p.total_amount, " +
                     "p.notes, p.purchase_date, p.created_at, s.name AS supplier_name " +
                     "FROM purchases p JOIN suppliers s ON s.id = p.supplier_id " +
                     "ORDER BY p.purchase_date DESC, p.created_at DESC LIMIT ? OFFSET ?";
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPurchaseRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching purchases", e);
        }
        return list;
    }

    /** Searches purchases by invoice number, supplier name, or date. */
    public List<Purchase> search(String keyword) {
        String sql = "SELECT p.id, p.invoice_number, p.supplier_id, p.total_amount, " +
                     "p.notes, p.purchase_date, p.created_at, s.name AS supplier_name " +
                     "FROM purchases p JOIN suppliers s ON s.id = p.supplier_id " +
                     "WHERE p.invoice_number LIKE ? OR s.name LIKE ? OR CAST(p.purchase_date AS CHAR) LIKE ? " +
                     "ORDER BY p.purchase_date DESC";
        List<Purchase> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPurchaseRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching purchases: {}", keyword, e);
        }
        return list;
    }

    /** Loads a purchase with all its line items. */
    public Purchase findById(int id) {
        String sql = "SELECT p.id, p.invoice_number, p.supplier_id, p.total_amount, " +
                     "p.notes, p.purchase_date, p.created_at, s.name AS supplier_name " +
                     "FROM purchases p JOIN suppliers s ON s.id = p.supplier_id WHERE p.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Purchase purchase = mapPurchaseRow(rs);
                    purchase.setItems(findItemsByPurchaseId(id, conn));
                    return purchase;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding purchase by id: {}", id, e);
        }
        return null;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM purchases";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting purchases", e);
        }
        return 0;
    }

    /** Returns the next invoice sequence number for today. */
    public long getNextPurchaseSequence() {
        String sql = "SELECT COUNT(*) + 1 FROM purchases WHERE DATE(purchase_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            logger.error("Error getting purchase sequence", e);
        }
        return 1L;
    }

    // ─────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────

    private List<PurchaseItem> findItemsByPurchaseId(int purchaseId, Connection conn) throws SQLException {
        String sql = "SELECT pi.id, pi.purchase_id, pi.medicine_id, pi.batch_id, " +
                     "pi.quantity, pi.purchase_price, pi.total_price, " +
                     "m.name AS medicine_name, mb.batch_number " +
                     "FROM purchase_items pi " +
                     "JOIN medicines m ON m.id = pi.medicine_id " +
                     "JOIN medicine_batches mb ON mb.id = pi.batch_id " +
                     "WHERE pi.purchase_id = ?";
        List<PurchaseItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, purchaseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PurchaseItem item = new PurchaseItem();
                    item.setId(rs.getInt("id"));
                    item.setPurchaseId(rs.getInt("purchase_id"));
                    item.setMedicineId(rs.getInt("medicine_id"));
                    item.setBatchId(rs.getInt("batch_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPurchasePrice(rs.getBigDecimal("purchase_price"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setBatchNumber(rs.getString("batch_number"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private MedicineBatch findBatchByNumber(int medicineId, String batchNumber, Connection conn)
            throws SQLException {
        String sql = "SELECT * FROM medicine_batches WHERE medicine_id = ? AND batch_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            ps.setString(2, batchNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MedicineBatch b = new MedicineBatch();
                    b.setId(rs.getInt("id"));
                    b.setMedicineId(rs.getInt("medicine_id"));
                    b.setBatchNumber(rs.getString("batch_number"));
                    b.setQuantity(rs.getInt("quantity"));
                    return b;
                }
            }
        }
        return null;
    }

    private Purchase mapPurchaseRow(ResultSet rs) throws SQLException {
        Purchase p = new Purchase();
        p.setId(rs.getInt("id"));
        p.setInvoiceNumber(rs.getString("invoice_number"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setTotalAmount(rs.getBigDecimal("total_amount"));
        p.setNotes(rs.getString("notes"));
        Date pd = rs.getDate("purchase_date");
        if (pd != null) p.setPurchaseDate(pd.toLocalDate());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) p.setCreatedAt(created.toLocalDateTime());
        p.setSupplierName(rs.getString("supplier_name"));
        return p;
    }
}
