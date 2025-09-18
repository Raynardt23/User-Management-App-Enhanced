package com.example.controller;

import com.example.dao.UserDAO;
import com.example.model.User;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger("user-management"); // Use global logger
    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = (UserDAO) getServletContext().getAttribute("userDAO");
        if (userDAO == null) {
            logger.severe("Failed to get UserDAO from application context");
        } else {
            logger.info("AdminServlet initialized successfully with shared UserDAO");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            logger.warning("Unauthorized GET attempt to /admin (no user session)");
            request.setAttribute("error", "You must be logged in to access admin pages.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        if (!"admin".equals(user.getRole())) {
            logger.warning("Failed access attempt by user: " + user.getUsername());
            request.setAttribute("error", "Access denied. Admin privileges required.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        String action = request.getPathInfo();
        if (action != null) {
            switch (action) {
                case "/delete":
                    deleteUser(request, response);
                    break;
                case "/edit":
                    showEditForm(request, response);
                    break;
                default:
                    listUsers(request, response);
                    break;
            }
        } else {
            listUsers(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            logger.warning("Unauthorized POST attempt to /admin (no user session)");
            request.setAttribute("error", "You must be logged in to perform this action.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        if (!"admin".equals(user.getRole())) {
            logger.warning("Failed POST access attempt by user: " + user.getUsername());
            request.setAttribute("error", "Access denied. Admin privileges required.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
            return;
        }

        String action = request.getPathInfo();
        if (action != null) {
            switch (action) {
                case "/update":
                    updateUser(request, response);
                    break;
                default:
                    listUsers(request, response);
                    break;
            }
        } else {
            listUsers(request, response);
        }
    }

    private void listUsers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("allUsers", userDAO.getAllUsers());
            request.getRequestDispatcher("/WEB-INF/views/users.jsp").forward(request, response);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while listing users", e);
            request.setAttribute("error", "Unable to load users.");
            request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
        }
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userId = request.getParameter("id");
        try {
            int id = Integer.parseInt(userId);
            User userToEdit = userDAO.findById(id);
            if (userToEdit != null) {
                request.setAttribute("editUser", userToEdit);
                request.getRequestDispatcher("/WEB-INF/views/editUser.jsp").forward(request, response);
                return;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error in showEditForm with ID: " + userId, e);
        }
        request.setAttribute("error", "User not found.");
        listUsers(request, response);
    }

    private void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userId = request.getParameter("id");
        logger.info("Updating user with ID: " + userId);

        listUsers(request, response);
    }

    private void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String userId = request.getParameter("id");
        try {
            int id = Integer.parseInt(userId);
            User currentUser = (User) request.getSession().getAttribute("user");

            if (currentUser.getId() == id) {
                logger.warning("User attempted to delete their own account: " + currentUser.getUsername());
                request.setAttribute("error", "You cannot delete your own account.");
            } else if (userDAO.deleteUser(id)) {
                logger.info("Deleted user with ID: " + id);
                request.setAttribute("success", "User deleted successfully.");
            } else {
                logger.warning("Failed to delete user with ID: " + id);
                request.setAttribute("error", "Failed to delete user.");
            }
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid user ID format: " + userId, e);
            request.setAttribute("error", "Invalid user ID.");
        }
        listUsers(request, response);
    }
}
