package com.example.filter;

import com.example.model.User;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    private static final Logger logger = Logger.getLogger(AuthorizationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {

            FileHandler fileHandler = new FileHandler("user-management.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // avoid console duplication
        } catch (IOException e) {
            throw new ServletException("Failed to initialize AuthorizationFilter logger", e);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String path = httpRequest.getRequestURI();

        // Allow login/register without restriction
        if (path.endsWith("/") || path.endsWith("login") || path.contains("login.jsp") || path.endsWith("register") || path.contains("resources")) {
            chain.doFilter(request, response);
            return;
        }

        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            // No user logged in
            logger.warning("Unauthorized access attempt to " + path
                    + " from IP: " + httpRequest.getRemoteAddr()
                    + " (no active session)");
            httpRequest.setAttribute("error", "Please login to access this page.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(httpRequest, httpResponse);
            return;
        }

        // Admin-only restriction
        if (path.startsWith("/admin") && !"admin".equals(user.getRole())) {
            logger.warning("Unauthorized access attempt to " + path
                    + " by user: " + user.getUsername()
                    + " from IP: " + httpRequest.getRemoteAddr());
            httpRequest.setAttribute("error", "Access denied. Admin privileges required.");
            httpRequest.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(httpRequest, httpResponse);
            return;
        }

        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed for logger
    }
}
