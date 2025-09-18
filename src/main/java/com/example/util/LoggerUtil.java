package com.example.util;

import java.io.IOException;
import java.util.logging.*;

public class LoggerUtil {

    private static final Logger logger = Logger.getLogger("UserManagementLogger");
    private static FileHandler fileHandler;

    static {
        try {
            // Logs will go to logs/app.log
            fileHandler = new FileHandler("logs/app.log", true); // append = true
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

            // Optional: disable console logging if you only want file logs
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler h : handlers) {
                if (h instanceof ConsoleHandler) {
                    rootLogger.removeHandler(h);
                }
            }

        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
