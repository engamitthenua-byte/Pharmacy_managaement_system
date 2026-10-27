package com.pharmacy.model;

import java.math.BigDecimal;

/**
 * SaleItem — a single line item in a sales invoice.
 */
public class SaleItem {

    private int id;
    private int saleId;
    private int medicineId;
    private int batchId;
    private int quantity;
    private BigDecimal sellingPrice;
    private BigDecimal discount;     // Per-item discount amount
    private BigDecimal totalPrice;

    // Derived
    private String medicineName;
    private String batchNumber;
    private String unit;

    public SaleItem() {}

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public int getSaleId()                          { return saleId; }
    public void setSaleId(int saleId)               { this.saleId = saleId; }

    public int getMedicineId()                      { return medicineId; }
    public void setMedicineId(int medicineId)       { this.medicineId = medicineId; }

    public int getBatchId()                         { return batchId; }
    public void setBatchId(int batchId)             { this.batchId = batchId; }

    public int getQuantity()                        { return quantity; }
    public void setQuantity(int quantity)           { this.quantity = quantity; }

    public BigDecimal getSellingPrice()             { return sellingPrice; }
    public void setSellingPrice(BigDecimal price)   { this.sellingPrice = price; }

    public BigDecimal getDiscount()                 { return discount; }
    public void setDiscount(BigDecimal discount)    { this.discount = discount; }

    public BigDecimal getTotalPrice()               { return totalPrice; }
    public void setTotalPrice(BigDecimal total)     { this.totalPrice = total; }

    public String getMedicineName()                 { return medicineName; }
    public void setMedicineName(String name)        { this.medicineName = name; }

    public String getBatchNumber()                  { return batchNumber; }
    public void setBatchNumber(String bn)           { this.batchNumber = bn; }

    public String getUnit()                         { return unit; }
    public void setUnit(String unit)                { this.unit = unit; }

    /** Calculates totalPrice = (sellingPrice * quantity) - discount */
    public void calculateTotal() {
        if (sellingPrice != null && quantity > 0) {
            BigDecimal lineTotal = sellingPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal d = (discount != null) ? discount : BigDecimal.ZERO;
            this.totalPrice = lineTotal.subtract(d);
        }
    }
}
