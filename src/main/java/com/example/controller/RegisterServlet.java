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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(RegisterServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = (UserDAO) getServletContext().getAttribute("userDAO");

        if (userDAO == null) {
            logger.severe("UserDAO not initialized in ServletContext. Did you set it in AppContextListener?");
            throw new ServletException("UserDAO not initialized in ServletContext.");
        } else {
            logger.info("RegisterServlet initialized successfully with shared UserDAO");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");

        logger.log(Level.INFO, "Registration attempt: username={0}, email={1}", new Object[]{username, email});

        // ===== Server-side validation =====
        if (!SecurityUtil.isValidUsername(username)) {
            logger.warning("Invalid username during registration: " + username);
            request.setAttribute("error", "Invalid username. Use 3-20 alphanumeric characters.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (!SecurityUtil.isValidPassword(password)) {
            logger.warning("Invalid password submitted by user: " + username);
            request.setAttribute("error", "Password must be at least 6 characters long.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (!SecurityUtil.isValidEmail(email)) {
            logger.warning("Invalid email during registration: " + email);
            request.setAttribute("error", "Invalid email format.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (!SecurityUtil.isValidName(firstName)) {
            logger.warning("Invalid first name during registration: " + firstName);
            request.setAttribute("error", "Invalid first name. Use 2-50 letters, spaces, or hyphens.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (!SecurityUtil.isValidName(lastName)) {
            logger.warning("Invalid last name during registration: " + lastName);
            request.setAttribute("error", "Invalid last name. Use 2-50 letters, spaces, or hyphens.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        // ===== Check if username/email already exists =====
        if (userDAO.findByUsername(username) != null) {
            logger.warning("Registration failed: username already exists -> " + username);
            request.setAttribute("error", "Username already exists.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        if (userDAO.findByEmail(email) != null) {
            logger.warning("Registration failed: email already exists -> " + email);
            request.setAttribute("error", "Email already exists.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
            return;
        }

        // ===== Hash password before saving =====
        String hashedPassword = SecurityUtil.hashPassword(password);

        // ===== Create new user with default "user" role =====
        User user = new User();
        user.setUsername(username);
        user.setPassword(hashedPassword);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole("user");

        boolean success = userDAO.createUser(user);

        if (success) {
            logger.log(Level.INFO, "User registered successfully: {0}", username);
            HttpSession session = request.getSession();
            session.setAttribute("success", "Registration successful. Please login.");
            response.sendRedirect("login");
        } else {
            logger.log(Level.SEVERE, "Registration failed for user: {0}", username);
            request.setAttribute("error", "Registration failed. Please try again.");
            request.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(request, response);
        }
    }
}
