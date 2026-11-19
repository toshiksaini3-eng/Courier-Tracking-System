<<<<<<< HEAD
# 🚚 Courier Tracking System (Logistics Management Desktop Application)

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2026-blue?logo=openjdk)](https://www.oracle.com/java/)
[![Swing](https://img.shields.io/badge/GUI-Java%20Swing%20%2B%20FlatLaf-orange)](https://www.formdev.com/flatlaf/)
[![Database](https://img.shields.io/badge/Database-MySQL%208%20%2B%20JDBC-blue?logo=mysql)](https://www.mysql.com/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20Layered%20MVC-success)]()
[![Academic](https://img.shields.io/badge/Project-B.Tech%20OOP%20Mini--Project-purple)]()

A complete, production-grade Java Swing desktop application engineered for courier booking, package lifecycle tracking, hub management, tariff computation, and logistics analytics. 

Developed as a **B.Tech Computer Science Java OOP Mini-Project**, it rigorously demonstrates all core Object-Oriented Programming concepts, JDBC best practices, thread-safe background simulation, and modern desktop UI design.

---

## 🌟 Key Application Features

1. **Executive Operations Dashboard**:
   - 6 Dynamic KPI Metric Cards (*Total Parcels, Booked, In Transit, Out for Delivery, Delivered, Pending Deliveries*).
   - Recent Bookings Table with double-click Airway Bill inspection.
   - Real-time Activity Audit Feed.
   - All statistics queried dynamically via SQL (`PreparedStatement` & `ResultSet`).

2. **Parcel Booking Engine (Module 1)**:
   - Comprehensive two-party booking form (Sender Information & Receiver Information).
   - Source & Destination logistics hub routing populated dynamically from the database.
   - Dynamic real-time delivery charge calculator based on parcel weight (kg) and parcel type (*Document* vs. *Package*).
   - Unique collision-safe consignment number generation (*e.g., `CRX202611180001`*).
   - Instant booking confirmation modal with "Copy Consignment Number", "View / Print Airway Bill", and "Track Now".

3. **Unique Consignment Number Generator (Module 2)**:
   - Format: `CRX` + YYYYMMDD + 4-digit sequence.
   - Atomic and collision-proof sequence generation backed by database uniqueness constraints.

4. **Status Management & State Machine (Module 3)**:
   - Supports 8 lifecycle states: `BOOKED`, `PICKED_UP`, `IN_TRANSIT`, `ARRIVED_AT_DESTINATION`, `OUT_FOR_DELIVERY`, `DELIVERED`, `DELIVERY_FAILED`, `RETURNED`.
   - Strict State Machine enforcement preventing invalid transitions (e.g., `BOOKED` cannot jump directly to `DELIVERED`; `DELIVERED` is terminal).
   - Full location checkpoint logging with timestamps and remarks.

5. **Customer & Staff Tracking Portal**:
   - Search by consignment number.
   - Visual 5-Stage Stepper Progress Bar (*Booked ➔ Picked Up ➔ In Transit ➔ Out for Delivery ➔ Delivered*).
   - Complete chronological audit log timeline with date/time, status badge, and location.

6. **Shipment Management Console**:
   - Interactive search by Consignment No, Sender Name, Receiver Name, or Phone.
   - Filter by Status and Branch Hub.
   - Printable Airway Bill dialog with text export.
   - Export all filtered consignments to CSV.

7. **Branch Hub Network Management**:
   - Add, edit, and toggle active status for logistics hubs across major cities.
   - Instant synchronization with booking form dropdowns.

8. **Delivery Analytics & Reports**:
   - Time-period filters (*Today, Last 7 Days, This Month, All Time*).
   - Metrics: Total Billed Revenue, Deliveries Completed, Delivery Success Rate (%), Average Parcel Weight.
   - Export to CSV & Native printer preview dialog.

9. **Multithreaded Background Transit Simulator (Key Feature)**:
   - Dedicated multithreading simulation panel.
   - Uses `ExecutorService` and background worker thread to advance parcel status across checkpoints.
   - Speed controls (1x, 2x, 3x), Pause, Resume, and Stop controls.
   - Thread-safe UI updates dispatched on Swing's Event Dispatch Thread (`SwingUtilities.invokeLater`).
   - Live terminal log console showing background thread execution in real-time.

10. **About Project & Academic Viva Documentation**:
    - Complete presentation panel detailing OOP concepts, design patterns, architecture, and technology badges.

---

## 🏗️ Software Architecture & Design Patterns

The system strictly follows a clean layered MVC architecture:

```
[UI Layer (Swing + FlatLaf)]
   ├── MainFrame (Maximized Window, Sidebar Navigation, Header)
   ├── Panels (Dashboard, Booking, Tracking, Management, Reports, Simulator, About)
   ├── Dialogs (Booking Confirmation, Airway Bill, Branch Edit, DB Settings)
   └── Common Components (CardPanel, ModernTable, StatusBadge, StatCard)
         │
         ▼
[Service / Business Logic Layer]
   ├── ParcelService (Booking orchestration, validation, queries)
   ├── TrackingService (Lifecycle audit logging)
   ├── BranchService (Branch network management)
   ├── DeliveryChargeCalculator (Tariff computation engine)
   ├── StatusTransitionValidator (State machine validator)
   └── ParcelSimulationService (Background multithreaded simulation)
         │
         ▼
[Data Access Object (DAO) Layer]
   ├── ParcelDao & ParcelDaoImpl (PreparedStatement / ResultSet)
   ├── BranchDao & BranchDaoImpl
   └── TrackingDao & TrackingDaoImpl
         │
         ▼
[Database & Utility Layer]
   ├── DatabaseConnection (Singleton pattern with MySQL 8 & zero-setup fallback)
   ├── DatabaseInitializer (Auto schema creation & seed data)
   ├── ConsignmentGenerator (Atomic unique ID factory)
   └── ValidationUtil & ReceiptGenerator
```

---

## 🎓 Core OOP Concepts Demonstrated (Viva Reference)

| OOP Concept | Project Implementation |
| :--- | :--- |
| **Encapsulation** | All entities (`Parcel`, `Branch`, `TrackingHistory`, `DashboardStats`) have private fields with validation in getters and setters. External code cannot modify state without business rules. |
| **Inheritance** | Custom UI widgets (`CardPanel`, `ModernTable`, `StatusBadge`) extend Swing's `JPanel`, `JTable`, and `JLabel`. Custom checked exception hierarchy (`CourierException` ➔ `DatabaseException`, `ValidationException`, `InvalidStatusTransitionException`). |
| **Polymorphism** | DAO interfaces (`BranchDao`, `ParcelDao`, `TrackingDao`) allow interchangeable implementations. Overridden methods: `paintComponent()`, `toString()`, `isCellEditable()`, custom table renderers. |
| **Abstraction** | Service and DAO layer interfaces completely decouple SQL and database engine implementation from the graphical user interface. |
| **Multithreading** | `ParcelSimulationService` utilizes Java `ExecutorService` and background worker threads, synchronizing UI updates via `SwingUtilities.invokeLater()` to preserve Swing EDT thread safety. |
| **Exception Handling**| Checked exception hierarchy, robust try-with-resources for leak-free SQL resource closing, parameter validation before database calls. |
| **Collections Framework** | Extensive usage of `List`, `Map`, `Set`, `EnumMap`, `EnumSet`, and Java 8 Streams for filtering, sorting, and aggregating metrics. |
| **Design Patterns** | **DAO Pattern**, **Singleton Pattern** (`DatabaseConnection`), **Factory Pattern** (`ConsignmentGenerator`), **Strategy Pattern** (`DeliveryChargeCalculator`), **Observer Pattern** (UI callbacks). |

---

## 🚀 How to Run the Application

### Method 1: Instant Run using Batch File (Recommended)
Simply double-click:
```cmd
run.bat
```
*(If the application has not been compiled yet, `run.bat` will automatically invoke `compile.bat` and launch the application!)*

### Method 2: In VS Code (Press F5)
1. Open this workspace folder in **VS Code**.
2. Open `src/main/java/com/couriertracking/Main.java`.
3. Click the **Run** button at the top right, or press **F5**.
*(VS Code configuration files `.vscode/launch.json` and `.vscode/settings.json` are already set up).*

### Method 3: Command Line (Windows PowerShell / CMD)
```cmd
# 1. Compile all Java sources
.\compile.bat

# 2. Launch the application
java -cp "bin;lib/*;src/main/resources" com.couriertracking.Main
```

### Method 4: Using Maven
```cmd
mvn clean package
mvn exec:java
```

---

## 🗄️ Database Setup & Dual-Mode Connectivity

The application supports **both MySQL 8 and an embedded zero-setup mode**:

### Option A: MySQL 8 (Recommended for Full Evaluation)
1. Ensure your MySQL server (or XAMPP / MariaDB) is running on port `3306`.
2. Open `config/db.properties` (or click **⚙ Settings** at the bottom of the sidebar inside the app).
3. Set your username and password:
   ```properties
   db.host=localhost
   db.port=3306
   db.name=courier_db
   db.user=root
   db.password=your_password
   ```
4. The application automatically executes `CREATE DATABASE IF NOT EXISTS courier_db`, builds all tables, and populates 6 regional branch hubs and 5 demo parcels with full tracking histories!

### Option B: Zero-Setup Offline Demo Fallback (Viva Safety Guarantee)
If MySQL is not installed or offline during your college viva demonstration, the application **automatically detects it and activates the embedded local database (`courier_db.sqlite`)**.
- Zero configuration required.
- The UI displays an indicator in the sidebar: `● SQLite (Local Demo Mode)`.
- You can switch between MySQL and SQLite live at any time using the built-in **⚙ Settings** dialog.

---

## 🧪 Automated Test Suite

To verify all database queries, state machine rules, and multithreading headless:
```cmd
java -cp "bin;lib/*;src/main/resources" com.couriertracking.TestRunner
```
Output:
```
==========================================================
   COURIER TRACKING SYSTEM - AUTOMATED VERIFICATION SUITE  
==========================================================
[TEST 1] Initializing Database & Seed Data... PASSED
[TEST 2] Verifying Branch Hubs in Database... PASSED (6 branches active)
[TEST 3] Testing Dynamic Delivery Tariff Calculator... PASSED
[TEST 4] Testing Parcel Booking & Auto Consignment No... PASSED
[TEST 5] Testing Tracking History Audit Trail... PASSED
[TEST 6] Testing Logistics State Machine Validation... PASSED
[TEST 7] Testing Valid Status Update Execution... PASSED
[TEST 8] Testing Dynamic Dashboard KPI Queries... PASSED
[TEST 9] Testing Printable Airway Bill Receipt Generation... PASSED
==========================================================
   TEST EXECUTION SUMMARY: 9 PASSED, 0 FAILED
==========================================================
```

---

## 📂 Project Directory Structure

```
Courier System/
├── pom.xml                               # Maven Project Descriptor
├── schema.sql                            # Full MySQL 8 Database Schema & Seeds
├── README.md                             # Comprehensive Documentation
├── compile.bat                           # 1-Click Java Compiler Script
├── run.bat                               # 1-Click Application Launcher
├── config/
│   └── db.properties                     # Database Configuration
├── lib/                                  # Bundled Dependency Libraries
│   ├── flatlaf-3.4.1.jar                 # Modern Swing Look & Feel
│   ├── mysql-connector-j-8.4.0.jar       # MySQL 8 JDBC Driver
│   ├── sqlite-jdbc-3.45.2.0.jar          # Fail-safe Embedded DB Driver
│   ├── slf4j-api-2.0.12.jar              # Logging API
│   └── slf4j-simple-2.0.12.jar           # Simple Logger
├── .vscode/
│   ├── settings.json                     # Classpath & Compiler Settings
│   └── launch.json                       # F5 Debug / Run Configurations
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── couriertracking/
        │           ├── Main.java         # Application Entry Point
        │           ├── TestRunner.java   # Automated Headless Test Suite
        │           ├── SimulationTest.java# Multithreading Verification Test
        │           ├── model/            # Domain POJOs (Parcel, Branch, Status, etc.)
        │           ├── dao/              # JDBC DAO Interfaces & Implementations
        │           ├── service/          # Business Services, State Machine, Tariff Calc
        │           ├── util/             # Database Singleton, Generators, Theme
        │           ├── exception/        # Custom Exception Hierarchy
        │           └── ui/
        │               ├── MainFrame.java# Main Application Maximized Window
        │               ├── common/       # CardPanel, ModernTable, StatusBadge, StatCard
        │               ├── dialog/       # Booking Dialog, Airway Bill, Branch Edit, DB Config
        │               └── panel/        # Dashboard, Booking, Tracking, Reports, Simulation, About
        └── resources/
            ├── db.properties             # Fallback Classpath DB Configuration
            └── schema.sql                # Embedded Schema Definition
```

---

## 👨‍🎓 Academic Presentation Notes

- **Language**: Java 17+ (SE Runtime)
- **GUI Framework**: Java Swing with FlatLaf Modern Theme
- **Database Engine**: MySQL 8 (InnoDB, UTF-8, PreparedStatement parameterized JDBC)
- **Concurrency**: `ScheduledExecutorService`, background daemon thread pool, `SwingUtilities.invokeLater()`
- **College Demonstration**: Maximized window on launch, responsive layout, real-time validations, printable receipts, and CSV exports.
=======
# Courier-Tracking-System
A Java-based Courier Tracking System that allows users to book parcels, generate unique consignment numbers, update shipment statuses, track parcels, and generate delivery reports.
>>>>>>> 91d929e739d8489d6a060573350e0f00339e17dd
