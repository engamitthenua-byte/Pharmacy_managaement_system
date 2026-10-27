-- ============================================================
-- Pharmacy Management System — Database Initialization Script
-- Database: pharmacy_management
-- MySQL 8.x | InnoDB Engine | UTF-8 Encoding
-- ============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS pharmacy_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE pharmacy_management;

-- ============================================================
-- TABLE: admins
-- Stores administrator accounts for system login
-- ============================================================
CREATE TABLE IF NOT EXISTS admins (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(150) UNIQUE,
    phone       VARCHAR(20),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: categories
-- Medicine categories (e.g., Antibiotics, Analgesics)
-- ============================================================
CREATE TABLE IF NOT EXISTS categories (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: suppliers
-- Medicine suppliers / distributors
-- ============================================================
CREATE TABLE IF NOT EXISTS suppliers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    contact_person  VARCHAR(100),
    phone           VARCHAR(20)  NOT NULL,
    email           VARCHAR(150),
    address         TEXT,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: customers
-- Walk-in or registered customers
-- ============================================================
CREATE TABLE IF NOT EXISTS customers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(150),
    address     TEXT,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: medicines
-- Master medicine catalogue (not batch-specific)
-- ============================================================
CREATE TABLE IF NOT EXISTS medicines (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    category_id         INT NOT NULL,
    supplier_id         INT NOT NULL,
    manufacturer        VARCHAR(200),
    description         TEXT,
    unit                VARCHAR(30) DEFAULT 'Tablet',
    min_stock_level     INT NOT NULL DEFAULT 10 COMMENT 'Minimum quantity before low-stock alert',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medicine_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_medicine_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: medicine_batches
-- Each row is a specific batch of a medicine with its own
-- expiry date, purchase/selling price, and stock quantity.
-- This supports batch-level stock tracking so expired batches
-- are never used when valid stock is available.
-- ============================================================
CREATE TABLE IF NOT EXISTS medicine_batches (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id         INT NOT NULL,
    batch_number        VARCHAR(100) NOT NULL,
    purchase_price      DECIMAL(10,2) NOT NULL CHECK (purchase_price >= 0),
    selling_price       DECIMAL(10,2) NOT NULL CHECK (selling_price >= 0),
    quantity            INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    manufacturing_date  DATE,
    expiry_date         DATE NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_medicine_batch (medicine_id, batch_number),
    CONSTRAINT fk_batch_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: purchases
-- Purchase invoice header (one per supplier invoice)
-- ============================================================
CREATE TABLE IF NOT EXISTS purchases (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    supplier_id     INT NOT NULL,
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes           TEXT,
    purchase_date   DATE NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: purchase_items
-- Line items within a purchase invoice
-- ============================================================
CREATE TABLE IF NOT EXISTS purchase_items (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    purchase_id     INT NOT NULL,
    medicine_id     INT NOT NULL,
    batch_id        INT NOT NULL COMMENT 'References medicine_batches',
    quantity        INT NOT NULL CHECK (quantity > 0),
    purchase_price  DECIMAL(10,2) NOT NULL CHECK (purchase_price >= 0),
    total_price     DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_pi_purchase  FOREIGN KEY (purchase_id) REFERENCES purchases(id)         ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pi_medicine  FOREIGN KEY (medicine_id) REFERENCES medicines(id)         ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_pi_batch     FOREIGN KEY (batch_id)    REFERENCES medicine_batches(id)  ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: sales
-- Sales invoice header
-- ============================================================
CREATE TABLE IF NOT EXISTS sales (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(50)  NOT NULL UNIQUE,
    customer_id     INT COMMENT 'NULL means walk-in customer',
    customer_name   VARCHAR(150) COMMENT 'Walk-in customer name (if no customer_id)',
    subtotal        DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount        DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (discount >= 0),
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (tax_amount >= 0),
    total_amount    DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    notes           TEXT,
    sale_date       DATE NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sale_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: sale_items
-- Line items within a sales invoice
-- ============================================================
CREATE TABLE IF NOT EXISTS sale_items (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    sale_id         INT NOT NULL,
    medicine_id     INT NOT NULL,
    batch_id        INT NOT NULL,
    quantity        INT NOT NULL CHECK (quantity > 0),
    selling_price   DECIMAL(10,2) NOT NULL CHECK (selling_price >= 0),
    discount        DECIMAL(10,2) NOT NULL DEFAULT 0.00 CHECK (discount >= 0),
    total_price     DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_si_sale      FOREIGN KEY (sale_id)     REFERENCES sales(id)             ON DELETE CASCADE  ON UPDATE CASCADE,
    CONSTRAINT fk_si_medicine  FOREIGN KEY (medicine_id) REFERENCES medicines(id)         ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_si_batch     FOREIGN KEY (batch_id)    REFERENCES medicine_batches(id)  ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: inventory_transactions
-- Full audit trail of every stock movement
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id         INT NOT NULL,
    batch_id            INT NOT NULL,
    transaction_type    ENUM('PURCHASE','SALE','ADJUSTMENT_IN','ADJUSTMENT_OUT','RETURN') NOT NULL,
    quantity_change     INT NOT NULL COMMENT 'Positive for IN, negative for OUT',
    reference_number    VARCHAR(100) COMMENT 'Invoice number or adjustment ref',
    reason              TEXT,
    transaction_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_medicine  FOREIGN KEY (medicine_id) REFERENCES medicines(id)        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_inv_batch     FOREIGN KEY (batch_id)    REFERENCES medicine_batches(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- INDEXES for performance
-- ============================================================
CREATE INDEX idx_medicines_name         ON medicines(name);
CREATE INDEX idx_medicines_category     ON medicines(category_id);
CREATE INDEX idx_medicines_supplier     ON medicines(supplier_id);
CREATE INDEX idx_batches_medicine       ON medicine_batches(medicine_id);
CREATE INDEX idx_batches_expiry         ON medicine_batches(expiry_date);
CREATE INDEX idx_batches_batch_number   ON medicine_batches(batch_number);
CREATE INDEX idx_purchases_supplier     ON purchases(supplier_id);
CREATE INDEX idx_purchases_date         ON purchases(purchase_date);
CREATE INDEX idx_sales_customer         ON sales(customer_id);
CREATE INDEX idx_sales_date             ON sales(sale_date);
CREATE INDEX idx_inv_medicine           ON inventory_transactions(medicine_id);
CREATE INDEX idx_inv_batch              ON inventory_transactions(batch_id);
CREATE INDEX idx_inv_type               ON inventory_transactions(transaction_type);
CREATE INDEX idx_inv_date               ON inventory_transactions(transaction_date);

-- ============================================================
-- INITIAL DATA
-- ============================================================

-- Default admin account
-- Username: admin | Password: Admin@1234 (BCrypt hashed below)
-- IMPORTANT: Change this password immediately after first login!
INSERT INTO admins (username, password, full_name, email)
VALUES (
    'admin',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NNMm6bLkq',
    'System Administrator',
    'admin@pharmacy.com'
);

-- Sample categories
INSERT INTO categories (name, description) VALUES
    ('Antibiotics',     'Medicines that kill or inhibit bacterial growth'),
    ('Analgesics',      'Pain-relieving medicines'),
    ('Antacids',        'Medicines for acid reflux and indigestion'),
    ('Antihistamines',  'Medicines for allergy relief'),
    ('Vitamins',        'Dietary supplements and vitamins'),
    ('Antiseptics',     'Medicines used to disinfect wounds'),
    ('Antifungals',     'Medicines to treat fungal infections'),
    ('Cardiovascular',  'Medicines for heart and blood pressure');

-- Sample suppliers
INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
    ('MedSupply Pvt Ltd',   'Rajesh Kumar',  '9876543210', 'rajesh@medsupply.com',   '12 Industrial Area, Mumbai, Maharashtra'),
    ('PharmaDist Co.',      'Priya Sharma',  '9123456780', 'priya@pharmadist.com',   '45 Medical Complex, Delhi'),
    ('HealthCare Vendors',  'Suresh Nair',   '9012345678', 'suresh@hcvendors.com',   '78 Pharma Park, Chennai, Tamil Nadu');

-- Sample customer
INSERT INTO customers (name, phone, email, address) VALUES
    ('Walk-in Customer', '0000000000', NULL, 'General Walk-in Counter'),
    ('Ramesh Patel',     '9823456781', 'ramesh.patel@gmail.com', 'Apt 4B, Green Park, Mumbai'),
    ('Anita Sharma',     '9871234560', 'anita.sharma@yahoo.com', '15 Model Town, Delhi'),
    ('Vikram Verma',     '9765432109', 'vikram.v@outlook.com',   'Flat 202, Royal Residency, Pune');

-- Sample Medicines (Master Records)
INSERT INTO medicines (id, name, category_id, supplier_id, manufacturer, description, unit, min_stock_level) VALUES
    (1, 'Paracetamol 500mg',    2, 1, 'Cipla Ltd',        'Analgesic and antipyretic for fever and mild to moderate pain', 'Tablet',  50),
    (2, 'Amoxicillin 500mg',    1, 2, 'Sun Pharma',       'Broad-spectrum penicillin antibiotic for bacterial infections', 'Capsule', 30),
    (3, 'Cetirizine 10mg',      4, 1, 'Dr. Reddy Labs',   'Second-generation antihistamine for seasonal allergy relief',    'Tablet',  25),
    (4, 'Omeprazole 20mg',      3, 3, 'Torrent Pharma',   'Proton-pump inhibitor for acid reflux and GERD treatment',      'Capsule', 20),
    (5, 'Azithromycin 250mg',   1, 2, 'Lupin Pharma',     'Macrolide antibiotic for respiratory and skin infections',      'Tablet',  15),
    (6, 'Metformin 500mg',      8, 1, 'USV Pharma',       'First-line medication for the treatment of type 2 diabetes',     'Tablet',  40),
    (7, 'Cough Syrup 100ml',    4, 3, 'Dabur India',      'Herbal and dextromethorphan soothing cough relief syrup',        'Bottle',  10),
    (8, 'Vitamin C Chewable',   5, 1, 'Abbott Healthcare','Immunity booster dietary supplement tablets',                   'Tablet',  30);

-- Sample Batches (Prices, Expiry Dates, and Stock Quantities)
-- Note: Includes healthy stock, low stock, near-expiry, and expired batches for comprehensive testing!
INSERT INTO medicine_batches (id, medicine_id, batch_number, purchase_price, selling_price, quantity, manufacturing_date, expiry_date) VALUES
    -- Paracetamol: healthy stock batch (exp: 2028-12-31)
    (1, 1, 'PARA-2024-B1', 1.20, 2.50, 150, '2024-01-15', '2028-12-31'),
    -- Amoxicillin: active stock (exp: 2027-06-30)
    (2, 2, 'AMOX-2024-01', 5.50, 9.00, 80,  '2024-02-10', '2027-06-30'),
    -- Cetirizine: low stock batch (quantity 8 <= min_stock 25)
    (3, 3, 'CET-24-99',    1.00, 3.00, 8,   '2024-03-01', '2027-08-31'),
    -- Omeprazole: near-expiry batch (expires next month)
    (4, 4, 'OMP-23-45',    3.20, 6.50, 45,  '2023-05-10', DATE_ADD(CURDATE(), INTERVAL 20 DAY)),
    -- Azithromycin: expired batch (expires past date, quantity > 0)
    (5, 5, 'AZI-22-EX',    12.00, 22.00, 12, '2022-01-10', '2024-01-01'),
    -- Azithromycin: fresh batch
    (6, 5, 'AZI-24-N1',    14.00, 25.00, 60, '2024-04-01', '2027-11-30'),
    -- Metformin: healthy batch
    (7, 6, 'MET-24-M2',    1.80, 4.00, 120, '2024-02-20', '2028-05-31'),
    -- Cough Syrup: low stock (qty 5 <= min_stock 10)
    (8, 7, 'CS-24-09',     45.00, 75.00, 5,  '2024-01-05', '2026-12-31'),
    -- Vitamin C: healthy stock
    (9, 8, 'VITC-24-A',    2.00, 4.50, 90,  '2024-03-15', '2027-09-30');

-- Sample Purchases
INSERT INTO purchases (id, invoice_number, supplier_id, total_amount, notes, purchase_date) VALUES
    (1, 'PO-2024-001', 1, 480.00, 'Initial wholesale medicine stock order', '2024-02-01'),
    (2, 'PO-2024-002', 2, 740.00, 'Antibiotics restocking order',          '2024-03-15');

INSERT INTO purchase_items (purchase_id, medicine_id, batch_id, quantity, purchase_price, total_price) VALUES
    (1, 1, 1, 150, 1.20, 180.00),
    (1, 6, 7, 120, 1.80, 216.00),
    (2, 2, 2, 80,  5.50, 440.00),
    (2, 5, 6, 20,  14.00, 280.00);

-- Sample Sales
INSERT INTO sales (id, invoice_number, customer_id, customer_name, subtotal, discount, tax_amount, total_amount, notes, sale_date) VALUES
    (1, 'INV-2024-0001', 2, 'Ramesh Patel', 75.00,  5.00, 0.00, 70.00,  'Dr. Rao clinic prescription', '2024-04-10'),
    (2, 'INV-2024-0002', 1, 'Walk-in Cash', 45.00,  0.00, 0.00, 45.00,  'Counter OTC sale',            '2024-04-12'),
    (3, 'INV-2024-0003', 3, 'Anita Sharma', 150.00, 10.00, 5.00, 145.00,'Monthly regular medicines',   CURDATE());

INSERT INTO sale_items (sale_id, medicine_id, batch_id, quantity, selling_price, discount, total_price) VALUES
    (1, 1, 1, 10, 2.50, 0.00, 25.00),
    (1, 6, 7, 10, 4.00, 0.00, 40.00),
    (2, 8, 9, 10, 4.50, 0.00, 45.00),
    (3, 2, 2, 10, 9.00, 0.00, 90.00),
    (3, 4, 4, 10, 6.50, 0.00, 65.00);

-- Sample Inventory Audit Transactions
INSERT INTO inventory_transactions (medicine_id, batch_id, transaction_type, quantity_change, reference_number, reason) VALUES
    (1, 1, 'PURCHASE', 150, 'PO-2024-001',  'Initial Stock In'),
    (2, 2, 'PURCHASE', 80,  'PO-2024-002',  'Antibiotics Restock'),
    (1, 1, 'SALE',     -10, 'INV-2024-0001','Counter Sale to Ramesh Patel'),
    (6, 7, 'SALE',     -10, 'INV-2024-0001','Counter Sale to Ramesh Patel'),
    (8, 9, 'SALE',     -10, 'INV-2024-0002','Walk-in OTC Sale'),
    (2, 2, 'SALE',     -10, 'INV-2024-0003','Sale to Anita Sharma'),
    (4, 4, 'SALE',     -10, 'INV-2024-0003','Sale to Anita Sharma');

-- ============================================================
-- VERIFICATION QUERIES (run these after setup to confirm)
-- ============================================================
-- SELECT * FROM admins;
-- SELECT * FROM medicines;
-- SELECT * FROM medicine_batches;
-- SELECT * FROM sales;
-- SHOW TABLES;

-- ============================================================
-- HOW TO CHANGE THE ADMIN PASSWORD:
-- 1. Generate a BCrypt hash for your new password using the
--    application's /profile page after logging in, OR
-- 2. Use the application's built-in change password screen.
-- ============================================================

