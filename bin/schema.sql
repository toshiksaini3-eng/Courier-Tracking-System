-- ==========================================================
-- Courier Tracking System - MySQL Database Schema
-- Technology: MySQL 8 / MariaDB / Compatible SQL
-- ==========================================================

CREATE TABLE IF NOT EXISTS branches (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS parcels (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    consignment_no VARCHAR(30) NOT NULL UNIQUE,
    sender_name VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    sender_address VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    source_branch_id INT NOT NULL,
    destination_branch_id INT NOT NULL,
    parcel_type VARCHAR(20) NOT NULL,
    weight DECIMAL(8, 2) NOT NULL,
    description VARCHAR(255),
    delivery_charge DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'BOOKED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tracking_history (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    parcel_id INT NOT NULL,
    consignment_no VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    location VARCHAR(100) NOT NULL,
    remarks VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
