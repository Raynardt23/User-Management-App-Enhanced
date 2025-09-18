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

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ProfileServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = (UserDAO) getServletContext().getAttribute("userDAO");

        if (userDAO == null) {
            logger.severe("Failed to get UserDAO from application context");
        } else {
            logger.info("ProfileServlet initialized successfully with shared UserDAO");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            logger.warning("Unauthorized access attempt to profile page");
            response.sendRedirect("login");
            return;
        }

        User user = (User) session.getAttribute("user");

        if ("admin".equals(user.getRole())) {
            request.setAttribute("allUsers", userDAO.getAllUsers());
        }

        logger.log(Level.INFO, "User {0} accessed profile page", user.getUsername());
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            logger.warning("Unauthorized profile update attempt");
            response.sendRedirect("login");
            return;
        }

        User sessionUser = (User) session.getAttribute("user");
        String action = request.getParameter("action");
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");

        if ("changeRole".equals(action)) {
            handleRoleChange(request, response, sessionUser);
            return;
        }

        // Input validation logging
        if (!SecurityUtil.isValidEmail(email)) {
            logger.warning("Invalid email format submitted by user: " + sessionUser.getUsername());
            request.setAttribute("error", "Invalid email format.");
            forwardToProfile(request, response, sessionUser);
            return;
        }

        if (!SecurityUtil.isValidName(firstName)) {
            logger.warning("Invalid first name submitted by user: " + sessionUser.getUsername());
            request.setAttribute("error", "Invalid first name.");
            forwardToProfile(request, response, sessionUser);
            return;
        }

        if (!SecurityUtil.isValidName(lastName)) {
            logger.warning("Invalid last name submitted by user: " + sessionUser.getUsername());
            request.setAttribute("error", "Invalid last name.");
            forwardToProfile(request, response, sessionUser);
            return;
        }

        // Email uniqueness check
        User existingUser = userDAO.findByEmail(email);
        if (existingUser != null && existingUser.getId() != sessionUser.getId()) {
            logger.warning("Email conflict: user " + sessionUser.getUsername()
                    + " tried to use email already taken by " + existingUser.getUsername());
            request.setAttribute("error", "Email already taken by another user.");
            forwardToProfile(request, response, sessionUser);
            return;
        }

        // Update user
        User userToUpdate = userDAO.findByUsername(sessionUser.getUsername());
        userToUpdate.setEmail(email);
        userToUpdate.setFirstName(firstName);
        userToUpdate.setLastName(lastName);

        if ("admin".equals(sessionUser.getRole())) {
            String role = request.getParameter("role");
            if (role != null && ("admin".equals(role) || "user".equals(role))) {
                userToUpdate.setRole(role);
            }
        }

        if (userDAO.updateUser(userToUpdate)) {
            sessionUser.setEmail(email);
            sessionUser.setFirstName(firstName);
            sessionUser.setLastName(lastName);
            session.setAttribute("user", sessionUser);

            logger.log(Level.INFO, "User {0} successfully updated profile", sessionUser.getUsername());
            request.setAttribute("success", "Profile updated successfully.");
        } else {
            logger.log(Level.SEVERE, "Failed to update profile for user {0}", sessionUser.getUsername());
            request.setAttribute("error", "Failed to update profile.");
        }

        forwardToProfile(request, response, sessionUser);
    }

    private void forwardToProfile(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException {
        if ("admin".equals(user.getRole())) {
            request.setAttribute("allUsers", userDAO.getAllUsers());
        }
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    private void handleRoleChange(HttpServletRequest request, HttpServletResponse response, User sessionUser)
            throws ServletException, IOException {
        if (!"admin".equals(sessionUser.getRole())) {
            logger.warning("Unauthorized role change attempt by user " + sessionUser.getUsername());
            request.setAttribute("error", "Access denied. Admin privileges required.");
            forwardToProfile(request, response, sessionUser);
            return;
        }

        String userIdStr = request.getParameter("userId");
        String newRole = request.getParameter("role");

        if (userIdStr != null && newRole != null && ("admin".equals(newRole) || "user".equals(newRole))) {
            try {
                int userId = Integer.parseInt(userIdStr);
                User userToUpdate = userDAO.findById(userId);

                if (userToUpdate != null && userToUpdate.getId() != sessionUser.getId()) {
                    userToUpdate.setRole(newRole);
                    if (userDAO.updateUser(userToUpdate)) {
                        logger.log(Level.INFO, "Admin {0} changed role of user {1} to {2}",
                                new Object[]{sessionUser.getUsername(), userToUpdate.getUsername(), newRole});
                        request.setAttribute("success", "User role updated successfully.");
                    } else {
                        logger.log(Level.SEVERE, "Failed to update role for user {0}", userToUpdate.getUsername());
                        request.setAttribute("error", "Failed to update user role.");
                    }
                } else {
                    logger.warning("Admin " + sessionUser.getUsername() + " attempted to change their own role");
                    request.setAttribute("error", "Cannot change your own role.");
                }
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid user ID provided by admin {0}", sessionUser.getUsername());
                request.setAttribute("error", "Invalid user ID.");
            }
        } else {
            logger.log(Level.WARNING, "Invalid role change request by admin {0}", sessionUser.getUsername());
            request.setAttribute("error", "Invalid role change request.");
        }

        forwardToProfile(request, response, sessionUser);
    }
}
