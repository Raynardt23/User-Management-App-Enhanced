package com.example.controller;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.SecurityUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;
    private static final Logger logger = Logger.getLogger("user-management");

    @Override
    public void init() {
        userDAO = (UserDAO) getServletContext().getAttribute("userDAO");

        if (userDAO == null) {
            logger.severe("Failed to get UserDAO from application context");
        } else {
            logger.info("LoginServlet initialized successfully with shared UserDAO");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response); // corrected path
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        logger.info("=== LOGIN ATTEMPT ===");
        logger.info("Username: '" + username + "'");

        User user = null;

        // Step 1: safely fetch user
        try {
            user = userDAO.findByUsername(username);
            logger.info("User found: " + (user != null));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error querying user: " + username, e);
            request.setAttribute("error", "Login error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        // Step 2: check password only if user exists
        boolean passwordMatch = false;
        try {
            if (user != null) {
                passwordMatch = SecurityUtil.checkPassword(password, user.getPassword());
                logger.info("Password check result: " + passwordMatch);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking password for username: " + username, e);
            request.setAttribute("error", "Login error: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        // Step 3: handle login result
        if (user != null && passwordMatch) {
            logger.info("Login SUCCESS for user: " + username);
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            response.sendRedirect("profile");
        } else {
            logger.warning("Login FAILED for username: " + username);
            request.setAttribute("error", "Invalid username or password.");
            request.getRequestDispatcher("/login.jsp").forward(request, response); // corrected path
        }
    }
}
