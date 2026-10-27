# Pharmacy Management System (AIP Academic Project)

A complete, database-driven web application built with **Java 17**, **Jakarta Servlets**, **JSP**, **JDBC**, and **MySQL**, designed for deployment on **Apache Tomcat 10.1**.

Developed as an Advanced Internet Programming (AIP) college curriculum project, suitable for university laboratory practical examinations, code reviews, and viva voce.

---

## 1. Project Overview & Features

### Core Modules
* **Authentication & Security:**
  * Administrator authentication with **BCrypt password hashing** (cost factor 12).
  * **AuthenticationFilter** protecting all operational endpoints (`/*`).
  * Session fixation protection (`session.invalidate()` and re-generation on login).
  * `HttpOnly` cookie flags and strict browser caching control on sensitive pages.
* **Medicines Catalogue & Categories:**
  * Full CRUD for therapeutic categories and medicine master records.
  * Search by medicine name, therapeutic group, and manufacturer.
  * Minimum stock alert threshold tracking.
  * Foreign key protection: prevents accidental deletion of categories/medicines tied to existing purchases or sales.
* **Batch-Level Inventory & Expiry Management:**
  * Multi-batch support per medicine: each batch maintains its own batch number, manufacturing date, expiry date, purchase cost, selling price, and stock quantity.
  * **First-Expired-First-Out (FEFO)** auto-selection in POS billing.
  * **Automatic Expiry Guard:** Expired batches are blocked from billing and highlighted in alerts.
  * Near-expiry warning system (flags batches expiring within 30 days).
  * Full stock adjustment interface (disposal, damage, physical recount audit).
* **Point-of-Sale (POS) Billing & Customer Sales:**
  * Dynamic, responsive POS interface with real-time stock verification via AJAX.
  * Support for both registered customers and counter walk-in cash patrons.
  * Live client-side calculation of subtotals, item lines, discounts, tax/GST, and net total.
  * **Atomic Database Transactions:** Sale header, line items, stock deduction, and inventory audit logs are executed inside an atomic JDBC transaction (`setAutoCommit(false)` + `commit()` / `rollback()`).
  * Formatted, printable thermal/A4 retail drug invoice sheet (`@media print` supported).
* **Procurement & Supplier Purchases:**
  * Multi-line purchase order invoice entry.
  * Automatically creates or updates batches and increments available stock.
  * Complete supplier/distributor directory with purchase history.
* **Audit Trail & Inventory Transactions:**
  * Immutable transaction log for every stock movement: `PURCHASE`, `SALE`, `ADJUSTMENT_IN`, `ADJUSTMENT_OUT`, and `RETURN`.
* **Business Reports & Analytics:**
  * Daily and date-range sales reports with revenue and discount totals.
  * Monthly sales trend breakdown by calendar year.
  * Procurement spending by supplier.
  * Stock health and inventory valuation reports.
  * Top 20 best-selling medicines ranked by volume and revenue.
  * Customer lifetime spend analytics.

---

## 2. Technology Stack & Architectural Pattern

* **Architecture:** Model-View-Controller (MVC) Pattern
  * **Model (`com.pharmacy.model`):** Plain Old Java Objects (POJOs) with encapsulation.
  * **View (`src/main/webapp/WEB-INF/views`):** JSP with JSTL 3.0, custom CSS design system, and vanilla JavaScript.
  * **Controller (`com.pharmacy.controller`):** Jakarta Servlets handling HTTP GET/POST, query routing, validation, and session state.
  * **Data Access Layer (`com.pharmacy.dao`):** Pure JDBC with `PreparedStatement` to ensure SQL injection immunity.
  * **Filter Layer (`com.pharmacy.filter`):** Intercepts requests for authentication and authorization.
* **Language & Specifications:** Java 17+, Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)
* **Application Server:** Apache Tomcat 10.1+
* **Database:** MySQL 8.0+
* **Libraries (Managed via Maven):**
  * `mysql-connector-j` (8.3.0) — MySQL JDBC driver
  * `bcrypt` (0.10.2) by at.favre.lib — Industrial-grade password hashing
  * `slf4j-api` (2.0.12) & `logback-classic` (1.5.3) — Structured application logging

---

## 3. Project Directory Structure

```text
Pharmacy Management System/
│
├── database/
│   └── pharmacy_management.sql       # Database schema, indexes & seed data
│
├── pom.xml                           # Maven project descriptor & dependencies
│
└── src/
    └── main/
        ├── java/com/pharmacy/
        │   ├── controller/           # Servlet Controllers (12 Controllers)
        │   │   ├── CategoryServlet.java
        │   │   ├── CustomerServlet.java
        │   │   ├── DashboardServlet.java
        │   │   ├── InventoryServlet.java
        │   │   ├── LoginServlet.java
        │   │   ├── LogoutServlet.java
        │   │   ├── MedicineServlet.java
        │   │   ├── ProfileServlet.java
        │   │   ├── PurchaseServlet.java
        │   │   ├── ReportsServlet.java
        │   │   ├── SalesServlet.java
        │   │   └── SupplierServlet.java
        │   ├── dao/                  # Data Access Objects (JDBC PreparedStatements)
        │   │   ├── AdminDAO.java
        │   │   ├── CategoryDAO.java
        │   │   ├── CustomerDAO.java
        │   │   ├── InventoryDAO.java
        │   │   ├── MedicineDAO.java
        │   │   ├── PurchaseDAO.java
        │   │   ├── ReportsDAO.java
        │   │   ├── SalesDAO.java
        │   │   └── SupplierDAO.java
        │   ├── filter/
        │   │   └── AuthenticationFilter.java  # Security & session guard
        │   ├── model/                # Data Models & POJOs
        │   │   ├── Admin.java
        │   │   ├── Category.java
        │   │   ├── Customer.java
        │   │   ├── InventoryTransaction.java
        │   │   ├── Medicine.java
        │   │   ├── MedicineBatch.java
        │   │   ├── Purchase.java
        │   │   ├── PurchaseItem.java
        │   │   ├── Sale.java
        │   │   ├── SaleItem.java
        │   │   └── Supplier.java
        │   └── util/                 # Utilities
        │       ├── AppUtil.java
        │       ├── DBConnection.java # Database connection manager
        │       └── ValidationUtil.java
        │
        ├── resources/
        │   ├── application.properties# DB connection configurations
        │   └── logback.xml           # Logging configuration
        │
        └── webapp/
            ├── index.jsp             # Entry redirect
            ├── css/
            │   └── style.css         # Modern clinical CSS design system
            ├── js/
            │   └── main.js           # POS calculations, AJAX batch fetcher
            ├── error/
            │   ├── 404.jsp
            │   └── 500.jsp
            └── WEB-INF/
                ├── web.xml           # Deployment descriptor
                └── views/            # Protected JSP views
                    ├── common/       # header, navbar, sidebar, footer, alerts
                    ├── categories/
                    ├── customers/
                    ├── inventory/
                    ├── medicines/
                    ├── profile/
                    ├── purchases/
                    ├── reports/
                    ├── sales/
                    ├── dashboard.jsp
                    └── login.jsp
```

---

## 4. Setup & Installation Guide

### Step 1: Database Setup
1. Open **MySQL Workbench** or your MySQL command-line client:
   ```bash
   mysql -u root -p
   ```
2. Execute the provided SQL script:
   ```sql
   source e:/Pharmacy Management System/database/pharmacy_management.sql;
   ```
   *(Or copy-paste the contents of `database/pharmacy_management.sql` into MySQL Workbench and run it).*

3. Verify that tables are populated:
   ```sql
   USE pharmacy_management;
   SHOW TABLES;
   SELECT * FROM admins;
   ```

### Step 2: Configure Database Credentials
Open `src/main/resources/application.properties` and verify your MySQL host, port, username, and password:
```properties
db.url=jdbc:mysql://localhost:3306/pharmacy_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.user=root
db.password=your_mysql_password
```

### Step 3: Running in Eclipse IDE
1. Open **Eclipse IDE for Enterprise Java and Web Developers**.
2. Click **File &rarr; Import... &rarr; Maven &rarr; Existing Maven Projects**.
3. Browse to `e:\Pharmacy Management System` and click **Finish**.
4. Right-click on the project &rarr; **Run As &rarr; Run on Server**.
5. Select **Apache Tomcat v10.1** and click **Finish**.

### Step 4: Running in IntelliJ IDEA Ultimate
1. Open IntelliJ IDEA &rarr; **Open** &rarr; select `e:\Pharmacy Management System`.
2. Configure **Run/Debug Configuration** &rarr; Add **Tomcat Server (Local)**.
3. In the **Deployment** tab, add `pharmacy-management:war exploded`.
4. Set Application context to `/` or `/pharmacy`.
5. Click **Run**.

### Step 5: Building WAR file via Maven Command Line
```bash
mvn clean package
```
The resulting `pharmacy-management.war` in the `target/` directory can be copied directly into Tomcat's `webapps/` folder.

---

## 5. Default Credentials

| Role | Username | Default Password |
|---|---|---|
| **System Administrator** | `admin` | `Admin@1234` |

*After signing in, you can update your administrator profile details and change your password securely from the **Settings / Profile** screen.*

---

## 6. Viva Voce & Academic Exam Preparation Guide

### Q1: What architectural pattern does this application follow?
> **Answer:** The application strictly follows the **Model-View-Controller (MVC Architecture)**:
> * **Model:** Plain Java classes representing domain entities (`Medicine`, `Sale`, `Purchase`, `Customer`, etc.).
> * **View:** JSP pages styled with custom CSS and enhanced with JSTL tags. All business views are placed under `/WEB-INF/views/` to prevent direct URL access.
> * **Controller:** Jakarta Servlets (`HttpServlet`) handle routing, parameter extraction, validation, business rule enforcement, and forwarding.
> * **DAO Layer:** Encapsulates all SQL logic, separating database code from controllers.

### Q2: Why is Apache Tomcat 10.1 required instead of Tomcat 9?
> **Answer:** Tomcat 10+ implements the **Jakarta EE 10** specifications where the package namespace transitioned from `javax.*` (e.g. `javax.servlet.*`) to `jakarta.*` (e.g. `jakarta.servlet.*`). Since our code uses `jakarta.servlet.http.HttpServlet`, Tomcat 10.1 is mandatory.

### Q3: How is SQL Injection prevented in this application?
> **Answer:** Every query in the DAO layer uses **`java.sql.PreparedStatement`** with parameter placeholders (`?`). Values are bound using typed setters (`ps.setString()`, `ps.setInt()`, `ps.setDate()`). This ensures user inputs are never concatenated directly into SQL query strings and are treated as data literals rather than executable SQL commands.

### Q4: How is transaction management implemented in the POS Billing and Purchase modules?
> **Answer:** JDBC transactions are controlled manually using:
> 1. `connection.setAutoCommit(false);` at the start of the operation.
> 2. All related operations (inserting invoice header, verifying stock, inserting each line item, deducting batch inventory, and recording audit logs) run sequentially on the **same connection**.
> 3. If any step fails or an unexpected exception occurs, `connection.rollback();` is invoked inside the `catch` block to revert all modifications.
> 4. `connection.commit();` is executed only if all steps complete successfully.

### Q5: How does the system handle medicine batching and expiry dates?
> **Answer:** Rather than storing prices and quantities directly on the medicine master record, medicines have a 1-to-many relationship with `medicine_batches`. Each batch has its own expiry date and available quantity. The POS uses **First-Expired-First-Out (FEFO)** to prioritize selling batches that expire soonest, while strictly blocking batches whose expiry date is in the past.

### Q6: How are passwords stored and verified securely?
> **Answer:** Passwords are never stored in plaintext. We utilize **BCrypt** hashing with a work factor of 12. BCrypt includes a cryptographically random salt per user and an adaptive key derivation function that prevents rainbow table attacks and brute force cracking.

---

## 7. License & Academic Attribution
Developed for the Advanced Internet Programming (AIP) laboratory coursework. Free for educational study and examination evaluation.
