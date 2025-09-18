package com.example.listener;

import com.example.dao.DatabaseConnection;
import com.example.dao.UserDAO;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(AppContextListener.class.getName());
    private FileHandler fileHandler; // keep reference to close later

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {

            String logsDir = "C:" + File.separator + "Projects" + File.separator + "UserManagementApp Enhanced" + File.separator + "logs";
            new File(logsDir).mkdirs(); // create folder if it doesn't exist

            fileHandler = new FileHandler(logsDir + File.separator + "user-management.log", true); // append mode
            fileHandler.setFormatter(new SimpleFormatter());
            Logger fileLogger = Logger.getLogger("user-management");
            fileLogger.addHandler(fileHandler);

            fileLogger.info("Logging started!");

            // Init DB and DAO
            DatabaseConnection.createDatabaseIfNotExists();
            UserDAO userDAO = new UserDAO();
            userDAO.createUsersTableIfNotExists();
            userDAO.createDefaultAdmin();

            // Share DAO via application context
            sce.getServletContext().setAttribute("userDAO", userDAO);

            fileLogger.info("AppContext initialized successfully.");

        } catch (IOException e) {
            throw new RuntimeException("Failed to setup logger FileHandler", e);
        } catch (Exception e) {
            logger.severe("AppContext initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize AppContext", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("ServletContext destroyed. Cleaning up resources...");

        // Close the FileHandler to release the log file
        if (fileHandler != null) {
            fileHandler.close();
        }

        // Deregister JDBC drivers to avoid memory leaks
        Enumeration<java.sql.Driver> drivers = java.sql.DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            try {
                java.sql.DriverManager.deregisterDriver(driver);
                logger.info("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                logger.severe("Error deregistering driver: " + driver + " - " + e.getMessage());
            }
        }
    }
}
