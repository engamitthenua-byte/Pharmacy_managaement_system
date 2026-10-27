package com.pharmacy.controller;

import com.pharmacy.dao.CustomerDAO;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.dao.SalesDAO;
import com.pharmacy.model.*;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SalesServlet.class);
    private static final int PAGE_SIZE = 15;

    private final SalesDAO salesDAO = new SalesDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final MedicineDAO medicineDAO = new MedicineDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        switch (action) {
            case "pos", "new"  -> showPosForm(req, resp);
            case "invoice", "view" -> viewInvoice(req, resp);
            case "search"      -> searchSales(req, resp);
            case "batches"     -> getBatchesJson(req, resp);
            default            -> listSales(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        if ("save".equals(action)) {
            processSale(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/sales");
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET Handlers
    // ─────────────────────────────────────────────────────────

    private void listSales(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page   = parsePage(req);
        int total  = salesDAO.count();
        int offset = (page - 1) * PAGE_SIZE;

        req.setAttribute("sales",       salesDAO.findAll(PAGE_SIZE, offset));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages",  AppUtil.totalPages(total, PAGE_SIZE));
        req.setAttribute("totalCount",  total);
        req.setAttribute("pageInfo",    AppUtil.paginationInfo(page, PAGE_SIZE, total));
        req.setAttribute("totalRevenue", salesDAO.getTotalRevenue());
        req.setAttribute("todayRevenue", salesDAO.getTodayRevenue());
        forward(req, resp, "sales/list.jsp");
    }

    private void showPosForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("customers",     customerDAO.findAll());
        req.setAttribute("medicines",     medicineDAO.findAll());
        req.setAttribute("invoiceNumber", AppUtil.generateSaleInvoiceNumber(salesDAO.getNextSaleSequence()));
        req.setAttribute("today",         LocalDate.now().toString());
        forward(req, resp, "sales/pos.jsp");
    }

    private void viewInvoice(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id <= 0) {
            resp.sendRedirect(req.getContextPath() + "/sales");
            return;
        }

        Sale sale = salesDAO.findById(id);
        if (sale == null) {
            req.getSession().setAttribute("errorMsg", "Sale invoice #" + id + " not found.");
            resp.sendRedirect(req.getContextPath() + "/sales");
            return;
        }

        req.setAttribute("sale", sale);
        forward(req, resp, "sales/invoice.jsp");
    }

    private void searchSales(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = AppUtil.safe(req.getParameter("q"));
        List<Sale> results = salesDAO.search(keyword);

        req.setAttribute("sales",        results);
        req.setAttribute("keyword",      keyword);
        req.setAttribute("totalRevenue", salesDAO.getTotalRevenue());
        req.setAttribute("todayRevenue", salesDAO.getTodayRevenue());
        forward(req, resp, "sales/list.jsp");
    }

    /**
     * Light-weight JSON endpoint returning active batches for a given medicine.
     * Used by the POS frontend dynamic dropdown.
     */
    private void getBatchesJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int medicineId = ValidationUtil.parseId(req, "medicineId");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        if (medicineId <= 0) {
            out.write("[]");
            return;
        }

        List<MedicineBatch> batches = medicineDAO.findAvailableBatches(medicineId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < batches.size(); i++) {
            MedicineBatch b = batches.get(i);
            sb.append("{")
              .append("\"id\":").append(b.getId()).append(",")
              .append("\"batchNumber\":\"").append(escapeJson(b.getBatchNumber())).append("\",")
              .append("\"sellingPrice\":").append(b.getSellingPrice()).append(",")
              .append("\"quantity\":").append(b.getQuantity()).append(",")
              .append("\"expiryDate\":\"").append(b.getExpiryDate()).append("\"")
              .append("}");
            if (i < batches.size() - 1) sb.append(",");
        }
        sb.append("]");
        out.write(sb.toString());
    }

    // ─────────────────────────────────────────────────────────
    // POST Handlers
    // ─────────────────────────────────────────────────────────

    private void processSale(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<String> errors = new ArrayList<>();

        String invoiceNumber = ValidationUtil.getParam(req, "invoiceNumber");
        if (invoiceNumber.isEmpty()) {
            invoiceNumber = AppUtil.generateSaleInvoiceNumber(salesDAO.getNextSaleSequence());
        }

        String custIdParam = ValidationUtil.getParam(req, "customerId");
        Integer customerId = null;
        if (!custIdParam.isEmpty() && !"0".equals(custIdParam)) {
            try {
                customerId = Integer.parseInt(custIdParam);
            } catch (NumberFormatException ignored) {}
        }

        String customerName = ValidationUtil.getParam(req, "customerName");
        if (customerId == null && customerName.isEmpty()) {
            customerName = "Walk-in Customer";
        }

        String saleDateStr = ValidationUtil.getParam(req, "saleDate");
        LocalDate saleDate = LocalDate.now();
        if (!saleDateStr.isEmpty()) {
            try {
                saleDate = LocalDate.parse(saleDateStr);
            } catch (Exception e) {
                errors.add("Invalid sale date.");
            }
        }

        String discountStr = ValidationUtil.getParam(req, "discount");
        BigDecimal discount = ValidationUtil.parseBigDecimal(discountStr, BigDecimal.ZERO);

        String taxStr = ValidationUtil.getParam(req, "taxAmount");
        BigDecimal taxAmount = ValidationUtil.parseBigDecimal(taxStr, BigDecimal.ZERO);

        String notes = ValidationUtil.getParam(req, "notes");

        // Parse line items
        String[] medicineIds = req.getParameterValues("medicineId[]");
        String[] batchIds    = req.getParameterValues("batchId[]");
        String[] quantities  = req.getParameterValues("quantity[]");
        String[] prices      = req.getParameterValues("price[]");

        if (medicineIds == null || medicineIds.length == 0) {
            errors.add("Please add at least one medicine item to generate a bill.");
        }

        List<SaleItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (medicineIds != null && batchIds != null && quantities != null && prices != null) {
            for (int i = 0; i < medicineIds.length; i++) {
                int medId   = ValidationUtil.parseNonNegativeInt(medicineIds[i]);
                int batchId = ValidationUtil.parseNonNegativeInt(batchIds[i]);
                int qty     = ValidationUtil.parseNonNegativeInt(quantities[i]);
                BigDecimal price = ValidationUtil.parseBigDecimal(prices[i], BigDecimal.ZERO);

                if (medId <= 0) {
                    errors.add("Item #" + (i + 1) + ": Select a medicine.");
                    continue;
                }
                if (batchId <= 0) {
                    errors.add("Item #" + (i + 1) + ": Select a valid batch.");
                    continue;
                }
                if (qty <= 0) {
                    errors.add("Item #" + (i + 1) + ": Quantity must be greater than 0.");
                    continue;
                }
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Item #" + (i + 1) + ": Price must be greater than 0.");
                    continue;
                }

                BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));
                subtotal = subtotal.add(lineTotal);

                SaleItem item = new SaleItem();
                item.setMedicineId(medId);
                item.setBatchId(batchId);
                item.setQuantity(qty);
                item.setSellingPrice(price);
                item.setDiscount(BigDecimal.ZERO);
                item.setTotalPrice(lineTotal);
                items.add(item);
            }
        }

        if (items.isEmpty() && errors.isEmpty()) {
            errors.add("No valid items found in sale order.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("customers", customerDAO.findAll());
            req.setAttribute("medicines", medicineDAO.findAll());
            req.setAttribute("invoiceNumber", invoiceNumber);
            req.setAttribute("today", saleDate.toString());
            forward(req, resp, "sales/pos.jsp");
            return;
        }

        // Calculate final total
        BigDecimal totalAmount = subtotal.subtract(discount).add(taxAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber(invoiceNumber);
        sale.setCustomerId(customerId);
        sale.setCustomerName(customerName);
        sale.setSubtotal(subtotal);
        sale.setDiscount(discount);
        sale.setTaxAmount(taxAmount);
        sale.setTotalAmount(totalAmount);
        sale.setNotes(notes);
        sale.setSaleDate(saleDate);
        sale.setItems(items);

        try {
            int saleId = salesDAO.saveSale(sale);
            if (saleId > 0) {
                req.getSession().setAttribute("successMsg", "Sale invoice " + invoiceNumber + " completed successfully!");
                resp.sendRedirect(req.getContextPath() + "/sales?action=invoice&id=" + saleId);
            } else {
                req.getSession().setAttribute("errorMsg", "Failed to process sale. Check stock levels or database connectivity.");
                resp.sendRedirect(req.getContextPath() + "/sales?action=pos");
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Business rule violation in sale: {}", e.getMessage());
            req.setAttribute("errors", List.of(e.getMessage()));
            req.setAttribute("customers", customerDAO.findAll());
            req.setAttribute("medicines", medicineDAO.findAll());
            req.setAttribute("invoiceNumber", invoiceNumber);
            req.setAttribute("today", saleDate.toString());
            forward(req, resp, "sales/pos.jsp");
        } catch (Exception e) {
            logger.error("Unexpected error saving sale", e);
            req.getSession().setAttribute("errorMsg", "An unexpected error occurred: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/sales?action=pos");
        }
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

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

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
