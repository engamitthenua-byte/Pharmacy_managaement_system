package com.pharmacy.controller;

import com.pharmacy.dao.SupplierDAO;
import com.pharmacy.model.Supplier;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** SupplierServlet — CRUD for suppliers. */
public class SupplierServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SupplierServlet.class);
    private final SupplierDAO supplierDAO = new SupplierDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> forward(req, resp, "suppliers/add.jsp");
            case "edit"   -> showEditForm(req, resp);
            case "delete" -> deleteSupplier(req, resp);
            case "search" -> { req.setAttribute("suppliers", supplierDAO.search(AppUtil.safe(req.getParameter("q")))); req.setAttribute("keyword", req.getParameter("q")); forward(req, resp, "suppliers/list.jsp"); }
            default       -> { req.setAttribute("suppliers", supplierDAO.findAll()); forward(req, resp, "suppliers/list.jsp"); }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> insertSupplier(req, resp);
            case "edit"   -> updateSupplier(req, resp);
            case "delete" -> deleteSupplier(req, resp);
            default       -> resp.sendRedirect(req.getContextPath() + "/suppliers");
        }
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        Supplier supplier = (id > 0) ? supplierDAO.findById(id) : null;
        if (supplier == null) { req.getSession().setAttribute("errorMsg", "Supplier not found."); resp.sendRedirect(req.getContextPath() + "/suppliers"); return; }
        req.setAttribute("supplier", supplier);
        forward(req, resp, "suppliers/edit.jsp");
    }

    private void insertSupplier(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Supplier s = buildFromRequest(req);
        List<String> errors = validate(s);
        if (!errors.isEmpty()) { req.setAttribute("errors", errors); req.setAttribute("supplier", s); forward(req, resp, "suppliers/add.jsp"); return; }
        int id = supplierDAO.insert(s);
        if (id > 0) req.getSession().setAttribute("successMsg", "Supplier '" + s.getName() + "' added.");
        else        req.getSession().setAttribute("errorMsg", "Failed to add supplier.");
        resp.sendRedirect(req.getContextPath() + "/suppliers");
    }

    private void updateSupplier(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/suppliers"); return; }
        Supplier s = buildFromRequest(req);
        s.setId(id);
        List<String> errors = validate(s);
        if (!errors.isEmpty()) { req.setAttribute("errors", errors); req.setAttribute("supplier", s); forward(req, resp, "suppliers/edit.jsp"); return; }
        boolean ok = supplierDAO.update(s);
        req.getSession().setAttribute(ok ? "successMsg" : "errorMsg", ok ? "Supplier updated." : "Failed to update supplier.");
        resp.sendRedirect(req.getContextPath() + "/suppliers");
    }

    private void deleteSupplier(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/suppliers"); return; }
        String result = supplierDAO.delete(id);
        switch (result) {
            case "ok"           -> req.getSession().setAttribute("successMsg", "Supplier deleted.");
            case "has_purchases"-> req.getSession().setAttribute("errorMsg", "Cannot delete — supplier has purchase records.");
            case "has_medicines"-> req.getSession().setAttribute("errorMsg", "Cannot delete — supplier has medicines assigned.");
            default             -> req.getSession().setAttribute("errorMsg", "Failed to delete supplier.");
        }
        resp.sendRedirect(req.getContextPath() + "/suppliers");
    }

    private Supplier buildFromRequest(HttpServletRequest req) {
        return new Supplier(
            ValidationUtil.getParam(req, "name"),
            ValidationUtil.getParam(req, "contactPerson"),
            ValidationUtil.getParam(req, "phone"),
            ValidationUtil.getParam(req, "email"),
            ValidationUtil.getParam(req, "address")
        );
    }

    private List<String> validate(Supplier s) {
        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(s.getName(), "Supplier name")) != null) errors.add(e);
        if ((e = ValidationUtil.requireNonEmpty(s.getPhone(), "Phone number"))  != null) errors.add(e);
        if ((e = ValidationUtil.validatePhone(s.getPhone()))  != null) errors.add(e);
        if ((e = ValidationUtil.validateEmail(s.getEmail()))  != null) errors.add(e);
        return errors;
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
