package com.example.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session != null) {
            Object userObj = session.getAttribute("user");
            session.invalidate();
            logger.log(Level.INFO, "User {0} has logged out.",
                    (userObj != null ? userObj.toString() : "unknown"));
        } else {
            logger.warning("Logout attempted with no active session.");
        }

        response.sendRedirect("login");
    }
}
