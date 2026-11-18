package com.couriertracking.util;

import com.couriertracking.exception.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes database schemas and injects rich seed data.
 * Works seamlessly across both MySQL 8 and SQLite engines.
 */
public final class DatabaseInitializer {
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());

    private DatabaseInitializer() {}

    public static void initialize() {
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            boolean isSQLite = DatabaseConnection.getInstance().getActiveDatabaseType().toLowerCase().contains("sqlite");

            createTables(conn, isSQLite);
            seedBranchesIfEmpty(conn);
            seedParcelsIfEmpty(conn);
            LOGGER.info("Database initialization completed successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void createTables(Connection conn, boolean isSQLite) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            if (isSQLite) {
                stmt.execute("CREATE TABLE IF NOT EXISTS branches (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "branch_code TEXT NOT NULL UNIQUE, " +
                        "branch_name TEXT NOT NULL, " +
                        "city TEXT NOT NULL, " +
                        "state TEXT NOT NULL, " +
                        "contact_phone TEXT NOT NULL, " +
                        "address TEXT NOT NULL, " +
                        "is_active INTEGER DEFAULT 1, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS parcels (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "consignment_no TEXT NOT NULL UNIQUE, " +
                        "sender_name TEXT NOT NULL, " +
                        "sender_phone TEXT NOT NULL, " +
                        "sender_address TEXT NOT NULL, " +
                        "receiver_name TEXT NOT NULL, " +
                        "receiver_phone TEXT NOT NULL, " +
                        "receiver_address TEXT NOT NULL, " +
                        "source_branch_id INTEGER NOT NULL, " +
                        "destination_branch_id INTEGER NOT NULL, " +
                        "parcel_type TEXT NOT NULL, " +
                        "weight REAL NOT NULL, " +
                        "description TEXT, " +
                        "delivery_charge REAL NOT NULL, " +
                        "status TEXT NOT NULL DEFAULT 'BOOKED', " +
                        "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS tracking_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "parcel_id INTEGER NOT NULL, " +
                        "consignment_no TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "location TEXT NOT NULL, " +
                        "remarks TEXT, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            } else {
                // MySQL 8 Syntax
                stmt.execute("CREATE TABLE IF NOT EXISTS branches (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "branch_code VARCHAR(20) NOT NULL UNIQUE, " +
                        "branch_name VARCHAR(100) NOT NULL, " +
                        "city VARCHAR(50) NOT NULL, " +
                        "state VARCHAR(50) NOT NULL, " +
                        "contact_phone VARCHAR(20) NOT NULL, " +
                        "address VARCHAR(255) NOT NULL, " +
                        "is_active BOOLEAN DEFAULT TRUE, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS parcels (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "consignment_no VARCHAR(30) NOT NULL UNIQUE, " +
                        "sender_name VARCHAR(100) NOT NULL, " +
                        "sender_phone VARCHAR(20) NOT NULL, " +
                        "sender_address VARCHAR(255) NOT NULL, " +
                        "receiver_name VARCHAR(100) NOT NULL, " +
                        "receiver_phone VARCHAR(20) NOT NULL, " +
                        "receiver_address VARCHAR(255) NOT NULL, " +
                        "source_branch_id INT NOT NULL, " +
                        "destination_branch_id INT NOT NULL, " +
                        "parcel_type VARCHAR(20) NOT NULL, " +
                        "weight DECIMAL(8, 2) NOT NULL, " +
                        "description VARCHAR(255), " +
                        "delivery_charge DECIMAL(10, 2) NOT NULL, " +
                        "status VARCHAR(30) NOT NULL DEFAULT 'BOOKED', " +
                        "booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS tracking_history (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "parcel_id INT NOT NULL, " +
                        "consignment_no VARCHAR(30) NOT NULL, " +
                        "status VARCHAR(30) NOT NULL, " +
                        "location VARCHAR(100) NOT NULL, " +
                        "remarks VARCHAR(255), " +
                        "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            }
        }
    }

    private static void seedBranchesIfEmpty(Connection conn) throws Exception {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM branches")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO branches (branch_code, branch_name, city, state, contact_phone, address, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    String[][] branches = {
                        {"BR-DEL-01", "Delhi Central Logistics Hub", "New Delhi", "Delhi", "9811223344", "Connaught Place Sector 4, New Delhi"},
                        {"BR-MUM-01", "Mumbai Port Gateway Hub", "Mumbai", "Maharashtra", "9822334455", "Andheri East Cargo Complex, Mumbai"},
                        {"BR-BLR-01", "Bengaluru Silicon Depot", "Bengaluru", "Karnataka", "9833445566", "Electronic City Phase 1, Bengaluru"},
                        {"BR-HYD-01", "Hyderabad Regional Center", "Hyderabad", "Telangana", "9844556677", "Hitec City Logistic Park, Hyderabad"},
                        {"BR-KOL-01", "Kolkata Eastern Hub", "Kolkata", "West Bengal", "9855667788", "Salt Lake Sector 5, Kolkata"},
                        {"BR-CHE-01", "Chennai Coastal Depot", "Chennai", "Tamil Nadu", "9866778899", "Guindy Industrial Estate, Chennai"}
                    };

                    for (String[] b : branches) {
                        ps.setString(1, b[0]);
                        ps.setString(2, b[1]);
                        ps.setString(3, b[2]);
                        ps.setString(4, b[3]);
                        ps.setString(5, b[4]);
                        ps.setString(6, b[5]);
                        ps.setInt(7, 1);
                        ps.executeUpdate();
                    }
                }
                LOGGER.info("Seeded initial courier branches.");
            }
        }
    }

    private static void seedParcelsIfEmpty(Connection conn) throws Exception {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM parcels")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String parcelSql = "INSERT INTO parcels (consignment_no, sender_name, sender_phone, sender_address, " +
                        "receiver_name, receiver_phone, receiver_address, source_branch_id, destination_branch_id, " +
                        "parcel_type, weight, description, delivery_charge, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                String[][] parcels = {
                    {"CRX202611180001", "Rahul Sharma", "9876543210", "Flat 402, Green Avenue, Delhi", "Priya Patel", "9876501234", "12 Palm Beach Road, Mumbai", "1", "2", "PACKAGE", "2.5", "Electronics & Smart Watch", "225.0", "IN_TRANSIT"},
                    {"CRX202611180002", "Amit Verma", "9123456780", "B-14 Civil Lines, Delhi", "Kavita Nair", "9234567890", "45 Indiranagar 100ft Rd, Bengaluru", "1", "3", "DOCUMENT", "0.5", "University Marksheets & Certificate", "60.0", "DELIVERED"},
                    {"CRX202611180003", "Vikram Singh", "9345678901", "Sector 17, Chandigarh", "Suresh Rao", "9456789012", "Gachibowli Financial Dist, Hyderabad", "1", "4", "PACKAGE", "5.0", "Apparel & Festive Gifts", "350.0", "OUT_FOR_DELIVERY"},
                    {"CRX202611180004", "Sneha Roy", "9567890123", "Park Street 3rd Floor, Kolkata", "Ananya Gupta", "9678901234", "Anna Nagar West, Chennai", "5", "6", "PACKAGE", "1.2", "Handicrafts Sample", "160.0", "BOOKED"},
                    {"CRX202611180005", "Rajesh Kothari", "9789012345", "Bandra West, Mumbai", "Deepak Joshi", "9890123456", "Koramangala 4th Block, Bengaluru", "2", "3", "DOCUMENT", "0.2", "Legal Contract Agreement", "50.0", "PICKED_UP"}
                };

                for (String[] p : parcels) {
                    int parcelId;
                    try (PreparedStatement ps = conn.prepareStatement(parcelSql, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, p[0]);
                        ps.setString(2, p[1]);
                        ps.setString(3, p[2]);
                        ps.setString(4, p[3]);
                        ps.setString(5, p[4]);
                        ps.setString(6, p[5]);
                        ps.setString(7, p[6]);
                        ps.setInt(8, Integer.parseInt(p[7]));
                        ps.setInt(9, Integer.parseInt(p[8]));
                        ps.setString(10, p[9]);
                        ps.setDouble(11, Double.parseDouble(p[10]));
                        ps.setString(11 + 1, p[11]);
                        ps.setDouble(13, Double.parseDouble(p[12]));
                        ps.setString(14, p[13]);
                        ps.executeUpdate();

                        try (ResultSet gk = ps.getGeneratedKeys()) {
                            if (gk.next()) {
                                parcelId = gk.getInt(1);
                            } else {
                                parcelId = 1;
                            }
                        }
                    }

                    seedTrackingForParcel(conn, parcelId, p[0], p[13]);
                }
                LOGGER.info("Seeded initial parcels and tracking timelines.");
            }
        }
    }

    private static void seedTrackingForParcel(Connection conn, int parcelId, String cno, String currentStatus) throws Exception {
        String trackSql = "INSERT INTO tracking_history (parcel_id, consignment_no, status, location, remarks) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(trackSql)) {
            // Initial booked event
            addEvent(ps, parcelId, cno, "BOOKED", "Origin Branch Hub", "Parcel registered and label generated");

            if ("PICKED_UP".equals(currentStatus) || "IN_TRANSIT".equals(currentStatus) || 
                "OUT_FOR_DELIVERY".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                addEvent(ps, parcelId, cno, "PICKED_UP", "Origin Branch Hub", "Picked up by dispatch agent");
            }
            if ("IN_TRANSIT".equals(currentStatus) || "OUT_FOR_DELIVERY".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                addEvent(ps, parcelId, cno, "IN_TRANSIT", "National Highway Cargo Corridor", "Moving on express logistics container");
            }
            if ("OUT_FOR_DELIVERY".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                addEvent(ps, parcelId, cno, "ARRIVED_AT_DESTINATION", "Destination City Hub", "Received at destination sorting facility");
                addEvent(ps, parcelId, cno, "OUT_FOR_DELIVERY", "Local Delivery Zone", "Handed over to delivery executive for delivery");
            }
            if ("DELIVERED".equals(currentStatus)) {
                addEvent(ps, parcelId, cno, "DELIVERED", "Recipient Doorstep", "Delivered successfully. Recipient signature obtained.");
            }
        }
    }

    private static void addEvent(PreparedStatement ps, int parcelId, String cno, String status, String loc, String rem) throws Exception {
        ps.setInt(1, parcelId);
        ps.setString(2, cno);
        ps.setString(3, status);
        ps.setString(4, loc);
        ps.setString(5, rem);
        ps.executeUpdate();
    }
}
