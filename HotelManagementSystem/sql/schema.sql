-- ============================================================
-- Grand Vista Hotel Management System
-- Database Schema (SQLite Compatible)
-- ============================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS billing_items;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS alacarte_menu;

-- ============================================================
-- ROOMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS rooms (
    room_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    room_number    TEXT NOT NULL UNIQUE,
    room_type      TEXT NOT NULL,          -- STANDARD, DELUXE, SUITE, PENTHOUSE
    price_per_night REAL NOT NULL,
    floor          INTEGER NOT NULL,
    capacity       INTEGER NOT NULL DEFAULT 2,
    description    TEXT,
    amenities      TEXT,                   -- JSON string of amenities
    is_available   INTEGER NOT NULL DEFAULT 1,  -- 1 = available, 0 = occupied
    created_at     TEXT DEFAULT (datetime('now'))
);

-- ============================================================
-- CUSTOMERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    customer_id    INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name      TEXT NOT NULL,
    email          TEXT NOT NULL UNIQUE,
    phone          TEXT NOT NULL,
    id_proof_type  TEXT NOT NULL,          -- PASSPORT, AADHAR, DRIVING_LICENSE
    id_proof_number TEXT NOT NULL,
    address        TEXT,
    nationality    TEXT DEFAULT 'Indian',
    created_at     TEXT DEFAULT (datetime('now'))
);

-- ============================================================
-- BOOKINGS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS bookings (
    booking_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_ref    TEXT NOT NULL UNIQUE,   -- e.g., GV-2024-001
    customer_id    INTEGER NOT NULL,
    room_id        INTEGER NOT NULL,
    check_in_date  TEXT NOT NULL,
    check_out_date TEXT NOT NULL,
    num_guests     INTEGER NOT NULL DEFAULT 1,
    total_amount   REAL,
    status         TEXT NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, CHECKED_OUT, CANCELLED
    special_requests TEXT,
    created_at     TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- ============================================================
-- BILLING ITEMS TABLE (À La Carte + Services)
-- ============================================================
CREATE TABLE IF NOT EXISTS billing_items (
    item_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id     INTEGER NOT NULL,
    item_name      TEXT NOT NULL,
    category       TEXT NOT NULL,          -- FOOD, BEVERAGE, SPA, LAUNDRY, MISC
    quantity       INTEGER NOT NULL DEFAULT 1,
    unit_price     REAL NOT NULL,
    total_price    REAL NOT NULL,
    ordered_at     TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);

-- ============================================================
-- A LA CARTE MENU TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS alacarte_menu (
    menu_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    item_name      TEXT NOT NULL,
    category       TEXT NOT NULL,
    description    TEXT,
    price          REAL NOT NULL,
    is_available   INTEGER DEFAULT 1
);

-- ============================================================
-- REVIEWS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS reviews (
    review_id      INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id     INTEGER NOT NULL,
    customer_id    INTEGER NOT NULL,
    rating         INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title          TEXT,
    comments       TEXT,
    category       TEXT DEFAULT 'GENERAL',
    created_at     TEXT DEFAULT (datetime('now')),
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- Insert Rooms
INSERT OR IGNORE INTO rooms (room_number, room_type, price_per_night, floor, capacity, description, amenities) VALUES
('101', 'STANDARD', 3500.00, 1, 2, 'Cozy standard room with garden view', 'WiFi,AC,TV,Mini-Bar'),
('102', 'STANDARD', 3500.00, 1, 2, 'Comfortable room with courtyard view', 'WiFi,AC,TV'),
('103', 'STANDARD', 3500.00, 1, 2, 'Standard room ideal for business travelers', 'WiFi,AC,TV,Work-Desk'),
('201', 'DELUXE', 6500.00, 2, 2, 'Spacious deluxe room with pool view', 'WiFi,AC,TV,Mini-Bar,Bathtub,Balcony'),
('202', 'DELUXE', 6500.00, 2, 3, 'Deluxe triple room for families', 'WiFi,AC,TV,Mini-Bar,Bathtub'),
('203', 'DELUXE', 7000.00, 2, 2, 'Premium deluxe with city skyline view', 'WiFi,AC,TV,Mini-Bar,Jacuzzi,Balcony'),
('301', 'SUITE', 12000.00, 3, 4, 'Luxurious suite with separate living area', 'WiFi,AC,TV,Mini-Bar,Jacuzzi,Butler,Balcony'),
('302', 'SUITE', 12000.00, 3, 4, 'Executive suite with panoramic ocean view', 'WiFi,AC,TV,Mini-Bar,Jacuzzi,Butler,Balcony'),
('401', 'PENTHOUSE', 25000.00, 4, 6, 'The Grand Penthouse — unparalleled luxury', 'WiFi,AC,TV,Mini-Bar,Private-Pool,Chef,Butler,Terrace');

-- Insert À La Carte Menu
INSERT OR IGNORE INTO alacarte_menu (item_name, category, description, price) VALUES
('Continental Breakfast', 'FOOD', 'Croissants, eggs, fresh juice, coffee', 850.00),
('Full Indian Breakfast', 'FOOD', 'Idli, dosa, poha, chai', 650.00),
('Club Sandwich', 'FOOD', 'Triple-decker with fries', 550.00),
('Butter Chicken', 'FOOD', 'Classic Punjabi curry with naan', 750.00),
('Grilled Sea Bass', 'FOOD', 'Pan-seared with lemon butter sauce', 1200.00),
('Chocolate Lava Cake', 'FOOD', 'Warm cake with vanilla ice cream', 450.00),
('Fresh Lime Soda', 'BEVERAGE', 'Sparkling or still', 180.00),
('Freshly Brewed Coffee', 'BEVERAGE', 'Arabica single origin', 250.00),
('Imported Wine (Bottle)', 'BEVERAGE', 'Red or white, house selection', 3500.00),
('Premium Whiskey', 'BEVERAGE', 'Single malt, 30ml', 850.00),
('Swedish Massage (60min)', 'SPA', 'Full-body relaxation massage', 2500.00),
('Aromatherapy Session', 'SPA', 'Essential oils, 45 minutes', 1800.00),
('Laundry (per piece)', 'LAUNDRY', 'Express 4-hour service', 200.00),
('Dry Cleaning (per piece)', 'LAUNDRY', 'Premium garment care', 350.00),
('Airport Transfer', 'MISC', 'One-way sedan service', 1500.00),
('City Tour (Half Day)', 'MISC', 'AC vehicle with guide', 2000.00);
