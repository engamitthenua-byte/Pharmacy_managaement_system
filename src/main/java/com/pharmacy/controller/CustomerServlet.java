package com.pharmacy.controller;

import com.pharmacy.dao.CustomerDAO;
import com.pharmacy.model.Customer;
import com.pharmacy.util.AppUtil;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** CustomerServlet — CRUD for customers. */
public class CustomerServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServlet.class);
    private final CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> forward(req, resp, "customers/add.jsp");
            case "edit"   -> showEditForm(req, resp);
            case "delete" -> deleteCustomer(req, resp);
            case "search" -> { req.setAttribute("customers", customerDAO.search(AppUtil.safe(req.getParameter("q")))); req.setAttribute("keyword", req.getParameter("q")); forward(req, resp, "customers/list.jsp"); }
            default       -> { req.setAttribute("customers", customerDAO.findAll()); forward(req, resp, "customers/list.jsp"); }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> insertCustomer(req, resp);
            case "edit"   -> updateCustomer(req, resp);
            case "delete" -> deleteCustomer(req, resp);
            default       -> resp.sendRedirect(req.getContextPath() + "/customers");
        }
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        Customer customer = (id > 0) ? customerDAO.findById(id) : null;
        if (customer == null) { req.getSession().setAttribute("errorMsg", "Customer not found."); resp.sendRedirect(req.getContextPath() + "/customers"); return; }
        req.setAttribute("customer", customer);
        forward(req, resp, "customers/edit.jsp");
    }

    private void insertCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Customer c = buildFromRequest(req);
        List<String> errors = validate(c);
        if (!errors.isEmpty()) { req.setAttribute("errors", errors); req.setAttribute("customer", c); forward(req, resp, "customers/add.jsp"); return; }
        int id = customerDAO.insert(c);
        if (id > 0) req.getSession().setAttribute("successMsg", "Customer '" + c.getName() + "' added.");
        else        req.getSession().setAttribute("errorMsg", "Failed to add customer.");
        resp.sendRedirect(req.getContextPath() + "/customers");
    }

    private void updateCustomer(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/customers"); return; }
        Customer c = buildFromRequest(req);
        c.setId(id);
        List<String> errors = validate(c);
        if (!errors.isEmpty()) { req.setAttribute("errors", errors); req.setAttribute("customer", c); forward(req, resp, "customers/edit.jsp"); return; }
        boolean ok = customerDAO.update(c);
        req.getSession().setAttribute(ok ? "successMsg" : "errorMsg", ok ? "Customer updated." : "Failed to update.");
        resp.sendRedirect(req.getContextPath() + "/customers");
    }

    private void deleteCustomer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/customers"); return; }
        String result = customerDAO.delete(id);
        switch (result) {
            case "ok"       -> req.getSession().setAttribute("successMsg", "Customer deleted.");
            case "has_sales"-> req.getSession().setAttribute("errorMsg", "Cannot delete — customer has sales records.");
            default         -> req.getSession().setAttribute("errorMsg", "Failed to delete customer.");
        }
        resp.sendRedirect(req.getContextPath() + "/customers");
    }

    private Customer buildFromRequest(HttpServletRequest req) {
        return new Customer(
            ValidationUtil.getParam(req, "name"),
            ValidationUtil.getParam(req, "phone"),
            ValidationUtil.getParam(req, "email"),
            ValidationUtil.getParam(req, "address")
        );
    }

    private List<String> validate(Customer c) {
        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(c.getName(), "Customer name")) != null) errors.add(e);
        if ((e = ValidationUtil.validatePhone(c.getPhone()))  != null) errors.add(e);
        if ((e = ValidationUtil.validateEmail(c.getEmail()))  != null) errors.add(e);
        return errors;
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
