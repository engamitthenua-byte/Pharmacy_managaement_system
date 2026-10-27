package com.pharmacy.controller;

import com.pharmacy.dao.InventoryDAO;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.InventoryTransaction;
import com.pharmacy.model.Medicine;
import com.pharmacy.model.MedicineBatch;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * InventoryServlet — manages stock monitoring, batch inspections,
 * manual stock adjustments, and inventory movement audit logs.
 *
 * GET  /inventory                 → Overview of inventory & batches
 * GET  /inventory?action=transactions → Audit log of all stock movements
 * GET  /inventory?action=adjust   → Form for manual stock adjustment
 * POST /inventory?action=adjust   → Submit stock adjustment
 */
public class InventoryServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServlet.class);
    private static final int PAGE_SIZE = 20;

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final MedicineDAO medicineDAO   = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        switch (action) {
            case "transactions", "audit" -> listTransactions(req, resp);
            case "adjust"                -> showAdjustForm(req, resp);
            case "lowstock"              -> showLowStock(req, resp);
            case "expired"               -> showExpired(req, resp);
            case "nearexpiry"            -> showNearExpiry(req, resp);
            default                      -> listInventory(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        if ("adjust".equals(action)) {
            processAdjustment(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/inventory");
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET Handlers
    // ─────────────────────────────────────────────────────────

    private void listInventory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Medicine> medicines = medicineDAO.findAll();
        req.setAttribute("medicines", medicines);
        req.setAttribute("lowStockList", medicineDAO.findLowStock());
        req.setAttribute("expiredBatches", medicineDAO.findExpiredBatches());
        req.setAttribute("nearExpiryBatches", medicineDAO.findNearExpiryBatches(30));
        forward(req, resp, "inventory/list.jsp");
    }

    private void listTransactions(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page   = parsePage(req);
        int total  = inventoryDAO.count();
        int offset = (page - 1) * PAGE_SIZE;

        String medIdStr = req.getParameter("medicineId");
        List<InventoryTransaction> transactions;

        if (medIdStr != null && !medIdStr.isBlank()) {
            int medId = ValidationUtil.parseNonNegativeInt(medIdStr);
            transactions = inventoryDAO.findByMedicine(medId);
            req.setAttribute("filterMedicineId", medId);
        } else {
            transactions = inventoryDAO.findAll(PAGE_SIZE, offset);
        }

        req.setAttribute("transactions", transactions);
        req.setAttribute("currentPage",  page);
        req.setAttribute("totalPages",   AppUtil.totalPages(total, PAGE_SIZE));
        req.setAttribute("totalCount",   total);
        req.setAttribute("pageInfo",     AppUtil.paginationInfo(page, PAGE_SIZE, total));
        req.setAttribute("medicines",    medicineDAO.findAll());
        forward(req, resp, "inventory/transactions.jsp");
    }

    private void showAdjustForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("medicines", medicineDAO.findAll());
        forward(req, resp, "inventory/adjust.jsp");
    }

    private void showLowStock(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("medicines", medicineDAO.findLowStock());
        req.setAttribute("filterTitle", "Low-Stock Medicines Alert");
        forward(req, resp, "inventory/lowstock.jsp");
    }

    private void showExpired(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("batches", medicineDAO.findExpiredBatches());
        req.setAttribute("filterTitle", "Expired Batches");
        forward(req, resp, "inventory/expired.jsp");
    }

    private void showNearExpiry(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("batches", medicineDAO.findNearExpiryBatches(30));
        req.setAttribute("filterTitle", "Batches Near Expiry (Next 30 Days)");
        forward(req, resp, "inventory/nearexpiry.jsp");
    }

    // ─────────────────────────────────────────────────────────
    // POST Handlers
    // ─────────────────────────────────────────────────────────

    private void processAdjustment(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int medId   = ValidationUtil.parseId(req, "medicineId");
        int batchId = ValidationUtil.parseId(req, "batchId");
        String typeStr = ValidationUtil.getParam(req, "type");
        int qty     = ValidationUtil.parseNonNegativeInt(ValidationUtil.getParam(req, "quantity"));
        String reason = ValidationUtil.getParam(req, "reason");

        if (medId <= 0 || batchId <= 0 || qty <= 0) {
            req.getSession().setAttribute("errorMsg", "Please select valid medicine, batch, and a quantity > 0.");
            resp.sendRedirect(req.getContextPath() + "/inventory?action=adjust");
            return;
        }

        InventoryTransaction.TransactionType type;
        int qtyChange;
        if ("OUT".equalsIgnoreCase(typeStr) || "ADJUSTMENT_OUT".equalsIgnoreCase(typeStr)) {
            type = InventoryTransaction.TransactionType.ADJUSTMENT_OUT;
            qtyChange = -qty;
        } else {
            type = InventoryTransaction.TransactionType.ADJUSTMENT_IN;
            qtyChange = qty;
        }

        InventoryTransaction txn = new InventoryTransaction();
        txn.setMedicineId(medId);
        txn.setBatchId(batchId);
        txn.setTransactionType(type);
        txn.setQuantityChange(qtyChange);
        txn.setReferenceNumber("ADJ-" + System.currentTimeMillis() % 1000000);
        txn.setReason(reason.isEmpty() ? "Manual Stock Adjustment" : reason);

        int result = inventoryDAO.recordAdjustment(txn);
        if (result > 0) {
            req.getSession().setAttribute("successMsg", "Stock adjustment recorded successfully.");
            resp.sendRedirect(req.getContextPath() + "/inventory?action=transactions");
        } else {
            req.getSession().setAttribute("errorMsg", "Failed to record adjustment. Please check batch details.");
            resp.sendRedirect(req.getContextPath() + "/inventory?action=adjust");
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    private int parsePage(HttpServletRequest req) {
        try {
            return Math.max(1, Integer.parseInt(AppUtil.safe(req.getParameter("page"))));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
