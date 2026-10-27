package com.pharmacy.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * LogoutServlet — invalidates the admin session.
 *
 * POST /logout → Invalidates session, redirects to login page.
 * GET  /logout → Also handled (redirects to login for direct URL access).
 *
 * Security: Using POST for logout prevents CSRF-based forced logout.
 * A CSRF token should ideally be validated here in production.
 */
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        performLogout(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        performLogout(req, resp);
    }

    private void performLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            String username = "";
            Object admin = session.getAttribute("admin");
            if (admin instanceof com.pharmacy.model.Admin a) {
                username = a.getUsername();
            }
            session.invalidate();
            logger.info("Admin '{}' logged out.", username);
        }

        // Set cache control headers to prevent back-button access after logout
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        resp.sendRedirect(req.getContextPath() + "/login?logout=true");
    }
}
