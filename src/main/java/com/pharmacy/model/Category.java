package com.pharmacy.model;

import java.time.LocalDateTime;

/**
 * Category — represents a medicine category (e.g., Antibiotics, Analgesics)
 */
public class Category {

    private int id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Derived field — not in DB, populated by DAO join query
    private int medicineCount;

    public Category() {}

    public Category(String name, String description) {
        this.name        = name;
        this.description = description;
    }

    public int getId()                           { return id; }
    public void setId(int id)                    { this.id = id; }

    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }

    public String getDescription()               { return description; }
    public void setDescription(String desc)      { this.description = desc; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t)    { this.updatedAt = t; }

    public int getMedicineCount()                { return medicineCount; }
    public void setMedicineCount(int count)      { this.medicineCount = count; }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "'}";
    }
}
