package com.pharmacy.model;

import java.time.LocalDateTime;

/**
 * InventoryTransaction — immutable audit record for every stock movement.
 * Positive quantityChange = stock in (purchase, adjustment in, return)
 * Negative quantityChange = stock out (sale, adjustment out)
 */
public class InventoryTransaction {

    public enum TransactionType {
        PURCHASE, SALE, ADJUSTMENT_IN, ADJUSTMENT_OUT, RETURN
    }

    private int id;
    private int medicineId;
    private int batchId;
    private TransactionType transactionType;
    private int quantityChange;    // positive or negative
    private String referenceNumber;
    private String reason;
    private LocalDateTime transactionDate;

    // Derived
    private String medicineName;
    private String batchNumber;

    public InventoryTransaction() {}

    public int getId()                                       { return id; }
    public void setId(int id)                               { this.id = id; }

    public int getMedicineId()                              { return medicineId; }
    public void setMedicineId(int medicineId)               { this.medicineId = medicineId; }

    public int getBatchId()                                 { return batchId; }
    public void setBatchId(int batchId)                     { this.batchId = batchId; }

    public TransactionType getTransactionType()             { return transactionType; }
    public void setTransactionType(TransactionType type)    { this.transactionType = type; }

    public int getQuantityChange()                          { return quantityChange; }
    public void setQuantityChange(int quantityChange)       { this.quantityChange = quantityChange; }

    public String getReferenceNumber()                      { return referenceNumber; }
    public void setReferenceNumber(String ref)              { this.referenceNumber = ref; }

    public String getReason()                               { return reason; }
    public void setReason(String reason)                    { this.reason = reason; }

    public LocalDateTime getTransactionDate()               { return transactionDate; }
    public void setTransactionDate(LocalDateTime t)         { this.transactionDate = t; }

    public String getMedicineName()                         { return medicineName; }
    public void setMedicineName(String name)                { this.medicineName = name; }

    public String getBatchNumber()                          { return batchNumber; }
    public void setBatchNumber(String bn)                   { this.batchNumber = bn; }

    /** Returns a user-friendly display label for the transaction type */
    public String getTransactionTypeLabel() {
        return switch (transactionType) {
            case PURCHASE       -> "Purchase";
            case SALE           -> "Sale";
            case ADJUSTMENT_IN  -> "Adjustment (In)";
            case ADJUSTMENT_OUT -> "Adjustment (Out)";
            case RETURN         -> "Return";
            default             -> "Unknown";
        };
    }
}
