package com.pharmacy.model;

import java.time.LocalDateTime;

/**
 * Medicine — master record for a medicine type.
 *
 * Note: Prices, batch numbers, and expiry dates are stored in MedicineBatch
 * to support multiple batches per medicine. This class holds the
 * medicine's general information.
 */
public class Medicine {

    private int id;
    private String name;
    private int categoryId;
    private int supplierId;
    private String manufacturer;
    private String description;
    private String unit;
    private int minStockLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived fields — populated by JOIN queries in DAO
    private String categoryName;
    private String supplierName;
    private int totalQuantity;    // Sum of all non-expired batch quantities
    private boolean lowStock;     // true if totalQuantity <= minStockLevel

    public Medicine() {}

    // Core getters and setters
    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public int getCategoryId()                   { return categoryId; }
    public void setCategoryId(int categoryId)    { this.categoryId = categoryId; }

    public int getSupplierId()                   { return supplierId; }
    public void setSupplierId(int supplierId)    { this.supplierId = supplierId; }

    public String getManufacturer()              { return manufacturer; }
    public void setManufacturer(String m)        { this.manufacturer = m; }

    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }

    public String getUnit()                      { return unit; }
    public void setUnit(String unit)             { this.unit = unit; }

    public int getMinStockLevel()                { return minStockLevel; }
    public void setMinStockLevel(int level)      { this.minStockLevel = level; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)    { this.updatedAt = t; }

    // Derived getters and setters
    public String getCategoryName()              { return categoryName; }
    public void setCategoryName(String n)        { this.categoryName = n; }

    public String getSupplierName()              { return supplierName; }
    public void setSupplierName(String n)        { this.supplierName = n; }

    public int getTotalQuantity()                { return totalQuantity; }
    public void setTotalQuantity(int qty)        {
        this.totalQuantity = qty;
        this.lowStock = qty <= this.minStockLevel;
    }

    public boolean isLowStock()                  { return lowStock; }
    public void setLowStock(boolean lowStock)    { this.lowStock = lowStock; }

    @Override
    public String toString() {
        return "Medicine{id=" + id + ", name='" + name + "', totalQty=" + totalQuantity + "}";
    }
}
