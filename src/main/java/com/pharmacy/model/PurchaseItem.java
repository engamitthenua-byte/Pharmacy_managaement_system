package com.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * PurchaseItem — a single line item in a purchase invoice.
 * Records which medicine, which batch, quantity, and price.
 */
public class PurchaseItem {

    private int id;
    private int purchaseId;
    private int medicineId;
    private int batchId;
    private int quantity;
    private BigDecimal purchasePrice;
    private BigDecimal totalPrice;

    // For new purchases (not yet saved), store batch details
    private String batchNumber;
    private BigDecimal sellingPrice;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;

    // Derived — from JOIN queries
    private String medicineName;
    private String invoiceNumber;

    public PurchaseItem() {}

    public int getId()                               { return id; }
    public void setId(int id)                        { this.id = id; }

    public int getPurchaseId()                       { return purchaseId; }
    public void setPurchaseId(int purchaseId)        { this.purchaseId = purchaseId; }

    public int getMedicineId()                       { return medicineId; }
    public void setMedicineId(int medicineId)        { this.medicineId = medicineId; }

    public int getBatchId()                          { return batchId; }
    public void setBatchId(int batchId)              { this.batchId = batchId; }

    public int getQuantity()                         { return quantity; }
    public void setQuantity(int quantity)            { this.quantity = quantity; }

    public BigDecimal getPurchasePrice()             { return purchasePrice; }
    public void setPurchasePrice(BigDecimal price)   { this.purchasePrice = price; }

    public BigDecimal getTotalPrice()                { return totalPrice; }
    public void setTotalPrice(BigDecimal total)      { this.totalPrice = total; }

    public String getBatchNumber()                   { return batchNumber; }
    public void setBatchNumber(String bn)            { this.batchNumber = bn; }

    public BigDecimal getSellingPrice()              { return sellingPrice; }
    public void setSellingPrice(BigDecimal price)    { this.sellingPrice = price; }

    public LocalDate getManufacturingDate()          { return manufacturingDate; }
    public void setManufacturingDate(LocalDate d)    { this.manufacturingDate = d; }

    public LocalDate getExpiryDate()                 { return expiryDate; }
    public void setExpiryDate(LocalDate d)           { this.expiryDate = d; }

    public String getMedicineName()                  { return medicineName; }
    public void setMedicineName(String name)         { this.medicineName = name; }

    public String getInvoiceNumber()                 { return invoiceNumber; }
    public void setInvoiceNumber(String num)         { this.invoiceNumber = num; }

    /** Calculates and sets totalPrice = quantity * purchasePrice */
    public void calculateTotal() {
        if (purchasePrice != null && quantity > 0) {
            this.totalPrice = purchasePrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
