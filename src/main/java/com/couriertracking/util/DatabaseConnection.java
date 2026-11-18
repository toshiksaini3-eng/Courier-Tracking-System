package com.couriertracking.util;

import com.couriertracking.exception.DatabaseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton Database Connection Manager.
 * Demonstrates the Singleton Design Pattern and robust JDBC Connection lifecycle.
 * Primary: MySQL 8 (via PreparedStatement & DriverManager).
 * Automatic Fallback: Local Embedded SQLite for 100% guaranteed college viva demonstration.
 */
public class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static DatabaseConnection instance;

    private String dbHost = "localhost";
    private int dbPort = 3306;
    private String dbName = "courier_db";
    private String dbUser = "root";
    private String dbPassword = "";
    private String mySqlUrl;

    private boolean fallbackEnabled = true;
    private String sqliteUrl = "jdbc:sqlite:courier_db.sqlite";

    private String activeDatabaseType = "Unknown";
    private String activeConnectionInfo = "";
    private Connection currentConnection;

    private DatabaseConnection() {
        loadProperties();
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private void loadProperties() {
        Properties props = new Properties();
        boolean loaded = false;

        // Try loading from external config file first
        File externalConfig = new File("config/db.properties");
        if (externalConfig.exists()) {
            try (InputStream in = new FileInputStream(externalConfig)) {
                props.load(in);
                loaded = true;
            } catch (Exception e) {
                LOGGER.warning("Could not read external config/db.properties: " + e.getMessage());
            }
        }

        // Fallback to classpath resource
        if (!loaded) {
            try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    loaded = true;
                }
            } catch (Exception e) {
                LOGGER.warning("Could not read classpath db.properties: " + e.getMessage());
            }
        }

        if (loaded) {
            dbHost = props.getProperty("db.host", dbHost);
            try {
                dbPort = Integer.parseInt(props.getProperty("db.port", String.valueOf(dbPort)));
            } catch (NumberFormatException ignored) {}
            dbName = props.getProperty("db.name", dbName);
            dbUser = props.getProperty("db.user", dbUser);
            dbPassword = props.getProperty("db.password", dbPassword);
            mySqlUrl = props.getProperty("db.url");
            fallbackEnabled = Boolean.parseBoolean(props.getProperty("db.fallback.enabled", "true"));
            sqliteUrl = props.getProperty("db.fallback.url", sqliteUrl);
        }

        if (mySqlUrl == null || mySqlUrl.isEmpty()) {
            mySqlUrl = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    dbHost, dbPort, dbName);
        }
    }

    /**
     * Obtains an active SQL connection.
     * Reuses open connection or creates a new one if closed.
     */
    public synchronized Connection getConnection() throws DatabaseException {
        try {
            if (currentConnection != null && !currentConnection.isClosed() && currentConnection.isValid(2)) {
                return currentConnection;
            }
        } catch (SQLException ignored) {
            // Reconnect needed
        }

        // 1. Try MySQL Connection First
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            currentConnection = DriverManager.getConnection(mySqlUrl, dbUser, dbPassword);
            activeDatabaseType = "MySQL 8";
            activeConnectionInfo = dbHost + ":" + dbPort + "/" + dbName;
            LOGGER.info("Successfully connected to MySQL database: " + activeConnectionInfo);
            return currentConnection;
        } catch (Exception mySqlEx) {
            LOGGER.log(Level.WARNING, "MySQL connection failed: " + mySqlEx.getMessage() + ". Attempting fallback...");

            // 2. Fallback to SQLite if MySQL is offline or not installed
            if (fallbackEnabled) {
                try {
                    Class.forName("org.sqlite.JDBC");
                    currentConnection = DriverManager.getConnection(sqliteUrl);
                    activeDatabaseType = "SQLite (Local Demo Mode)";
                    activeConnectionInfo = "courier_db.sqlite";
                    LOGGER.info("Connected to local embedded database: " + activeConnectionInfo);
                    return currentConnection;
                } catch (Exception sqliteEx) {
                    throw new DatabaseException("Failed to connect to both MySQL (" + mySqlEx.getMessage() + 
                            ") and SQLite Fallback (" + sqliteEx.getMessage() + ").", sqliteEx);
                }
            } else {
                throw new DatabaseException("Failed to connect to MySQL database at " + mySqlUrl + 
                        ". Ensure MySQL Server is running.", mySqlEx);
            }
        }
    }

    public synchronized void updateCredentials(String host, int port, String database, String user, String password) throws DatabaseException {
        this.dbHost = host;
        this.dbPort = port;
        this.dbName = database;
        this.dbUser = user;
        this.dbPassword = password;
        this.mySqlUrl = String.format("jdbc:mysql://%s:%d/%s?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                host, port, database);

        closeConnection();
        getConnection(); // test and establish
    }

    public synchronized void closeConnection() {
        if (currentConnection != null) {
            try {
                if (!currentConnection.isClosed()) {
                    currentConnection.close();
                }
            } catch (SQLException e) {
                LOGGER.warning("Error closing connection: " + e.getMessage());
            } finally {
                currentConnection = null;
            }
        }
    }

    public String getActiveDatabaseType() {
        return activeDatabaseType;
    }

    public String getActiveConnectionInfo() {
        return activeConnectionInfo;
    }

    public boolean isMySQL() {
        return "MySQL 8".equalsIgnoreCase(activeDatabaseType);
    }

    public String getDbHost() {
        return dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public String getDbUser() {
        return dbUser;
    }
}
