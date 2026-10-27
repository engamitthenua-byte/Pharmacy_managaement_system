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
 * SalesDAO — handles sales invoice creation, retrieval, and reporting.
 *
 * The saveSale() method runs everything in a single database transaction:
 *  1. Insert sale header
 *  2. For each item:
 *     a. Validate batch is not expired
 *     b. Validate sufficient stock exists
 *     c. Deduct from medicine_batch quantity
 *     d. Insert sale_item row
 *     e. Record inventory_transaction
 *  3. Commit — or rollback on any failure
 */
public class SalesDAO {

    private static final Logger logger = LoggerFactory.getLogger(SalesDAO.class);

    private final MedicineDAO medicineDAO = new MedicineDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Saves a complete sale (header + items + stock deductions) atomically.
     *
     * @return generated sale ID on success, -1 on failure/rollback
     * @throws IllegalArgumentException if a business rule is violated
     */
    public int saveSale(Sale sale) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // Start transaction

            // ── Step 1: Insert sale header ───────────────────────────────────
            String insertSale =
                "INSERT INTO sales (invoice_number, customer_id, customer_name, subtotal, " +
                "discount, tax_amount, total_amount, notes, sale_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int saleId;
            try (PreparedStatement ps = conn.prepareStatement(insertSale, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, sale.getInvoiceNumber());
                if (sale.getCustomerId() != null) {
                    ps.setInt(2, sale.getCustomerId());
                } else {
                    ps.setNull(2, Types.INTEGER);
                }
                ps.setString(3, sale.getCustomerName());
                ps.setBigDecimal(4, sale.getSubtotal());
                ps.setBigDecimal(5, sale.getDiscount() != null ? sale.getDiscount() : BigDecimal.ZERO);
                ps.setBigDecimal(6, sale.getTaxAmount() != null ? sale.getTaxAmount() : BigDecimal.ZERO);
                ps.setBigDecimal(7, sale.getTotalAmount());
                ps.setString(8, sale.getNotes());
                ps.setDate(9, Date.valueOf(sale.getSaleDate()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Failed to get sale ID");
                    saleId = keys.getInt(1);
                }
            }

            // ── Step 2: Process each line item ──────────────────────────────
            for (SaleItem item : sale.getItems()) {

                // 2a. Lock and verify the batch row
                String lockSql = "SELECT id, expiry_date, quantity FROM medicine_batches " +
                                 "WHERE id = ? FOR UPDATE";
                try (PreparedStatement lockPs = conn.prepareStatement(lockSql)) {
                    lockPs.setInt(1, item.getBatchId());
                    try (ResultSet rs = lockPs.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Batch not found: " + item.getBatchId());
                        }
                        // Check expiry
                        Date expiry = rs.getDate("expiry_date");
                        if (expiry != null && expiry.toLocalDate().isBefore(java.time.LocalDate.now())) {
                            throw new IllegalArgumentException(
                                "Cannot sell expired medicine. Batch ID: " + item.getBatchId());
                        }
                        // Check stock
                        int available = rs.getInt("quantity");
                        if (available < item.getQuantity()) {
                            throw new IllegalArgumentException(
                                "Insufficient stock. Available: " + available +
                                ", Requested: " + item.getQuantity() +
                                ", Batch ID: " + item.getBatchId());
                        }
                    }
                }

                // 2b. Deduct stock from batch
                medicineDAO.decreaseBatchQuantity(item.getBatchId(), item.getQuantity(), conn);

                // 2c. Insert sale_item row
                String insertItem =
                    "INSERT INTO sale_items (sale_id, medicine_id, batch_id, quantity, " +
                    "selling_price, discount, total_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
                    ps.setInt(1, saleId);
                    ps.setInt(2, item.getMedicineId());
                    ps.setInt(3, item.getBatchId());
                    ps.setInt(4, item.getQuantity());
                    ps.setBigDecimal(5, item.getSellingPrice());
                    ps.setBigDecimal(6, item.getDiscount() != null ? item.getDiscount() : BigDecimal.ZERO);
                    ps.setBigDecimal(7, item.getTotalPrice());
                    ps.executeUpdate();
                }

                // 2d. Record inventory transaction
                InventoryTransaction txn = new InventoryTransaction();
                txn.setMedicineId(item.getMedicineId());
                txn.setBatchId(item.getBatchId());
                txn.setTransactionType(InventoryTransaction.TransactionType.SALE);
                txn.setQuantityChange(-item.getQuantity());   // negative = stock out
                txn.setReferenceNumber(sale.getInvoiceNumber());
                txn.setReason("Sale via invoice: " + sale.getInvoiceNumber());
                inventoryDAO.recordTransaction(txn, conn);
            }

            conn.commit();   // ── All operations succeeded — commit transaction ──
            logger.info("Sale saved. ID: {}, Invoice: {}, Total: {}",
                        saleId, sale.getInvoiceNumber(), sale.getTotalAmount());
            return saleId;

        } catch (IllegalArgumentException e) {
            logger.warn("Sale rejected — business rule violation: {}", e.getMessage());
            if (conn != null) { try { conn.rollback(); } catch (SQLException re) { logger.error("Rollback failed", re); } }
            throw e;   // Re-throw so Servlet can show user-friendly error
        } catch (SQLException e) {
            logger.error("Error saving sale — rolling back transaction", e);
            if (conn != null) { try { conn.rollback(); } catch (SQLException re) { logger.error("Rollback failed", re); } }
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); }
                catch (SQLException e) { logger.warn("Error closing connection after sale", e); }
            }
        }
    }

    /** Returns all sales, most recent first. */
    public List<Sale> findAll(int limit, int offset) {
        String sql = "SELECT s.id, s.invoice_number, s.customer_id, s.customer_name, " +
                     "s.subtotal, s.discount, s.tax_amount, s.total_amount, " +
                     "s.notes, s.sale_date, s.created_at, c.name AS registered_customer_name " +
                     "FROM sales s LEFT JOIN customers c ON c.id = s.customer_id " +
                     "ORDER BY s.sale_date DESC, s.created_at DESC LIMIT ? OFFSET ?";
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSaleRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching sales", e);
        }
        return list;
    }

    /** Searches sales by invoice number, customer name, or date. */
    public List<Sale> search(String keyword) {
        String sql = "SELECT s.id, s.invoice_number, s.customer_id, s.customer_name, " +
                     "s.subtotal, s.discount, s.tax_amount, s.total_amount, " +
                     "s.notes, s.sale_date, s.created_at, c.name AS registered_customer_name " +
                     "FROM sales s LEFT JOIN customers c ON c.id = s.customer_id " +
                     "WHERE s.invoice_number LIKE ? OR s.customer_name LIKE ? OR c.name LIKE ? " +
                     "OR CAST(s.sale_date AS CHAR) LIKE ? " +
                     "ORDER BY s.sale_date DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw); ps.setString(2, kw); ps.setString(3, kw); ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSaleRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching sales: {}", keyword, e);
        }
        return list;
    }

    /** Loads a sale with all its line items. */
    public Sale findById(int id) {
        String sql = "SELECT s.id, s.invoice_number, s.customer_id, s.customer_name, " +
                     "s.subtotal, s.discount, s.tax_amount, s.total_amount, " +
                     "s.notes, s.sale_date, s.created_at, c.name AS registered_customer_name " +
                     "FROM sales s LEFT JOIN customers c ON c.id = s.customer_id WHERE s.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapSaleRow(rs);
                    sale.setItems(findItemsBySaleId(id, conn));
                    return sale;
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding sale by id: {}", id, e);
        }
        return null;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting sales", e);
        }
        return 0;
    }

    /** Returns total sales revenue from all time. */
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating total revenue", e);
        }
        return BigDecimal.ZERO;
    }

    /** Returns total revenue for today. */
    public BigDecimal getTodayRevenue() {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) FROM sales WHERE sale_date = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getBigDecimal(1);
        } catch (SQLException e) {
            logger.error("Error calculating today's revenue", e);
        }
        return BigDecimal.ZERO;
    }

    /** Returns the next invoice sequence number for today. */
    public long getNextSaleSequence() {
        String sql = "SELECT COUNT(*) + 1 FROM sales WHERE DATE(sale_date) = CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            logger.error("Error getting sale sequence", e);
        }
        return 1L;
    }

    // ─────────────────────────────────────────────────────────
    // Report queries
    // ─────────────────────────────────────────────────────────

    /** Returns sales within a date range. */
    public List<Sale> findByDateRange(java.time.LocalDate from, java.time.LocalDate to) {
        String sql = "SELECT s.id, s.invoice_number, s.customer_id, s.customer_name, " +
                     "s.subtotal, s.discount, s.tax_amount, s.total_amount, " +
                     "s.notes, s.sale_date, s.created_at, c.name AS registered_customer_name " +
                     "FROM sales s LEFT JOIN customers c ON c.id = s.customer_id " +
                     "WHERE s.sale_date BETWEEN ? AND ? ORDER BY s.sale_date DESC";
        List<Sale> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(from));
            ps.setDate(2, Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSaleRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching sales by date range", e);
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────

    private List<SaleItem> findItemsBySaleId(int saleId, Connection conn) throws SQLException {
        String sql = "SELECT si.id, si.sale_id, si.medicine_id, si.batch_id, " +
                     "si.quantity, si.selling_price, si.discount, si.total_price, " +
                     "m.name AS medicine_name, m.unit, mb.batch_number " +
                     "FROM sale_items si " +
                     "JOIN medicines m ON m.id = si.medicine_id " +
                     "JOIN medicine_batches mb ON mb.id = si.batch_id " +
                     "WHERE si.sale_id = ?";
        List<SaleItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem();
                    item.setId(rs.getInt("id"));
                    item.setSaleId(rs.getInt("sale_id"));
                    item.setMedicineId(rs.getInt("medicine_id"));
                    item.setBatchId(rs.getInt("batch_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setSellingPrice(rs.getBigDecimal("selling_price"));
                    item.setDiscount(rs.getBigDecimal("discount"));
                    item.setTotalPrice(rs.getBigDecimal("total_price"));
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setUnit(rs.getString("unit"));
                    item.setBatchNumber(rs.getString("batch_number"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    private Sale mapSaleRow(ResultSet rs) throws SQLException {
        Sale s = new Sale();
        s.setId(rs.getInt("id"));
        s.setInvoiceNumber(rs.getString("invoice_number"));
        int custId = rs.getInt("customer_id");
        s.setCustomerId(rs.wasNull() ? null : custId);
        s.setCustomerName(rs.getString("customer_name"));
        s.setSubtotal(rs.getBigDecimal("subtotal"));
        s.setDiscount(rs.getBigDecimal("discount"));
        s.setTaxAmount(rs.getBigDecimal("tax_amount"));
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        s.setNotes(rs.getString("notes"));
        Date sd = rs.getDate("sale_date");
        if (sd != null) s.setSaleDate(sd.toLocalDate());
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());
        s.setRegisteredCustomerName(rs.getString("registered_customer_name"));
        return s;
    }
}
