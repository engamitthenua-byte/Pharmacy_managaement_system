package com.pharmacy.controller;

import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.PurchaseDAO;
import com.pharmacy.dao.SupplierDAO;
import com.pharmacy.model.*;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * PurchaseServlet — handles purchase invoice creation and history.
 *
 * GET  /purchases              → Purchase history list
 * GET  /purchases?action=new   → New purchase form
 * GET  /purchases?action=view&id=N → View purchase invoice
 * POST /purchases?action=save  → Save new purchase (transactional)
 */
public class PurchaseServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseServlet.class);
    private static final int PAGE_SIZE = 15;

    private final PurchaseDAO purchaseDAO = new PurchaseDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "new"    -> showNewPurchaseForm(req, resp);
            case "view"   -> viewPurchase(req, resp);
            case "search" -> searchPurchases(req, resp);
            default       -> listPurchases(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        if ("save".equals(action)) {
            savePurchase(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/purchases");
        }
    }

    private void listPurchases(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page   = parsePage(req);
        int total  = purchaseDAO.count();
        int offset = (page - 1) * PAGE_SIZE;
        req.setAttribute("purchases",   purchaseDAO.findAll(PAGE_SIZE, offset));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages",  AppUtil.totalPages(total, PAGE_SIZE));
        req.setAttribute("totalCount",  total);
        req.setAttribute("pageInfo",    AppUtil.paginationInfo(page, PAGE_SIZE, total));
        forward(req, resp, "purchases/list.jsp");
    }

    private void showNewPurchaseForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("suppliers",     supplierDAO.findAll());
        req.setAttribute("medicines",     medicineDAO.findAll());
        req.setAttribute("invoiceNumber", AppUtil.generatePurchaseInvoiceNumber(purchaseDAO.getNextPurchaseSequence()));
        req.setAttribute("today",         LocalDate.now().toString());
        forward(req, resp, "purchases/new.jsp");
    }

    private void viewPurchase(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/purchases"); return; }
        Purchase purchase = purchaseDAO.findById(id);
        if (purchase == null) {
            req.getSession().setAttribute("errorMsg", "Purchase invoice not found.");
            resp.sendRedirect(req.getContextPath() + "/purchases");
            return;
        }
        req.setAttribute("purchase", purchase);
        forward(req, resp, "purchases/view.jsp");
    }

    private void searchPurchases(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = AppUtil.safe(req.getParameter("q"));
        req.setAttribute("purchases", purchaseDAO.search(keyword));
        req.setAttribute("keyword",   keyword);
        forward(req, resp, "purchases/list.jsp");
    }

    /**
     * Parses purchase form data and creates the purchase.
     *
     * Form fields (dynamic - submitted as arrays):
     *   medicineIds[]     - medicine IDs for each line item
     *   batchNumbers[]    - batch numbers
     *   sellingPrices[]   - selling prices
     *   purchasePrices[]  - purchase prices
     *   quantities[]      - quantities
     *   mfgDates[]        - manufacturing dates
     *   expiryDates[]     - expiry dates
     */
    private void savePurchase(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        List<String> errors = new ArrayList<>();

        // ── Parse header fields ──────────────────────────────────────────────
        String invoiceNumber = ValidationUtil.getParam(req, "invoiceNumber");
        String supplierId    = ValidationUtil.getParam(req, "supplierId");
        String purchaseDate  = ValidationUtil.getParam(req, "purchaseDate");
        String notes         = ValidationUtil.getParam(req, "notes");

        if (invoiceNumber.isEmpty()) errors.add("Invoice number is required.");
        if (supplierId.isEmpty() || ValidationUtil.parsePositiveInt(supplierId) < 0) errors.add("Please select a supplier.");
        LocalDate date = ValidationUtil.parseDate(purchaseDate);
        if (date == null) { errors.add("Purchase date is required."); date = LocalDate.now(); }

        // ── Parse line items ─────────────────────────────────────────────────
        String[] medicineIds    = req.getParameterValues("medicineIds");
        String[] batchNumbers   = req.getParameterValues("batchNumbers");
        String[] sellingPrices  = req.getParameterValues("sellingPrices");
        String[] purchasePrices = req.getParameterValues("purchasePrices");
        String[] quantities     = req.getParameterValues("quantities");
        String[] mfgDates       = req.getParameterValues("mfgDates");
        String[] expiryDates    = req.getParameterValues("expiryDates");

        if (medicineIds == null || medicineIds.length == 0) {
            errors.add("Please add at least one medicine to the purchase.");
        }

        List<PurchaseItem> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (medicineIds != null) {
            for (int i = 0; i < medicineIds.length; i++) {
                PurchaseItem item = new PurchaseItem();

                int medId = ValidationUtil.parsePositiveInt(medicineIds[i]);
                if (medId < 0) { errors.add("Row " + (i+1) + ": Invalid medicine selected."); continue; }
                item.setMedicineId(medId);

                String bn = (batchNumbers != null && i < batchNumbers.length) ? batchNumbers[i].trim() : "";
                if (bn.isEmpty()) { errors.add("Row " + (i+1) + ": Batch number is required."); continue; }
                item.setBatchNumber(bn);

                BigDecimal pp = (purchasePrices != null && i < purchasePrices.length)
                    ? ValidationUtil.parseNonNegativeDecimal(purchasePrices[i]) : null;
                if (pp == null) { errors.add("Row " + (i+1) + ": Invalid purchase price."); continue; }
                item.setPurchasePrice(pp);

                BigDecimal sp = (sellingPrices != null && i < sellingPrices.length)
                    ? ValidationUtil.parseNonNegativeDecimal(sellingPrices[i]) : null;
                if (sp == null) { errors.add("Row " + (i+1) + ": Invalid selling price."); continue; }
                item.setSellingPrice(sp);

                int qty = (quantities != null && i < quantities.length)
                    ? ValidationUtil.parsePositiveInt(quantities[i]) : -1;
                if (qty < 0) { errors.add("Row " + (i+1) + ": Quantity must be a positive number."); continue; }
                item.setQuantity(qty);

                if (expiryDates != null && i < expiryDates.length) {
                    LocalDate expiry = ValidationUtil.parseDate(expiryDates[i]);
                    if (expiry == null) { errors.add("Row " + (i+1) + ": Invalid expiry date."); continue; }
                    item.setExpiryDate(expiry);
                }
                if (mfgDates != null && i < mfgDates.length && !mfgDates[i].trim().isEmpty()) {
                    LocalDate mfg = ValidationUtil.parseDate(mfgDates[i]);
                    item.setManufacturingDate(mfg);
                }

                item.calculateTotal();
                totalAmount = totalAmount.add(item.getTotalPrice());
                items.add(item);
            }
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors",        errors);
            req.setAttribute("suppliers",     supplierDAO.findAll());
            req.setAttribute("medicines",     medicineDAO.findAll());
            req.setAttribute("invoiceNumber", invoiceNumber);
            req.setAttribute("today",         LocalDate.now().toString());
            forward(req, resp, "purchases/new.jsp");
            return;
        }

        // ── Build Purchase object and save ───────────────────────────────────
        Purchase purchase = new Purchase();
        purchase.setInvoiceNumber(invoiceNumber);
        purchase.setSupplierId(ValidationUtil.parsePositiveInt(supplierId));
        purchase.setTotalAmount(totalAmount);
        purchase.setNotes(notes);
        purchase.setPurchaseDate(date);
        purchase.setItems(items);

        int id = purchaseDAO.savePurchase(purchase);
        if (id > 0) {
            req.getSession().setAttribute("successMsg",
                "Purchase invoice " + invoiceNumber + " saved successfully. Stock updated.");
            resp.sendRedirect(req.getContextPath() + "/purchases?action=view&id=" + id);
        } else {
            req.getSession().setAttribute("errorMsg", "Failed to save purchase. Please try again.");
            resp.sendRedirect(req.getContextPath() + "/purchases?action=new");
        }
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    private int parsePage(HttpServletRequest req) {
        try { return Math.max(1, Integer.parseInt(AppUtil.safe(req.getParameter("page")))); }
        catch (NumberFormatException e) { return 1; }
    }
}
