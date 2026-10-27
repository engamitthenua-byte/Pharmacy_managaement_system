package com.pharmacy.controller;

import com.pharmacy.dao.AdminDAO;
import com.pharmacy.model.Admin;
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
 * ProfileServlet — allows the logged-in administrator to view & update
 * their account profile information and change their password.
 *
 * GET  /profile              → View profile page
 * POST /profile?action=info  → Update name, email, phone
 * POST /profile?action=pass  → Change password
 */
public class ProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Admin sessionAdmin = (Admin) req.getSession().getAttribute("admin");
        if (sessionAdmin != null) {
            Admin fresh = adminDAO.findById(sessionAdmin.getId());
            if (fresh != null) {
                req.setAttribute("admin", fresh);
                req.getSession().setAttribute("admin", fresh);
            }
        }
        forward(req, resp, "profile/view.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = AppUtil.defaultIfBlank(req.getParameter("action"), "info");

        if ("password".equals(action)) {
            changePassword(req, resp);
        } else {
            updateInfo(req, resp);
        }
    }

    private void updateInfo(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Admin sessionAdmin = (Admin) req.getSession().getAttribute("admin");
        if (sessionAdmin == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String fullName = ValidationUtil.getParam(req, "fullName");
        String email    = ValidationUtil.getParam(req, "email");
        String phone    = ValidationUtil.getParam(req, "phone");

        List<String> errors = new ArrayList<>();
        String e;
        if ((e = ValidationUtil.requireNonEmpty(fullName, "Full name")) != null) errors.add(e);
        if ((e = ValidationUtil.validateEmail(email)) != null) errors.add(e);

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            forward(req, resp, "profile/view.jsp");
            return;
        }

        Admin updated = new Admin();
        updated.setId(sessionAdmin.getId());
        updated.setFullName(fullName);
        updated.setEmail(email);
        updated.setPhone(phone);

        if (adminDAO.updateProfile(updated)) {
            sessionAdmin.setFullName(fullName);
            sessionAdmin.setEmail(email);
            sessionAdmin.setPhone(phone);
            req.getSession().setAttribute("admin", sessionAdmin);
            req.getSession().setAttribute("successMsg", "Profile updated successfully.");
        } else {
            req.getSession().setAttribute("errorMsg", "Failed to update profile.");
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    private void changePassword(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Admin sessionAdmin = (Admin) req.getSession().getAttribute("admin");
        if (sessionAdmin == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String currentPass = req.getParameter("currentPassword");
        String newPass     = req.getParameter("newPassword");
        String confirmPass = req.getParameter("confirmPassword");

        List<String> errors = new ArrayList<>();
        if (currentPass == null || currentPass.isBlank()) {
            errors.add("Please enter your current password.");
        }
        if (newPass == null || newPass.length() < 6) {
            errors.add("New password must be at least 6 characters long.");
        }
        if (newPass != null && !newPass.equals(confirmPass)) {
            errors.add("New passwords do not match.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("passwordErrors", errors);
            forward(req, resp, "profile/view.jsp");
            return;
        }

        boolean success = adminDAO.updatePassword(sessionAdmin.getId(), currentPass, newPass);
        if (success) {
            req.getSession().setAttribute("successMsg", "Password changed successfully.");
        } else {
            req.getSession().setAttribute("errorMsg", "Current password was incorrect. Please try again.");
        }

        resp.sendRedirect(req.getContextPath() + "/profile");
    }

    private void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view).forward(req, resp);
    }
}
