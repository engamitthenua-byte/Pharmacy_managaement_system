package com.pharmacy.controller;

import com.pharmacy.dao.CategoryDAO;
import com.pharmacy.model.Category;
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
 * CategoryServlet — CRUD for medicine categories.
 */
public class CategoryServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CategoryServlet.class);
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> showAddForm(req, resp);
            case "edit"   -> showEditForm(req, resp);
            case "delete" -> deleteCategory(req, resp);
            case "search" -> searchCategories(req, resp);
            default       -> listCategories(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "list");
        switch (action) {
            case "add"    -> insertCategory(req, resp);
            case "edit"   -> updateCategory(req, resp);
            case "delete" -> deleteCategory(req, resp);
            default       -> resp.sendRedirect(req.getContextPath() + "/categories");
        }
    }

    private void listCategories(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setAttribute("categories", categoryDAO.findAll());
        forward(req, resp, "categories/list.jsp");
    }

    private void showAddForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        forward(req, resp, "categories/add.jsp");
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/categories"); return; }
        Category cat = categoryDAO.findById(id);
        if (cat == null) {
            req.getSession().setAttribute("errorMsg", "Category not found.");
            resp.sendRedirect(req.getContextPath() + "/categories");
            return;
        }
        req.setAttribute("category", cat);
        forward(req, resp, "categories/edit.jsp");
    }

    private void searchCategories(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String keyword = AppUtil.safe(req.getParameter("q"));
        req.setAttribute("categories", categoryDAO.search(keyword));
        req.setAttribute("keyword", keyword);
        forward(req, resp, "categories/list.jsp");
    }

    private void insertCategory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = ValidationUtil.getParam(req, "name");
        String desc = ValidationUtil.getParam(req, "description");

        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(name, "Category name")) != null) errors.add(e);
        if (name.length() > 100) errors.add("Category name must not exceed 100 characters.");
        if (!errors.isEmpty() || categoryDAO.nameExists(name, -1)) {
            if (categoryDAO.nameExists(name, -1)) errors.add("A category named '" + name + "' already exists.");
            req.setAttribute("errors", errors);
            req.setAttribute("category", new Category(name, desc));
            forward(req, resp, "categories/add.jsp");
            return;
        }

        int id = categoryDAO.insert(new Category(name, desc));
        if (id > 0) req.getSession().setAttribute("successMsg", "Category '" + name + "' added.");
        else        req.getSession().setAttribute("errorMsg", "Failed to add category. Name may already exist.");
        resp.sendRedirect(req.getContextPath() + "/categories");
    }

    private void updateCategory(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/categories"); return; }

        String name = ValidationUtil.getParam(req, "name");
        String desc = ValidationUtil.getParam(req, "description");

        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(name, "Category name")) != null) errors.add(e);
        if (categoryDAO.nameExists(name, id)) errors.add("A category named '" + name + "' already exists.");

        if (!errors.isEmpty()) {
            Category cat = new Category(name, desc);
            cat.setId(id);
            req.setAttribute("errors", errors);
            req.setAttribute("category", cat);
            forward(req, resp, "categories/edit.jsp");
            return;
        }

        Category cat = new Category(name, desc);
        cat.setId(id);
        boolean ok = categoryDAO.update(cat);
        if (ok) req.getSession().setAttribute("successMsg", "Category updated successfully.");
        else    req.getSession().setAttribute("errorMsg", "Failed to update category.");
        resp.sendRedirect(req.getContextPath() + "/categories");
    }

    private void deleteCategory(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int id = ValidationUtil.parseId(req, "id");
        if (id < 0) { resp.sendRedirect(req.getContextPath() + "/categories"); return; }
        String result = categoryDAO.delete(id);
        switch (result) {
            case "ok"           -> req.getSession().setAttribute("successMsg", "Category deleted.");
            case "has_medicines"-> req.getSession().setAttribute("errorMsg",
                                   "Cannot delete — medicines are assigned to this category.");
            default             -> req.getSession().setAttribute("errorMsg", "Failed to delete category.");
        }
        resp.sendRedirect(req.getContextPath() + "/categories");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
