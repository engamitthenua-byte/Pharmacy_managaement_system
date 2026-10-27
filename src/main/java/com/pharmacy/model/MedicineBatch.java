package com.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MedicineBatch — a specific batch of a medicine.
 *
 * Batch-level tracking means:
 *  - Each purchase creates or updates a batch record.
 *  - Sales deduct from the oldest non-expired batch (FEFO: First Expired, First Out).
 *  - Expired batches cannot be sold.
 *  - Multiple batches of the same medicine can coexist with different prices / expiry.
 */
public class MedicineBatch {

    private int id;
    private int medicineId;
    private String batchNumber;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private int quantity;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived — populated by JOIN queries
    private String medicineName;
    private String categoryName;
    private String supplierName;
    private int minStockLevel;
    private boolean expired;          // true if expiryDate < today
    private boolean nearExpiry;       // true if expiring within warning window
    private int daysToExpiry;

    public MedicineBatch() {}

    // Core getters/setters
    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getMedicineId()                      { return medicineId; }
    public void setMedicineId(int medicineId)       { this.medicineId = medicineId; }

    public String getBatchNumber()                  { return batchNumber; }
    public void setBatchNumber(String batchNumber)  { this.batchNumber = batchNumber; }

    public BigDecimal getPurchasePrice()            { return purchasePrice; }
    public void setPurchasePrice(BigDecimal price)  { this.purchasePrice = price; }

    public BigDecimal getSellingPrice()             { return sellingPrice; }
    public void setSellingPrice(BigDecimal price)   { this.sellingPrice = price; }

    public int getQuantity()                        { return quantity; }
    public void setQuantity(int quantity)           { this.quantity = quantity; }

    public LocalDate getManufacturingDate()         { return manufacturingDate; }
    public void setManufacturingDate(LocalDate d)   { this.manufacturingDate = d; }

    public LocalDate getExpiryDate()                { return expiryDate; }
    public void setExpiryDate(LocalDate d)          {
        this.expiryDate = d;
        if (d != null) {
            LocalDate today = LocalDate.now();
            this.expired     = d.isBefore(today);
            this.daysToExpiry = (int) today.until(d, java.time.temporal.ChronoUnit.DAYS);
            this.nearExpiry  = !this.expired && this.daysToExpiry <= 30;
        }
    }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)       { this.updatedAt = t; }

    // Derived getters/setters
    public String getMedicineName()                 { return medicineName; }
    public void setMedicineName(String name)        { this.medicineName = name; }

    public String getCategoryName()                 { return categoryName; }
    public void setCategoryName(String name)        { this.categoryName = name; }

    public String getSupplierName()                 { return supplierName; }
    public void setSupplierName(String name)        { this.supplierName = name; }

    public int getMinStockLevel()                   { return minStockLevel; }
    public void setMinStockLevel(int level)         { this.minStockLevel = level; }

    public boolean isExpired()                      { return expired; }
    public void setExpired(boolean expired)         { this.expired = expired; }

    public boolean isNearExpiry()                   { return nearExpiry; }
    public void setNearExpiry(boolean nearExpiry)   { this.nearExpiry = nearExpiry; }

    public int getDaysToExpiry()                    { return daysToExpiry; }
    public void setDaysToExpiry(int days)           { this.daysToExpiry = days; }

    @Override
    public String toString() {
        return "MedicineBatch{id=" + id + ", medicineId=" + medicineId +
               ", batch='" + batchNumber + "', qty=" + quantity +
               ", expiry=" + expiryDate + "}";
    }
}
