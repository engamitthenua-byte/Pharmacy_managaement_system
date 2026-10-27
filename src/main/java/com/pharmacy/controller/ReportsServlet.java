package com.pharmacy.controller;

import com.pharmacy.dao.ReportsDAO;
import com.pharmacy.util.AppUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ReportsServlet — provides various business reports:
 *  - Daily / Date-range Sales Report
 *  - Monthly Revenue Breakdown
 *  - Supplier Purchase Report
 *  - Current Inventory & Valuation Report
 *  - Top Best-Selling Medicines
 *  - Customer History
 *  - Supplier Order History
 */
public class ReportsServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ReportsServlet.class);
    private final ReportsDAO reportsDAO = new ReportsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "index");

        switch (action) {
            case "sales"       -> generateSalesReport(req, resp);
            case "monthly"     -> generateMonthlyReport(req, resp);
            case "purchases"   -> generatePurchaseReport(req, resp);
            case "inventory"   -> generateInventoryReport(req, resp);
            case "bestsellers" -> generateBestSellersReport(req, resp);
            case "customers"   -> generateCustomerReport(req, resp);
            case "suppliers"   -> generateSupplierReport(req, resp);
            default            -> showReportsIndex(req, resp);
        }
    }

    private void showReportsIndex(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("summary", reportsDAO.getDashboardSummary());
        forward(req, resp, "reports/index.jsp");
    }

    private void generateSalesReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate to   = parseDate(req.getParameter("to"), LocalDate.now());
        LocalDate from = parseDate(req.getParameter("from"), to.minusDays(30));

        List<Map<String, Object>> data = reportsDAO.getDailySalesReport(from, to);
        req.setAttribute("reportData", data);
        req.setAttribute("fromDate",   from.toString());
        req.setAttribute("toDate",     to.toString());
        forward(req, resp, "reports/sales.jsp");
    }

    private void generateMonthlyReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int year = LocalDate.now().getYear();
        String yStr = req.getParameter("year");
        if (yStr != null && !yStr.isBlank()) {
            try { year = Integer.parseInt(yStr); } catch (NumberFormatException ignored) {}
        }

        List<Map<String, Object>> data = reportsDAO.getMonthlySalesReport(year);
        req.setAttribute("reportData", data);
        req.setAttribute("currentYear", year);
        forward(req, resp, "reports/monthly.jsp");
    }

    private void generatePurchaseReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LocalDate to   = parseDate(req.getParameter("to"), LocalDate.now());
        LocalDate from = parseDate(req.getParameter("from"), to.minusDays(30));

        List<Map<String, Object>> data = reportsDAO.getPurchaseReport(from, to);
        req.setAttribute("reportData", data);
        req.setAttribute("fromDate",   from.toString());
        req.setAttribute("toDate",     to.toString());
        forward(req, resp, "reports/purchases.jsp");
    }

    private void generateInventoryReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportsDAO.getInventoryReport();
        req.setAttribute("reportData", data);
        forward(req, resp, "reports/inventory.jsp");
    }

    private void generateBestSellersReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportsDAO.getBestSellingMedicines(20);
        req.setAttribute("reportData", data);
        forward(req, resp, "reports/bestsellers.jsp");
    }

    private void generateCustomerReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportsDAO.getCustomerPurchaseHistory();
        req.setAttribute("reportData", data);
        forward(req, resp, "reports/customers.jsp");
    }

    private void generateSupplierReport(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        List<Map<String, Object>> data = reportsDAO.getSupplierPurchaseHistory();
        req.setAttribute("reportData", data);
        forward(req, resp, "reports/suppliers.jsp");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }

    private LocalDate parseDate(String val, LocalDate fallback) {
        if (val == null || val.isBlank()) return fallback;
        try {
            return LocalDate.parse(val);
        } catch (Exception e) {
            return fallback;
        }
    }
}
