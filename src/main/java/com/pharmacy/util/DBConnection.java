package com.pharmacy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection — Database Connection Utility
 * ------------------------------------------
 * Provides a shared, centrally-configured JDBC connection.
 *
 * Design: Simple JDBC connection factory using application.properties
 * for credentials. Each DAO opens and closes its own connection using
 * try-with-resources to ensure proper resource cleanup.
 *
 * For production use, this could be upgraded to a connection pool
 * (e.g., HikariCP), but for this academic project simple connections
 * are used to demonstrate JDBC clearly.
 *
 * Usage:
 *   try (Connection conn = DBConnection.getConnection()) {
 *       // use connection
 *   }
 */
public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    // Loaded once at class initialization from application.properties
    private static final String DB_URL;
    private static final String DB_USERNAME;
    private static final String DB_PASSWORD;
    private static final String DB_DRIVER;

    // Static initializer — loads properties when the class is first used
    static {
        Properties props = new Properties();
        try (InputStream is = DBConnection.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new RuntimeException(
                    "application.properties not found on the classpath. " +
                    "Make sure src/main/resources/application.properties exists."
                );
            }
            props.load(is);
            logger.info("application.properties loaded successfully.");
        } catch (IOException e) {
            logger.error("Failed to load application.properties", e);
            throw new RuntimeException("Could not load database configuration.", e);
        }

        DB_URL      = props.getProperty("db.url");
        DB_USERNAME = props.getProperty("db.username");
        DB_PASSWORD = props.getProperty("db.password");
        DB_DRIVER   = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        // Validate required properties
        if (DB_URL == null || DB_URL.isBlank()) {
            throw new RuntimeException("db.url is missing from application.properties");
        }
        if (DB_USERNAME == null || DB_USERNAME.isBlank()) {
            throw new RuntimeException("db.username is missing from application.properties");
        }

        // Register the JDBC driver
        try {
            Class.forName(DB_DRIVER);
            logger.info("MySQL JDBC Driver registered: {}", DB_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("MySQL JDBC Driver not found. Add mysql-connector-j to pom.xml", e);
            throw new RuntimeException("JDBC Driver not found: " + DB_DRIVER, e);
        }
    }

    // Private constructor — this is a static utility class
    private DBConnection() {}

    /**
     * Returns a new JDBC Connection to the pharmacy_management database.
     * The caller MUST close this connection (best done with try-with-resources).
     *
     * @return an open {@link Connection}
     * @throws SQLException if the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        logger.debug("Database connection opened.");
        return conn;
    }

    /**
     * Convenience method: silently closes a Connection.
     * Prefer try-with-resources; use this only when necessary.
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Database connection closed.");
            } catch (SQLException e) {
                logger.warn("Error closing database connection", e);
            }
        }
    }

    /**
     * Tests database connectivity. Called on application startup.
     * @return true if a connection can be established
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            logger.error("Database connection test FAILED: {}", e.getMessage());
            return false;
        }
    }
}
