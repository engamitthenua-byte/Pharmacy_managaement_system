package com.pharmacy.controller;

import com.pharmacy.dao.AdminDAO;
import com.pharmacy.model.Admin;
import com.pharmacy.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * LoginServlet — handles admin authentication.
 *
 * GET  /login  → Shows the login form (login.jsp)
 * POST /login  → Validates credentials, creates session, redirects to dashboard
 */
public class LoginServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
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

        // Show login page
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String username = ValidationUtil.getParam(req, "username");
        String password = req.getParameter("password");

        if (username.isEmpty() || password == null || password.isEmpty()) {
            req.setAttribute("errorMsg", "Username and password are required.");
            req.setAttribute("username", username);
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        try {
            // Validate credentials against database
            Admin admin = adminDAO.validateLogin(username, password);

            if (admin != null) {
                // Invalidate old session first (prevents session fixation)
                HttpSession oldSession = req.getSession(false);
                if (oldSession != null) oldSession.invalidate();

                // Create a brand new session
                HttpSession session = req.getSession(true);
                session.setAttribute("admin", admin);
                session.setAttribute("adminName", admin.getFullName());
                session.setAttribute("adminId", admin.getId());
                session.setMaxInactiveInterval(1800); // 30 minutes

                logger.info("Admin '{}' logged in successfully.", admin.getUsername());
                resp.sendRedirect(req.getContextPath() + "/dashboard");
            } else {
                req.setAttribute("errorMsg", "Invalid username or password. Please verify credentials or create an account.");
                req.setAttribute("username", ValidationUtil.escapeHtml(username));
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            logger.error("Database connection failure during login", e);
            req.setAttribute("errorMsg", "Database connection error: " + e.getMessage() + 
                ". Please check MySQL credentials in src/main/resources/application.properties.");
            req.setAttribute("username", ValidationUtil.escapeHtml(username));
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            req.setAttribute("errorMsg", "Login error: " + e.getMessage());
            req.setAttribute("username", ValidationUtil.escapeHtml(username));
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }
}
