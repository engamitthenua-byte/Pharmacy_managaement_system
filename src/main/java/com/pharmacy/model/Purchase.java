package com.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase — purchase invoice header.
 * One Purchase contains a list of PurchaseItem objects.
 */
public class Purchase {

    private int id;
    private String invoiceNumber;
    private int supplierId;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDate purchaseDate;
    private LocalDateTime createdAt;

    // Derived
    private String supplierName;
    private List<PurchaseItem> items = new ArrayList<>();

    public Purchase() {}

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }

    public String getInvoiceNumber()                { return invoiceNumber; }
    public void setInvoiceNumber(String num)        { this.invoiceNumber = num; }

    public int getSupplierId()                      { return supplierId; }
    public void setSupplierId(int supplierId)       { this.supplierId = supplierId; }

    public BigDecimal getTotalAmount()              { return totalAmount; }
    public void setTotalAmount(BigDecimal amount)   { this.totalAmount = amount; }

    public String getNotes()                        { return notes; }
    public void setNotes(String notes)              { this.notes = notes; }

    public LocalDate getPurchaseDate()              { return purchaseDate; }
    public void setPurchaseDate(LocalDate date)     { this.purchaseDate = date; }

    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }

    public String getSupplierName()                 { return supplierName; }
    public void setSupplierName(String name)        { this.supplierName = name; }

    public List<PurchaseItem> getItems()            { return items; }
    public void setItems(List<PurchaseItem> items)  { this.items = items; }

    public void addItem(PurchaseItem item)          { this.items.add(item); }

    @Override
    public String toString() {
        return "Purchase{id=" + id + ", invoice='" + invoiceNumber +
               "', total=" + totalAmount + ", items=" + items.size() + "}";
    }
}
