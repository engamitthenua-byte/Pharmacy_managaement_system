package com.pharmacy.dao;

import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * ReportsDAO — SQL aggregate queries for all report types.
 *
 * All results are returned as List<Map<String,Object>> for flexibility —
 * this allows JSP pages to iterate generically without needing
 * dedicated report model classes.
 */
public class ReportsDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReportsDAO.class);

    // ─────────────────────────────────────────────────────────
    // Sales Reports
    // ─────────────────────────────────────────────────────────

    /** Daily sales report — one row per day within date range. */
    public List<Map<String, Object>> getDailySalesReport(LocalDate from, LocalDate to) {
        String sql = "SELECT sale_date, COUNT(*) AS total_invoices, " +
                     "SUM(total_amount) AS total_revenue, SUM(discount) AS total_discount " +
                     "FROM sales WHERE sale_date BETWEEN ? AND ? " +
                     "GROUP BY sale_date ORDER BY sale_date DESC";
        return executeQuery(sql, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
    }

    /** Monthly sales report — one row per month. */
    public List<Map<String, Object>> getMonthlySalesReport(int year) {
        String sql = "SELECT MONTH(sale_date) AS month, MONTHNAME(sale_date) AS month_name, " +
                     "COUNT(*) AS total_invoices, SUM(total_amount) AS total_revenue " +
                     "FROM sales WHERE YEAR(sale_date) = ? " +
                     "GROUP BY MONTH(sale_date), MONTHNAME(sale_date) ORDER BY month";
        return executeQuery(sql, year);
    }

    /** Purchase report within a date range. */
    public List<Map<String, Object>> getPurchaseReport(LocalDate from, LocalDate to) {
        String sql = "SELECT p.purchase_date, p.invoice_number, s.name AS supplier_name, " +
                     "p.total_amount, COUNT(pi.id) AS item_count " +
                     "FROM purchases p JOIN suppliers s ON s.id = p.supplier_id " +
                     "JOIN purchase_items pi ON pi.purchase_id = p.id " +
                     "WHERE p.purchase_date BETWEEN ? AND ? " +
                     "GROUP BY p.id ORDER BY p.purchase_date DESC";
        return executeQuery(sql, java.sql.Date.valueOf(from), java.sql.Date.valueOf(to));
    }

    /** Current inventory report — all medicines with total stock. */
    public List<Map<String, Object>> getInventoryReport() {
        String sql = "SELECT m.id, m.name AS medicine_name, c.name AS category_name, " +
                     "s.name AS supplier_name, m.unit, m.min_stock_level, " +
                     "COALESCE(SUM(CASE WHEN mb.expiry_date >= CURDATE() THEN mb.quantity ELSE 0 END), 0) AS available_qty, " +
                     "COALESCE(SUM(mb.quantity), 0) AS total_qty " +
                     "FROM medicines m " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "LEFT JOIN suppliers s ON s.id = m.supplier_id " +
                     "LEFT JOIN medicine_batches mb ON mb.medicine_id = m.id " +
                     "GROUP BY m.id ORDER BY available_qty ASC";
        return executeQuery(sql);
    }

    /** Best-selling medicines by quantity sold. */
    public List<Map<String, Object>> getBestSellingMedicines(int limit) {
        String sql = "SELECT m.name AS medicine_name, c.name AS category_name, " +
                     "SUM(si.quantity) AS total_sold, " +
                     "SUM(si.total_price) AS total_revenue " +
                     "FROM sale_items si " +
                     "JOIN medicines m ON m.id = si.medicine_id " +
                     "LEFT JOIN categories c ON c.id = m.category_id " +
                     "GROUP BY si.medicine_id ORDER BY total_sold DESC LIMIT ?";
        return executeQuery(sql, limit);
    }

    /** Customer purchase history — total spent per customer. */
    public List<Map<String, Object>> getCustomerPurchaseHistory() {
        String sql = "SELECT COALESCE(c.name, s.customer_name, 'Walk-in') AS customer_name, " +
                     "COUNT(s.id) AS total_invoices, SUM(s.total_amount) AS total_spent " +
                     "FROM sales s LEFT JOIN customers c ON c.id = s.customer_id " +
                     "GROUP BY s.customer_id, s.customer_name ORDER BY total_spent DESC";
        return executeQuery(sql);
    }

    /** Supplier purchase history. */
    public List<Map<String, Object>> getSupplierPurchaseHistory() {
        String sql = "SELECT s.name AS supplier_name, COUNT(p.id) AS total_orders, " +
                     "SUM(p.total_amount) AS total_purchased " +
                     "FROM purchases p JOIN suppliers s ON s.id = p.supplier_id " +
                     "GROUP BY p.supplier_id ORDER BY total_purchased DESC";
        return executeQuery(sql);
    }

    // ─────────────────────────────────────────────────────────
    // Dashboard summary
    // ─────────────────────────────────────────────────────────

    public Map<String, Object> getDashboardSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection()) {
            summary.put("totalMedicines", queryScalar(conn, "SELECT COUNT(*) FROM medicines"));
            summary.put("totalCategories", queryScalar(conn, "SELECT COUNT(*) FROM categories"));
            summary.put("totalSuppliers", queryScalar(conn, "SELECT COUNT(*) FROM suppliers"));
            summary.put("totalCustomers", queryScalar(conn, "SELECT COUNT(*) FROM customers"));
            summary.put("totalSales", queryScalar(conn, "SELECT COUNT(*) FROM sales"));
            summary.put("todayRevenue", queryDecimal(conn, "SELECT COALESCE(SUM(total_amount),0) FROM sales WHERE sale_date=CURDATE()"));
            summary.put("monthRevenue", queryDecimal(conn,
                "SELECT COALESCE(SUM(total_amount),0) FROM sales WHERE MONTH(sale_date)=MONTH(CURDATE()) AND YEAR(sale_date)=YEAR(CURDATE())"));
            summary.put("lowStockCount", queryScalar(conn,
                "SELECT COUNT(*) FROM (" +
                "  SELECT m.id FROM medicines m " +
                "  LEFT JOIN medicine_batches mb ON mb.medicine_id = m.id AND mb.expiry_date >= CURDATE() " +
                "  GROUP BY m.id, m.min_stock_level " +
                "  HAVING COALESCE(SUM(mb.quantity),0) <= m.min_stock_level" +
                ") AS t"));
            summary.put("expiredCount", queryScalar(conn,
                "SELECT COUNT(DISTINCT medicine_id) FROM medicine_batches WHERE expiry_date < CURDATE() AND quantity > 0"));
            summary.put("nearExpiryCount", queryScalar(conn,
                "SELECT COUNT(DISTINCT medicine_id) FROM medicine_batches " +
                "WHERE expiry_date >= CURDATE() AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) AND quantity > 0"));
        } catch (SQLException e) {
            logger.error("Error loading dashboard summary", e);
        }
        return summary;
    }

    // ─────────────────────────────────────────────────────────
    // Generic query helpers
    // ─────────────────────────────────────────────────────────

    private List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error executing report query: {}", sql, e);
        }
        return results;
    }

    private long queryScalar(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        }
    }

    private BigDecimal queryDecimal(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }
}
