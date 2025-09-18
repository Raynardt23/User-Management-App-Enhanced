package com.example.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnection {

    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static String DB_NAME;

    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("config.properties not found in classpath!");
            }

            Properties prop = new Properties();
            prop.load(input);

            URL = prop.getProperty("db.url");       // e.g., jdbc:mysql://localhost:3306/
            DB_NAME = prop.getProperty("db.name");  // e.g., userdb
            USER = prop.getProperty("db.user");
            PASSWORD = prop.getProperty("db.password");

            // Load MySQL driver explicitly
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("MySQL JDBC driver loaded successfully.");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("MySQL JDBC driver not found", e);
            }

            // Configure HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL + DB_NAME + "?useSSL=false&serverTimezone=UTC");
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // Optional pool tuning
            config.setMaximumPoolSize(10);   // Max active connections
            config.setMinimumIdle(2);        // Keep a few idle connections
            config.setIdleTimeout(30000);    // 30 seconds
            config.setMaxLifetime(1800000);  // 30 minutes

            dataSource = new HikariDataSource(config);

            System.out.println("HikariCP connection pool initialized.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize DatabaseConnection", e);
        }
    }

    // Get pooled connection
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    // Connect to MySQL server (without DB) - still needed for createDatabaseIfNotExists
    private static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection(URL + "?useSSL=false&serverTimezone=UTC", USER, PASSWORD);
    }

    // Create database if not exists
    public static void createDatabaseIfNotExists() {
        try (Connection conn = getServerConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database checked/created: " + DB_NAME);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create database", e);
        }
    }

    // Close pool on shutdown
    public static void closePool() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("HikariCP pool closed.");
        }
    }
}
