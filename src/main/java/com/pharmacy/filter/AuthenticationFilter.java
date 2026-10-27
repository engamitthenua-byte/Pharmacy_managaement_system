package com.pharmacy.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AuthenticationFilter — protects all pages except login.
 *
 * How it works:
 *  1. Every HTTP request passes through this filter.
 *  2. Public paths (login page, static assets) are allowed without a session.
 *  3. All other paths require an active session with the "admin" attribute.
 *  4. Unauthenticated users are redirected to the login page.
 *
 * This is a Servlet Filter registered via web.xml (not @WebFilter annotation)
 * to give explicit control over filter ordering.
 */
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void init(FilterConfig config) throws ServletException {
        logger.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest)  request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String uri         = req.getRequestURI();
        String contextPath = req.getContextPath();

        // ── Paths allowed without authentication ──────────────────────────
        // Login page, logout, static assets (CSS, JS, images)
        if (isPublicPath(uri, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        // ── Check session ─────────────────────────────────────────────────
        HttpSession session = req.getSession(false);  // false = don't create a new session
        boolean authenticated = (session != null) && (session.getAttribute("admin") != null);

        if (authenticated) {
            // Session is valid — continue to the requested resource
            chain.doFilter(request, response);
        } else {
            // Not authenticated — redirect to login page with a message
            logger.debug("Unauthenticated request to {} — redirecting to login", uri);
            req.getSession().setAttribute("errorMsg", "Please log in to access this page.");
            resp.sendRedirect(contextPath + "/login");
        }
    }

    @Override
    public void destroy() {
        logger.info("AuthenticationFilter destroyed.");
    }

    /**
     * Returns true for paths that do not require authentication.
     */
    private boolean isPublicPath(String uri, String contextPath) {
        // Strip the context path for cleaner comparison
        String path = uri.substring(contextPath.length());

        return path.equals("/")
            || path.equals("/login")
            || path.equals("/register")
            || path.equals("/index.jsp")
            || path.startsWith("/css/")
            || path.startsWith("/js/")
            || path.startsWith("/assets/")
            || path.startsWith("/error/")
            || path.endsWith(".css")
            || path.endsWith(".js")
            || path.endsWith(".png")
            || path.endsWith(".jpg")
            || path.endsWith(".ico");
    }
}
