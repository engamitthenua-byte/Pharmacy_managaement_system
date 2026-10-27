package com.pharmacy.dao;

import com.pharmacy.model.InventoryTransaction;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InventoryDAO — records and retrieves inventory transaction history.
 * Every stock movement (purchase, sale, adjustment) is recorded here.
 */
public class InventoryDAO {

    private static final Logger logger = LoggerFactory.getLogger(InventoryDAO.class);

    /**
     * Records a stock movement within an existing transaction.
     * Call this inside a purchase or sale transaction block.
     */
    public void recordTransaction(InventoryTransaction txn, Connection conn) throws SQLException {
        String sql = "INSERT INTO inventory_transactions " +
                     "(medicine_id, batch_id, transaction_type, quantity_change, reference_number, reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, txn.getMedicineId());
            ps.setInt(2, txn.getBatchId());
            ps.setString(3, txn.getTransactionType().name());
            ps.setInt(4, txn.getQuantityChange());
            ps.setString(5, txn.getReferenceNumber());
            ps.setString(6, txn.getReason());
            ps.executeUpdate();
        }
    }

    /**
     * Records a manual stock adjustment (opens its own connection).
     * Returns the generated transaction ID, or -1 on failure.
     */
    public int recordAdjustment(InventoryTransaction txn) {
        String sql = "INSERT INTO inventory_transactions " +
                     "(medicine_id, batch_id, transaction_type, quantity_change, reference_number, reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            conn.setAutoCommit(false);
            try {
                ps.setInt(1, txn.getMedicineId());
                ps.setInt(2, txn.getBatchId());
                ps.setString(3, txn.getTransactionType().name());
                ps.setInt(4, txn.getQuantityChange());
                ps.setString(5, txn.getReferenceNumber());
                ps.setString(6, txn.getReason());
                ps.executeUpdate();

                // Update batch quantity based on the adjustment type
                String updateBatch;
                if (txn.getQuantityChange() > 0) {
                    updateBatch = "UPDATE medicine_batches SET quantity = quantity + ? WHERE id = ?";
                } else {
                    updateBatch = "UPDATE medicine_batches SET quantity = GREATEST(quantity + ?, 0) WHERE id = ?";
                }
                try (PreparedStatement updatePs = conn.prepareStatement(updateBatch)) {
                    updatePs.setInt(1, txn.getQuantityChange());
                    updatePs.setInt(2, txn.getBatchId());
                    updatePs.executeUpdate();
                }

                conn.commit();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error("Error recording stock adjustment", e);
        }
        return -1;
    }

    /** Returns all inventory transactions, most recent first. */
    public List<InventoryTransaction> findAll(int limit, int offset) {
        String sql = "SELECT it.*, m.name AS medicine_name, mb.batch_number " +
                     "FROM inventory_transactions it " +
                     "JOIN medicines m ON m.id = it.medicine_id " +
                     "JOIN medicine_batches mb ON mb.id = it.batch_id " +
                     "ORDER BY it.transaction_date DESC LIMIT ? OFFSET ?";
        List<InventoryTransaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching inventory transactions", e);
        }
        return list;
    }

    /** Returns transactions for a specific medicine. */
    public List<InventoryTransaction> findByMedicine(int medicineId) {
        String sql = "SELECT it.*, m.name AS medicine_name, mb.batch_number " +
                     "FROM inventory_transactions it " +
                     "JOIN medicines m ON m.id = it.medicine_id " +
                     "JOIN medicine_batches mb ON mb.id = it.batch_id " +
                     "WHERE it.medicine_id = ? ORDER BY it.transaction_date DESC";
        List<InventoryTransaction> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching inventory transactions for medicine: {}", medicineId, e);
        }
        return list;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM inventory_transactions";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error counting inventory transactions", e);
        }
        return 0;
    }

    private InventoryTransaction mapRow(ResultSet rs) throws SQLException {
        InventoryTransaction t = new InventoryTransaction();
        t.setId(rs.getInt("id"));
        t.setMedicineId(rs.getInt("medicine_id"));
        t.setBatchId(rs.getInt("batch_id"));
        t.setTransactionType(InventoryTransaction.TransactionType.valueOf(rs.getString("transaction_type")));
        t.setQuantityChange(rs.getInt("quantity_change"));
        t.setReferenceNumber(rs.getString("reference_number"));
        t.setReason(rs.getString("reason"));
        Timestamp txDate = rs.getTimestamp("transaction_date");
        if (txDate != null) t.setTransactionDate(txDate.toLocalDateTime());
        t.setMedicineName(rs.getString("medicine_name"));
        t.setBatchNumber(rs.getString("batch_number"));
        return t;
    }
}
