# Grand Vista Hotel Management System
## Complete Setup & Run Guide

---

## 📋 Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| Java JDK | 17 or 21 (LTS) | https://adoptium.net |
| Apache Maven | 3.8+ | https://maven.apache.org |
| Git (optional) | Any | https://git-scm.com |

> **No MySQL installation needed** — the project uses **SQLite** (embedded, zero-config).
> The database file `grandvista.db` is auto-created in the project root on first run.

---

## 📁 Project Structure

```
HotelManagementSystem/
├── pom.xml                              ← Maven build file
├── sql/
│   └── schema.sql                       ← Database schema (reference)
├── docs/
│   └── README.md                        ← This file
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── com/hotel/
    │       ├── model/                   ← Domain entities
    │       │   ├── Room.java            (Abstract base — Week 1: Abstraction)
    │       │   ├── StandardRoom.java    (Inheritance — Week 1)
    │       │   ├── RoomType.java        (Enum — Week 2)
    │       │   ├── Amenities.java       (Interface — Week 1)
    │       │   ├── RoomFactory.java     (Factory pattern)
    │       │   ├── Customer.java        (Serializable — Week 6)
    │       │   ├── Booking.java         (Serializable — Week 6)
    │       │   ├── BillingItem.java     (Wrapper classes — Week 2)
    │       │   └── Review.java
    │       ├── dao/                     ← Data Access Layer (JDBC)
    │       │   ├── RoomDAO.java
    │       │   ├── CustomerDAO.java
    │       │   ├── BookingDAO.java
    │       │   ├── BillingDAO.java
    │       │   └── ReviewDAO.java
    │       ├── service/                 ← Business Logic Layer
    │       │   ├── BookingService.java  (Synchronization — Week 4)
    │       │   └── HousekeepingService.java (Threads — Week 3)
    │       ├── util/                    ← Utilities
    │       │   ├── DatabaseConnection.java (JDBC Singleton)
    │       │   ├── BillingCalculator.java  (Generics — Week 7)
    │       │   └── FileBackupUtil.java     (File I/O — Weeks 5 & 6)
    │       └── view/                   ← JavaFX UI Layer
    │           ├── Main.java           (Application entry point)
    │           ├── SplashController.java
    │           └── DashboardController.java
    └── resources/com/hotel/
        └── css/
            ├── splash.css
            └── dashboard.css
```

---

## 🚀 Running the Application

### Method 1: Maven (Recommended)

```bash
# 1. Navigate to project root
cd HotelManagementSystem

# 2. Download dependencies and compile
mvn clean compile

# 3. Run the application
mvn javafx:run
```

### Method 2: IntelliJ IDEA

1. Open IntelliJ → **File → Open** → select `HotelManagementSystem/` folder
2. IntelliJ auto-detects `pom.xml` → click **Trust Project**
3. Wait for Maven to download dependencies (~2 min first time)
4. Navigate to `src/main/java/com/hotel/view/Main.java`
5. Right-click → **Run 'Main'**

### Method 3: Eclipse

1. **File → Import → Maven → Existing Maven Projects**
2. Browse to `HotelManagementSystem/`
3. Select the project → **Finish**
4. Right-click `Main.java` → **Run As → Java Application**

---

## 🗄️ Database

- **Type:** SQLite (embedded — no server needed)
- **File:** `grandvista.db` (auto-created in project root on first launch)
- **Tables:** rooms, customers, bookings, billing_items, alacarte_menu, reviews
- **Seed Data:** 9 rooms and 16 menu items are auto-inserted on first run

To reset the database, simply **delete** `grandvista.db` and re-run the app.

---

## 🎓 Lab Concepts Map

| Week | Concept | File(s) |
|------|---------|---------|
| Week 1 | OOP: Encapsulation | All model classes (private fields + getters/setters) |
| Week 1 | OOP: Inheritance | `StandardRoom`, `DeluxeRoom`, `SuiteRoom`, `PenthouseRoom extends Room` |
| Week 1 | OOP: Polymorphism | `calculateBill()` overridden in each subclass |
| Week 1 | OOP: Abstraction | `Room.java` (abstract class with abstract methods) |
| Week 1 | Interface | `Amenities.java` (with default methods) |
| Week 1 | Constructor Overloading | `Room`, `Customer`, `Booking` — multiple constructors |
| Week 1 | `this` and `super` | `Room.java` — `this(...)` chaining; subclasses use `super(...)` |
| Week 2 | Wrapper Classes | `Integer.valueOf()`, `Double.valueOf()` in `Booking`, `BillingItem` |
| Week 2 | Autoboxing/Unboxing | `Booking.getNumberOfNights()` → returns `Integer`; used in arithmetic |
| Week 2 | Enum | `RoomType.java` — with fields, constructor, methods |
| Week 3 | Thread subclass | `RoomCleaningThread extends Thread` in `HousekeepingService` |
| Week 3 | Runnable interface | `BookingProcessorRunnable implements Runnable` |
| Week 3 | `sleep()`, `yield()`, `join()` | All in `HousekeepingService.java` |
| Week 4 | `synchronized` method | `BookingService.bookRoom()` — prevents double booking |
| Week 4 | `wait()` / `notify()` | `BookingService` — `cleaningMonitor.wait/notifyAll()` |
| Week 5 | FileWriter / FileReader | `FileBackupUtil.writeAuditLog()`, `readAuditLog()` |
| Week 5 | BufferedWriter / BufferedReader | Same — wraps FileWriter/Reader for efficiency |
| Week 5 | CSV Export | `FileBackupUtil.exportBookingsToCsv()` |
| Week 6 | ObjectOutputStream | `FileBackupUtil.serializeBookings()` |
| Week 6 | ObjectInputStream | `FileBackupUtil.deserializeBookings()` |
| Week 6 | Serializable | `Customer`, `Booking` implement `Serializable` |
| Week 6 | RandomAccessFile | `FileBackupUtil.writeRoomStatusRecord()` / `readRoomStatus()` |
| Week 7 | Generic class | `BillingCalculator<T extends Number>` |
| Week 7 | Generic method | `BillingCalculator.sum(List<N> values)` |
| Week 7 | Bounded type | `<N extends Number>` in `sum()` |
| Week 8 | ArrayList | `RoomDAO.findAll()`, `HousekeepingService`, etc. |
| Week 8 | HashMap | `BookingService.roomBookingMap` — room→booking fast lookup |
| Week 8 | Iterator | `BookingService.loadActiveBookingsIntoMap()`, `BillingCalculator` |
| Week 8 | Collections.sort() | `BookingService.getAllBookingsSorted()` with Comparator |
| Week 9-10 | JavaFX Scene | `SplashController`, `DashboardController` |
| Week 9-10 | CSS Styling | `dashboard.css`, `splash.css` |
| Week 9-10 | TableView | Customer, Booking, Review tables in dashboard |
| Week 9-10 | Animations | Fade, Translate, Scale transitions in splash + dashboard |
| Week 9-10 | Event Handling | Button `setOnAction`, `ListView` selection listeners |
| Week 9-10 | Layout Panes | VBox, HBox, GridPane, StackPane, BorderPane, FlowPane |
| JDBC | DAO Pattern | `RoomDAO`, `CustomerDAO`, `BookingDAO`, `BillingDAO`, `ReviewDAO` |
| JDBC | PreparedStatement | All DAO methods use parameterized queries |
| JDBC | Connection Singleton | `DatabaseConnection.getInstance()` |
| Maven | Build Tool | `pom.xml` — dependencies, plugins |

---

## 🖥️ Application Flow

```
Launch App
    │
    ▼
┌─────────────────────────────┐
│       SPLASH SCREEN         │
│  Grand Vista — Landing Page │
│  [★★★★★]  BOOK YOUR STAY → │
└──────────────┬──────────────┘
               │ Click "BOOK YOUR STAY"
               ▼
┌─────────────────────────────────────────────────────┐
│                    DASHBOARD                        │
│  ┌──────────────┬──────────────────────────────┐   │
│  │   SIDEBAR    │        CONTENT AREA           │   │
│  │              │                               │   │
│  │  🏨 Check-In │   (Active panel shown here)   │   │
│  │  🛏 Rooms    │                               │   │
│  │  🚪 Check-Out│                               │   │
│  │  🧹 Housekpg │                               │   │
│  │  🍽 À La Cart│                               │   │
│  │  👤 Customers│                               │   │
│  │  ⭐ Reviews  │                               │   │
│  └──────────────┴──────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

---

## 📊 Sample Data (Auto-Seeded)

| Room | Type | Price/Night | Floor | Capacity |
|------|------|-------------|-------|----------|
| 101-103 | Standard | ₹3,500 | 1 | 2 |
| 201-203 | Deluxe | ₹6,500–₹7,000 | 2 | 2–3 |
| 301-302 | Suite | ₹12,000 | 3 | 4 |
| 401 | Penthouse | ₹25,000 | 4 | 6 |

---

## ⚠️ Troubleshooting

| Problem | Solution |
|---------|---------|
| `JavaFX runtime components are missing` | Run via `mvn javafx:run`, not `java -jar` |
| `org.xerial.sqlite.JDBC not found` | Run `mvn clean install` first |
| `grandvista.db: permission denied` | Run app from a folder where you have write access |
| App opens but looks plain | CSS files are inside jar; check `src/main/resources` exists |
| Module errors with IntelliJ | Mark `src/main/java` as Sources Root, `src/main/resources` as Resources Root |

---

## 🏗️ Architecture

```
View (JavaFX Controllers)
    │  calls
    ▼
Service Layer (Business Logic + Synchronization)
    │  calls
    ▼
DAO Layer (JDBC + SQL)
    │  uses
    ▼
DatabaseConnection (Singleton)
    │  connects to
    ▼
grandvista.db (SQLite file)
```

---

*Grand Vista Hotel Management System — Built with Java 17 + JavaFX 21 + SQLite*
