-- ==========================================================
-- Courier Tracking System - MySQL Database Schema
-- Technology: MySQL 8 / MariaDB / Compatible SQL
-- ==========================================================

CREATE DATABASE IF NOT EXISTS courier_db;
USE courier_db;

-- 1. Branches Table
CREATE TABLE IF NOT EXISTS branches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    branch_code VARCHAR(20) NOT NULL UNIQUE,
    branch_name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Parcels Table
CREATE TABLE IF NOT EXISTS parcels (
    id INT AUTO_INCREMENT PRIMARY KEY,
    consignment_no VARCHAR(30) NOT NULL UNIQUE,
    sender_name VARCHAR(100) NOT NULL,
    sender_phone VARCHAR(20) NOT NULL,
    sender_address VARCHAR(255) NOT NULL,
    receiver_name VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(20) NOT NULL,
    receiver_address VARCHAR(255) NOT NULL,
    source_branch_id INT NOT NULL,
    destination_branch_id INT NOT NULL,
    parcel_type VARCHAR(20) NOT NULL, -- 'DOCUMENT' or 'PACKAGE'
    weight DECIMAL(8, 2) NOT NULL, -- in kg
    description VARCHAR(255),
    delivery_charge DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'BOOKED',
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (source_branch_id) REFERENCES branches(id) ON DELETE RESTRICT,
    FOREIGN KEY (destination_branch_id) REFERENCES branches(id) ON DELETE RESTRICT
);

-- 3. Tracking History Table
CREATE TABLE IF NOT EXISTS tracking_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    parcel_id INT NOT NULL,
    consignment_no VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    location VARCHAR(100) NOT NULL,
    remarks VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parcel_id) REFERENCES parcels(id) ON DELETE CASCADE
);

-- Index for high-performance tracking search
CREATE INDEX idx_consignment_no ON parcels(consignment_no);
CREATE INDEX idx_tracking_consignment ON tracking_history(consignment_no);
CREATE INDEX idx_parcels_status ON parcels(status);

-- ==========================================================
-- SEED DATA: Branches
-- ==========================================================
INSERT INTO branches (branch_code, branch_name, city, state, contact_phone, address, is_active)
VALUES
('BR-DEL-01', 'Delhi Central Logistics Hub', 'New Delhi', 'Delhi', '9811223344', 'Connaught Place Sector 4, New Delhi', TRUE),
('BR-MUM-01', 'Mumbai Port Gateway Hub', 'Mumbai', 'Maharashtra', '9822334455', 'Andheri East Cargo Complex, Mumbai', TRUE),
('BR-BLR-01', 'Bengaluru Silicon Depot', 'Bengaluru', 'Karnataka', '9833445566', 'Electronic City Phase 1, Bengaluru', TRUE),
('BR-HYD-01', 'Hyderabad Regional Center', 'Hyderabad', 'Telangana', '9844556677', 'Hitec City Logistic Park, Hyderabad', TRUE),
('BR-KOL-01', 'Kolkata Eastern Hub', 'Kolkata', 'West Bengal', '9855667788', 'Salt Lake Sector 5, Kolkata', TRUE),
('BR-CHE-01', 'Chennai Coastal Depot', 'Chennai', 'Tamil Nadu', '9866778899', 'Guindy Industrial Estate, Chennai', TRUE)
ON DUPLICATE KEY UPDATE branch_name=VALUES(branch_name);

-- ==========================================================
-- SEED DATA: Demo Parcels & Tracking Timeline
-- ==========================================================
INSERT INTO parcels (consignment_no, sender_name, sender_phone, sender_address, receiver_name, receiver_phone, receiver_address, source_branch_id, destination_branch_id, parcel_type, weight, description, delivery_charge, status, booking_date)
VALUES
('CRX202611180001', 'Rahul Sharma', '9876543210', 'Flat 402, Green Avenue, Delhi', 'Priya Patel', '9876501234', '12 Palm Beach Road, Mumbai', 1, 2, 'PACKAGE', 2.50, 'Electronics & Smart Watch', 225.00, 'IN_TRANSIT', NOW() - INTERVAL 2 DAY),
('CRX202611180002', 'Amit Verma', '9123456780', 'B-14 Civil Lines, Delhi', 'Kavita Nair', '9234567890', '45 Indiranagar 100ft Rd, Bengaluru', 1, 3, 'DOCUMENT', 0.50, 'University Marksheets & Certificate', 60.00, 'DELIVERED', NOW() - INTERVAL 4 DAY),
('CRX202611180003', 'Vikram Singh', '9345678901', 'Sector 17, Chandigarh', 'Suresh Rao', '9456789012', 'Gachibowli Financial Dist, Hyderabad', 1, 4, 'PACKAGE', 5.00, 'Apparel & Festive Gifts', 350.00, 'OUT_FOR_DELIVERY', NOW() - INTERVAL 1 DAY),
('CRX202611180004', 'Sneha Roy', '9567890123', 'Park Street 3rd Floor, Kolkata', 'Ananya Gupta', '9678901234', 'Anna Nagar West, Chennai', 5, 6, 'PACKAGE', 1.20, 'Handicrafts Sample', 160.00, 'BOOKED', NOW()),
('CRX202611180005', 'Rajesh Kothari', '9789012345', 'Bandra West, Mumbai', 'Deepak Joshi', '9890123456', 'Koramangala 4th Block, Bengaluru', 2, 3, 'DOCUMENT', 0.20, 'Legal Contract Agreement', 50.00, 'PICKED_UP', NOW() - INTERVAL 12 HOUR)
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Seed Tracking History for the above demo parcels
INSERT INTO tracking_history (parcel_id, consignment_no, status, location, remarks, updated_at)
VALUES
-- For CRX202611180001
(1, 'CRX202611180001', 'BOOKED', 'Delhi Central Logistics Hub', 'Parcel booked and label generated', NOW() - INTERVAL 48 HOUR),
(1, 'CRX202611180001', 'PICKED_UP', 'Delhi Central Logistics Hub', 'Parcel picked up by carrier agent', NOW() - INTERVAL 36 HOUR),
(1, 'CRX202611180001', 'IN_TRANSIT', 'Highway Hub - Jaipur Transit', 'In transit on Express Truck DL-01-AB-1234', NOW() - INTERVAL 18 HOUR),

-- For CRX202611180002
(2, 'CRX202611180002', 'BOOKED', 'Delhi Central Logistics Hub', 'Urgent documents booked', NOW() - INTERVAL 96 HOUR),
(2, 'CRX202611180002', 'PICKED_UP', 'Delhi Central Logistics Hub', 'Sorted for air freight', NOW() - INTERVAL 84 HOUR),
(2, 'CRX202611180002', 'IN_TRANSIT', 'Bengaluru Airport Cargo', 'Air freight arrived at destination cargo', NOW() - INTERVAL 48 HOUR),
(2, 'CRX202611180002', 'ARRIVED_AT_DESTINATION', 'Bengaluru Silicon Depot', 'Arrived at delivery branch', NOW() - INTERVAL 24 HOUR),
(2, 'CRX202611180002', 'OUT_FOR_DELIVERY', 'Bengaluru Silicon Depot', 'Assigned to delivery agent R. Kumar', NOW() - INTERVAL 6 HOUR),
(2, 'CRX202611180002', 'DELIVERED', 'Indiranagar, Bengaluru', 'Delivered safely. Signed by Kavita Nair', NOW() - INTERVAL 2 HOUR),

-- For CRX202611180003
(3, 'CRX202611180003', 'BOOKED', 'Delhi Central Logistics Hub', 'Consignment booked', NOW() - INTERVAL 28 HOUR),
(3, 'CRX202611180003', 'PICKED_UP', 'Delhi Central Logistics Hub', 'Picked up from warehouse', NOW() - INTERVAL 22 HOUR),
(3, 'CRX202611180003', 'IN_TRANSIT', 'Nagpur Central Hub', 'Mid-way cross dock completed', NOW() - INTERVAL 12 HOUR),
(3, 'CRX202611180003', 'ARRIVED_AT_DESTINATION', 'Hyderabad Regional Center', 'Received at local delivery station', NOW() - INTERVAL 4 HOUR),
(3, 'CRX202611180003', 'OUT_FOR_DELIVERY', 'Hyderabad Regional Center', 'Out for delivery via Courier Van #14', NOW() - INTERVAL 1 HOUR),

-- For CRX202611180004
(4, 'CRX202611180004', 'BOOKED', 'Kolkata Eastern Hub', 'Shipment order booked, awaiting dispatch', NOW()),

-- For CRX202611180005
(5, 'CRX202611180005', 'BOOKED', 'Mumbai Port Gateway Hub', 'Confidential document registered', NOW() - INTERVAL 12 HOUR),
(5, 'CRX202611180005', 'PICKED_UP', 'Mumbai Port Gateway Hub', 'Collected by dispatch van', NOW() - INTERVAL 6 HOUR);
