package com.example.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/error-handler")
public class ErrorHandlerServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(ErrorHandlerServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forwardToErrorPage(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forwardToErrorPage(request, response);
    }

    private void forwardToErrorPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get error details set by the container
        Throwable throwable = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String requestUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        if (requestUri == null) {
            requestUri = "Unknown";
        }

        if (throwable != null) {
            logger.log(Level.SEVERE, "Unhandled exception at " + requestUri, throwable);
        } else if (statusCode != null) {
            logger.log(Level.WARNING, "Error with status code {0} at {1}", new Object[]{statusCode, requestUri});
        } else {
            logger.warning("Unknown error occurred at " + requestUri);
        }

        // Forward to JSP inside WEB-INF/views
        request.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(request, response);
    }
}
