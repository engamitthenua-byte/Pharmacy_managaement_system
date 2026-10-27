package com.pharmacy.controller;

import com.pharmacy.dao.ReportsDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * DashboardServlet — loads summary data for the main dashboard.
 *
 * GET /dashboard → Fetches summary counts and redirects to dashboard.jsp
 */
public class DashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
    private final ReportsDAO reportsDAO = new ReportsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            // Load all dashboard summary data from the database
            Map<String, Object> summary = reportsDAO.getDashboardSummary();
            req.setAttribute("summary", summary);
        } catch (Exception e) {
            logger.error("Error loading dashboard data", e);
            req.setAttribute("summary", Map.of()); // empty map on error
        }

        req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
    }
}
