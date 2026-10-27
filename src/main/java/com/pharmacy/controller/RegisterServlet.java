package com.pharmacy.controller;

import com.pharmacy.dao.AdminDAO;
import com.pharmacy.model.Admin;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RegisterServlet — handles new administrator account creation.
 *
 * GET  /register → Displays the registration page (register.jsp)
 * POST /register → Validates registration inputs, hashes password, inserts new admin, redirects to login
 */
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // If already logged in, redirect to dashboard
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("admin") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }

        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String username        = ValidationUtil.getParam(req, "username");
        String fullName        = ValidationUtil.getParam(req, "fullName");
        String email           = ValidationUtil.getParam(req, "email");
        String phone           = ValidationUtil.getParam(req, "phone");
        String password        = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        List<String> errors = new ArrayList<>();

        // Validation
        if (username.isEmpty()) {
            errors.add("Username is required.");
        } else if (!username.matches("^[a-zA-Z0-9_]{3,30}$")) {
            errors.add("Username must be between 3 and 30 characters and contain only letters, numbers, or underscores.");
        } else if (adminDAO.isUsernameTaken(username)) {
            errors.add("Username '" + username + "' is already taken. Please pick another.");
        }

        if (fullName.isEmpty()) {
            errors.add("Full Name is required.");
        }

        if (!email.isEmpty()) {
            String emailErr = ValidationUtil.validateEmail(email);
            if (emailErr != null) errors.add(emailErr);
        }

        if (password == null || password.length() < 6) {
            errors.add("Password must be at least 6 characters long.");
        } else if (!password.equals(confirmPassword)) {
            errors.add("Password confirmation does not match.");
        }

        if (!errors.isEmpty()) {
            req.setAttribute("errors", errors);
            req.setAttribute("username", username);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        // Build Admin and persist
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setFullName(fullName);
        admin.setEmail(email);
        admin.setPhone(phone);

        int newId = adminDAO.registerAdmin(admin, password);
        if (newId > 0) {
            logger.info("New admin account registered successfully: id={}, username={}", newId, username);
            req.getSession().setAttribute("successMsg", "Account created successfully for " + username + "! You can now log in.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } else {
            req.setAttribute("errorMsg", "Failed to create account. Please ensure database is connected.");
            req.setAttribute("username", username);
            req.setAttribute("fullName", fullName);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        }
    }
}
