package com.pharmacy.controller;

import com.pharmacy.dao.CategoryDAO;
import com.pharmacy.dao.SupplierDAO;
import com.pharmacy.dao.MedicineDAO;
import com.pharmacy.model.Medicine;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MedicineServlet — handles all medicine CRUD operations.
 *
 * GET  /medicines?action=list              → List all medicines (paginated)
 * GET  /medicines?action=add              → Show add form
 * POST /medicines?action=add              → Insert new medicine
 * GET  /medicines?action=edit&id=N        → Show edit form
 * POST /medicines?action=edit             → Update medicine
 * POST /medicines?action=delete&id=N     → Delete medicine
 * GET  /medicines?action=view&id=N        → View medicine details
 * GET  /medicines?action=search&q=...    → Search medicines
 * GET  /medicines?action=lowstock        → Show low-stock medicines
 * GET  /medicines?action=batches&id=N    → View batches for a medicine
 */
public class MedicineServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(MedicineServlet.class);
    private static final int PAGE_SIZE = 15;

    private final MedicineDAO medicineDAO   = new MedicineDAO();
    private final CategoryDAO categoryDAO   = new CategoryDAO();
    private final SupplierDAO supplierDAO   = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        switch (action) {
            case "add"      -> showAddForm(req, resp);
            case "edit"     -> showEditForm(req, resp);
            case "view"     -> viewMedicine(req, resp);
            case "delete"   -> deleteMedicine(req, resp);
            case "search"   -> searchMedicines(req, resp);
            case "lowstock" -> showLowStock(req, resp);
            case "expired"  -> showExpired(req, resp);
            case "nearexpiry" -> showNearExpiry(req, resp);
            case "batches"  -> showBatches(req, resp);
            default         -> listMedicines(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");

        switch (action) {
            case "add"    -> insertMedicine(req, resp);
            case "edit"   -> updateMedicine(req, resp);
            case "delete" -> deleteMedicine(req, resp);
            default       -> resp.sendRedirect(req.getContextPath() + "/medicines");
        }
    }

    // ─────────────────────────────────────────────────────────
    // GET handlers
    // ─────────────────────────────────────────────────────────

    private void listMedicines(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int page     = parsePage(req);
        int total    = medicineDAO.count();
        int offset   = (page - 1) * PAGE_SIZE;
        int pages    = AppUtil.totalPages(total, PAGE_SIZE);

        req.setAttribute("medicines",   medicineDAO.findAllPaginated(offset, PAGE_SIZE));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages",  pages);
        req.setAttribute("totalCount",  total);
        req.setAttribute("pageInfo",    AppUtil.paginationInfo(page, PAGE_SIZE, total));
        req.setAttribute("categories",  categoryDAO.findAll());
        req.setAttribute("suppliers",   supplierDAO.findAll());
        forward(req, resp, "medicines/list.jsp");
    }

    private void showAddForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("categories", categoryDAO.findAll());
        req.setAttribute("suppliers",  supplierDAO.findAll());
        forward(req, resp, "medicines/add.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/medicines"); return; }

        Medicine medicine = medicineDAO.findById(id);
        if (medicine == null) {
            req.getSession().setAttribute("errorMsg", "Medicine not found.");
            resp.sendRedirect(req.getContextPath() + "/medicines");
            return;
        }
        req.setAttribute("medicine",   medicine);
        req.setAttribute("categories", categoryDAO.findAll());
        req.setAttribute("suppliers",  supplierDAO.findAll());
        forward(req, resp, "medicines/edit.jsp");
    }

    private void viewMedicine(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/medicines"); return; }

        Medicine medicine = medicineDAO.findById(id);
        if (medicine == null) {
            req.getSession().setAttribute("errorMsg", "Medicine not found.");
            resp.sendRedirect(req.getContextPath() + "/medicines");
            return;
        }
        req.setAttribute("medicine", medicine);
        req.setAttribute("batches",  medicineDAO.findBatchesByMedicine(id));
        forward(req, resp, "medicines/view.jsp");
    }

    private void searchMedicines(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword    = AppUtil.safe(req.getParameter("q"));
        String filterType = AppUtil.safe(req.getParameter("filter"));
        String filterVal  = AppUtil.safe(req.getParameter("filterVal"));

        List<Medicine> medicines;
        if (!keyword.isEmpty()) {
            medicines = medicineDAO.search(keyword);
        } else if ("category".equals(filterType) && !filterVal.isEmpty()) {
            try { medicines = medicineDAO.findByCategory(Integer.parseInt(filterVal)); }
            catch (NumberFormatException e) { medicines = new ArrayList<>(); }
        } else if ("supplier".equals(filterType) && !filterVal.isEmpty()) {
            try { medicines = medicineDAO.findBySupplier(Integer.parseInt(filterVal)); }
            catch (NumberFormatException e) { medicines = new ArrayList<>(); }
        } else {
            medicines = medicineDAO.findAll();
        }

        req.setAttribute("medicines",  medicines);
        req.setAttribute("keyword",    keyword);
        req.setAttribute("categories", categoryDAO.findAll());
        req.setAttribute("suppliers",  supplierDAO.findAll());
        forward(req, resp, "medicines/list.jsp");
    }

    private void showLowStock(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("medicines", medicineDAO.findLowStock());
        req.setAttribute("viewTitle", "Low-Stock Medicines");
        forward(req, resp, "medicines/lowstock.jsp");
    }

    private void showExpired(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("batches",   medicineDAO.findExpiredBatches());
        req.setAttribute("viewTitle", "Expired Medicines");
        forward(req, resp, "medicines/expired.jsp");
    }

    private void showNearExpiry(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("batches",   medicineDAO.findNearExpiryBatches(30));
        req.setAttribute("viewTitle", "Near-Expiry Medicines (within 30 days)");
        forward(req, resp, "medicines/expired.jsp");
    }

    private void showBatches(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/medicines"); return; }
        req.setAttribute("medicine", medicineDAO.findById(id));
        req.setAttribute("batches",  medicineDAO.findBatchesByMedicine(id));
        forward(req, resp, "medicines/batches.jsp");
    }

    // ─────────────────────────────────────────────────────────
    // POST handlers
    // ─────────────────────────────────────────────────────────

    private void insertMedicine(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Medicine m = buildMedicineFromRequest(req);
        List<String> errors = validateMedicine(m);

        if (!errors.isEmpty()) {
            req.setAttribute("errors",     errors);
            req.setAttribute("medicine",   m);
            req.setAttribute("categories", categoryDAO.findAll());
            req.setAttribute("suppliers",  supplierDAO.findAll());
            forward(req, resp, "medicines/add.jsp");
            return;
        }

        int id = medicineDAO.insert(m);
        if (id > 0) {
            req.getSession().setAttribute("successMsg", "Medicine '" + m.getName() + "' added successfully.");
        } else {
            req.getSession().setAttribute("errorMsg", "Failed to add medicine. Please try again.");
        }
        resp.sendRedirect(req.getContextPath() + "/medicines");
    }

    private void updateMedicine(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/medicines"); return; }

        Medicine m = buildMedicineFromRequest(req);
        m.setId(id);
        List<String> errors = validateMedicine(m);

        if (!errors.isEmpty()) {
            req.setAttribute("errors",     errors);
            req.setAttribute("medicine",   m);
            req.setAttribute("categories", categoryDAO.findAll());
            req.setAttribute("suppliers",  supplierDAO.findAll());
            forward(req, resp, "medicines/edit.jsp");
            return;
        }

        boolean ok = medicineDAO.update(m);
        if (ok) {
            req.getSession().setAttribute("successMsg", "Medicine updated successfully.");
        } else {
            req.getSession().setAttribute("errorMsg", "Failed to update medicine.");
        }
        resp.sendRedirect(req.getContextPath() + "/medicines");
    }

    private void deleteMedicine(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/medicines"); return; }

        String result = medicineDAO.delete(id);
        switch (result) {
            case "ok"             -> req.getSession().setAttribute("successMsg", "Medicine deleted successfully.");
            case "has_sales"      -> req.getSession().setAttribute("errorMsg",
                                     "Cannot delete — this medicine has existing sales records.");
            case "has_purchases"  -> req.getSession().setAttribute("errorMsg",
                                     "Cannot delete — this medicine has existing purchase records.");
            default               -> req.getSession().setAttribute("errorMsg", "Failed to delete medicine.");
        }
        resp.sendRedirect(req.getContextPath() + "/medicines");
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private Medicine buildMedicineFromRequest(HttpServletRequest req) {
        Medicine m = new Medicine();
        m.setName(ValidationUtil.getParam(req, "name"));
        m.setManufacturer(ValidationUtil.getParam(req, "manufacturer"));
        m.setDescription(ValidationUtil.getParam(req, "description"));
        m.setUnit(ValidationUtil.getParam(req, "unit"));

        String catId = ValidationUtil.getParam(req, "categoryId");
        String supId = ValidationUtil.getParam(req, "supplierId");
        String minSt = ValidationUtil.getParam(req, "minStockLevel");

        m.setCategoryId(catId.isEmpty() ? 0 : ValidationUtil.parseNonNegativeInt(catId));
        m.setSupplierId(supId.isEmpty() ? 0 : ValidationUtil.parseNonNegativeInt(supId));
        m.setMinStockLevel(minSt.isEmpty() ? 10 : Math.max(0, ValidationUtil.parseNonNegativeInt(minSt)));
        return m;
    }

    private List<String> validateMedicine(Medicine m) {
        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(m.getName(), "Medicine name")) != null)     errors.add(e);
        if (m.getCategoryId() <= 0) errors.add("Please select a valid category.");
        if (m.getSupplierId() <= 0) errors.add("Please select a valid supplier.");
        if (m.getMinStockLevel() < 0) errors.add("Minimum stock level cannot be negative.");
        if (m.getName() != null && m.getName().length() > 200) errors.add("Medicine name must not exceed 200 characters.");
        return errors;
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String viewPath)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(req, resp);
    }

    private int parsePage(HttpServletRequest req) {
        try {
            int p = Integer.parseInt(AppUtil.safe(req.getParameter("page")));
            return Math.max(1, p);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
