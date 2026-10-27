package com.pharmacy.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sale — sales invoice header.
 * One Sale contains a list of SaleItem objects.
 * customer_id is nullable — NULL = walk-in customer.
 */
public class Sale {

    private int id;
    private String invoiceNumber;
    private Integer customerId;     // Integer (nullable) — null means walk-in
    private String customerName;   // Walk-in name if no registered customer
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String notes;
    private LocalDate saleDate;
    private LocalDateTime createdAt;

    // Derived
    private String registeredCustomerName;  // From customers table JOIN
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {}

    public int getId()                                  { return id; }
    public void setId(int id)                           { this.id = id; }

    public String getInvoiceNumber()                    { return invoiceNumber; }
    public void setInvoiceNumber(String num)            { this.invoiceNumber = num; }

    public Integer getCustomerId()                      { return customerId; }
    public void setCustomerId(Integer customerId)       { this.customerId = customerId; }

    public String getCustomerName()                     { return customerName; }
    public void setCustomerName(String name)            { this.customerName = name; }

    public BigDecimal getSubtotal()                     { return subtotal; }
    public void setSubtotal(BigDecimal subtotal)        { this.subtotal = subtotal; }

    public BigDecimal getDiscount()                     { return discount; }
    public void setDiscount(BigDecimal discount)        { this.discount = discount; }

    public BigDecimal getTaxAmount()                    { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount)      { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount()                  { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount)  { this.totalAmount = totalAmount; }

    public String getNotes()                            { return notes; }
    public void setNotes(String notes)                  { this.notes = notes; }

    public LocalDate getSaleDate()                      { return saleDate; }
    public void setSaleDate(LocalDate saleDate)         { this.saleDate = saleDate; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime t)           { this.createdAt = t; }

    public String getRegisteredCustomerName()           { return registeredCustomerName; }
    public void setRegisteredCustomerName(String name)  { this.registeredCustomerName = name; }

    public List<SaleItem> getItems()                    { return items; }
    public void setItems(List<SaleItem> items)          { this.items = items; }

    public void addItem(SaleItem item)                  { this.items.add(item); }

    /** Returns display name — registered customer name or walk-in name */
    public String getDisplayCustomerName() {
        if (registeredCustomerName != null && !registeredCustomerName.isBlank()) {
            return registeredCustomerName;
        }
        return (customerName != null && !customerName.isBlank()) ? customerName : "Walk-in Customer";
    }

    @Override
    public String toString() {
        return "Sale{id=" + id + ", invoice='" + invoiceNumber +
               "', total=" + totalAmount + ", items=" + items.size() + "}";
    }
}
