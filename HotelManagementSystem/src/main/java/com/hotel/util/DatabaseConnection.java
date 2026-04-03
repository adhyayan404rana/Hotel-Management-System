package com.hotel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseConnection — Singleton JDBC connection manager.
 *
 * Uses SQLite for zero-config portability.
 * Applies the Singleton pattern to prevent multiple connections.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String DB_URL = "jdbc:sqlite:grandvista.db";

    // Singleton instance — one connection for the entire app
    private static DatabaseConnection instance;
    private Connection connection;

    /**
     * Private constructor — loads SQLite driver and initialises schema.
     */
    private DatabaseConnection() {
        try {
            // Explicitly load the SQLite driver (important for fat-JAR scenarios)
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);

            // Enable WAL mode for better concurrent read performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA foreign_keys = ON;");
            }

            initializeSchema();
            LOGGER.info("Database connection established successfully.");

        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQLite JDBC driver not found!", e);
            throw new RuntimeException("SQLite driver missing. Check pom.xml.", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database.", e);
            throw new RuntimeException("Database connection failed.", e);
        }
    }

    /**
     * Returns the single shared DatabaseConnection instance.
     * Thread-safe via synchronized keyword (Week 4 concept).
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || !instance.isConnectionValid()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Returns the raw JDBC Connection object for DAO use.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Checks if the current connection is still alive.
     */
    private boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reads and executes the SQL schema file bundled in resources.
     * Creates tables and seeds default data on first run.
     */
    private void initializeSchema() {
        try {
            InputStream schemaStream = getClass().getResourceAsStream("/com/hotel/sql/schema.sql");
            if (schemaStream == null) {
                // Fallback: create tables inline if resource missing
                createTablesInline();
                return;
            }

            StringBuilder sql = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(schemaStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip single-line comments
                    if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                        sql.append(line).append("\n");
                    }
                }
            }

            // Execute each statement separated by semicolons
            String[] statements = sql.toString().split(";");
            try (Statement stmt = connection.createStatement()) {
                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty()) {
                        stmt.execute(trimmed);
                    }
                }
            }
            LOGGER.info("Database schema initialised successfully.");

        } catch (IOException | SQLException e) {
            LOGGER.log(Level.WARNING, "Schema from resource failed, using inline creation.", e);
            createTablesInline();
        }
    }

    /**
     * Inline table creation as a fallback if the .sql resource isn't found.
     * This ensures the app always works even without the resource file.
     */
    private void createTablesInline() {
        String[] ddl = {
            // Rooms
            "CREATE TABLE IF NOT EXISTS rooms (" +
            "  room_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  room_number TEXT NOT NULL UNIQUE," +
            "  room_type TEXT NOT NULL," +
            "  price_per_night REAL NOT NULL," +
            "  floor INTEGER NOT NULL," +
            "  capacity INTEGER NOT NULL DEFAULT 2," +
            "  description TEXT," +
            "  amenities TEXT," +
            "  is_available INTEGER NOT NULL DEFAULT 1," +
            "  created_at TEXT DEFAULT (datetime('now'))" +
            ")",

            // Customers
            "CREATE TABLE IF NOT EXISTS customers (" +
            "  customer_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  full_name TEXT NOT NULL," +
            "  email TEXT NOT NULL UNIQUE," +
            "  phone TEXT NOT NULL," +
            "  id_proof_type TEXT NOT NULL," +
            "  id_proof_number TEXT NOT NULL," +
            "  address TEXT," +
            "  nationality TEXT DEFAULT 'Indian'," +
            "  created_at TEXT DEFAULT (datetime('now'))" +
            ")",

            // Bookings
            "CREATE TABLE IF NOT EXISTS bookings (" +
            "  booking_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  booking_ref TEXT NOT NULL UNIQUE," +
            "  customer_id INTEGER NOT NULL," +
            "  room_id INTEGER NOT NULL," +
            "  check_in_date TEXT NOT NULL," +
            "  check_out_date TEXT NOT NULL," +
            "  num_guests INTEGER NOT NULL DEFAULT 1," +
            "  total_amount REAL," +
            "  status TEXT NOT NULL DEFAULT 'ACTIVE'," +
            "  special_requests TEXT," +
            "  created_at TEXT DEFAULT (datetime('now'))," +
            "  FOREIGN KEY (customer_id) REFERENCES customers(customer_id)," +
            "  FOREIGN KEY (room_id) REFERENCES rooms(room_id)" +
            ")",

            // Billing Items
            "CREATE TABLE IF NOT EXISTS billing_items (" +
            "  item_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  booking_id INTEGER NOT NULL," +
            "  item_name TEXT NOT NULL," +
            "  category TEXT NOT NULL," +
            "  quantity INTEGER NOT NULL DEFAULT 1," +
            "  unit_price REAL NOT NULL," +
            "  total_price REAL NOT NULL," +
            "  ordered_at TEXT DEFAULT (datetime('now'))," +
            "  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)" +
            ")",

            // À La Carte Menu
            "CREATE TABLE IF NOT EXISTS alacarte_menu (" +
            "  menu_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  item_name TEXT NOT NULL," +
            "  category TEXT NOT NULL," +
            "  description TEXT," +
            "  price REAL NOT NULL," +
            "  is_available INTEGER DEFAULT 1" +
            ")",

            // Reviews
            "CREATE TABLE IF NOT EXISTS reviews (" +
            "  review_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "  booking_id INTEGER NOT NULL," +
            "  customer_id INTEGER NOT NULL," +
            "  rating INTEGER NOT NULL," +
            "  title TEXT," +
            "  comments TEXT," +
            "  category TEXT DEFAULT 'GENERAL'," +
            "  created_at TEXT DEFAULT (datetime('now'))," +
            "  FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)," +
            "  FOREIGN KEY (customer_id) REFERENCES customers(customer_id)" +
            ")"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : ddl) {
                stmt.execute(sql);
            }
            seedDefaultData(stmt);
            LOGGER.info("Tables created inline successfully.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Inline table creation failed.", e);
            throw new RuntimeException("Could not initialise database schema.", e);
        }
    }

    /**
     * Seeds initial room and menu data so the app is demo-ready on first launch.
     */
    private void seedDefaultData(Statement stmt) throws SQLException {
        // Check if rooms already exist
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
        if (rs.next() && rs.getInt(1) > 0) return;  // Already seeded

        String[] roomInserts = {
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('101','STANDARD',3500,1,2,'Garden view standard room','WiFi,AC,TV,Mini-Bar')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('102','STANDARD',3500,1,2,'Courtyard view standard room','WiFi,AC,TV')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('103','STANDARD',3500,1,2,'Business traveler room','WiFi,AC,TV,Work-Desk')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('201','DELUXE',6500,2,2,'Pool view deluxe room','WiFi,AC,TV,Mini-Bar,Bathtub,Balcony')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('202','DELUXE',6500,2,3,'Family deluxe triple','WiFi,AC,TV,Mini-Bar,Bathtub')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('203','DELUXE',7000,2,2,'City skyline deluxe','WiFi,AC,TV,Mini-Bar,Jacuzzi,Balcony')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('301','SUITE',12000,3,4,'Luxury suite with living area','WiFi,AC,TV,Mini-Bar,Jacuzzi,Butler,Balcony')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('302','SUITE',12000,3,4,'Panoramic ocean suite','WiFi,AC,TV,Mini-Bar,Jacuzzi,Butler,Balcony')",
            "INSERT OR IGNORE INTO rooms (room_number,room_type,price_per_night,floor,capacity,description,amenities) VALUES ('401','PENTHOUSE',25000,4,6,'The Grand Penthouse','WiFi,AC,TV,Mini-Bar,Private-Pool,Chef,Butler,Terrace')"
        };

        String[] menuInserts = {
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Continental Breakfast','FOOD','Croissants, eggs, fresh juice',850)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Full Indian Breakfast','FOOD','Idli, dosa, poha, chai',650)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Club Sandwich','FOOD','Triple-decker with fries',550)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Butter Chicken','FOOD','Classic Punjabi curry with naan',750)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Grilled Sea Bass','FOOD','Pan-seared with lemon butter',1200)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Freshly Brewed Coffee','BEVERAGE','Arabica single origin',250)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Imported Wine (Bottle)','BEVERAGE','Red or white house selection',3500)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Swedish Massage (60min)','SPA','Full-body relaxation massage',2500)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Laundry (per piece)','LAUNDRY','Express 4-hour service',200)",
            "INSERT OR IGNORE INTO alacarte_menu (item_name,category,description,price) VALUES ('Airport Transfer','MISC','One-way sedan service',1500)"
        };

        for (String sql : roomInserts) stmt.execute(sql);
        for (String sql : menuInserts) stmt.execute(sql);

        LOGGER.info("Default seed data inserted.");
    }

    /**
     * Closes the database connection cleanly on app shutdown.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing connection.", e);
        }
    }
}
