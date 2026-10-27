package com.pharmacy.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * AppUtil — General application helper methods
 * ---------------------------------------------
 * Contains miscellaneous utility methods used across
 * the application such as invoice number generation
 * and date formatting.
 */
public class AppUtil {

    private static final DateTimeFormatter DISPLAY_DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static final DateTimeFormatter DB_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private AppUtil() {}

    /**
     * Generates a unique purchase invoice number.
     * Format: PUR-YYYYMMDD-NNNNN (e.g., PUR-20241015-00001)
     *
     * @param sequence a sequential number (e.g., from DB count + 1)
     * @return formatted invoice number string
     */
    public static String generatePurchaseInvoiceNumber(long sequence) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("PUR-%s-%05d", date, sequence);
    }

    /**
     * Generates a unique sales invoice number.
     * Format: SAL-YYYYMMDD-NNNNN (e.g., SAL-20241015-00001)
     */
    public static String generateSalesInvoiceNumber(long sequence) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("SAL-%s-%05d", date, sequence);
    }

    /**
     * Formats a LocalDate for display: dd-MM-yyyy
     */
    public static String formatDateDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_DATE_FMT);
    }

    /**
     * Formats a LocalDate for database/HTML input: yyyy-MM-dd
     */
    public static String formatDateDb(LocalDate date) {
        if (date == null) return "";
        return date.format(DB_DATE_FMT);
    }

    /**
     * Parses a display date string (dd-MM-yyyy) to LocalDate.
     */
    public static LocalDate parseDisplayDate(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim(), DISPLAY_DATE_FMT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Null-safe string check — returns empty string if null.
     */
    public static String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Returns default value if input is null or blank.
     */
    public static String defaultIfBlank(String s, String defaultVal) {
        return (s == null || s.isBlank()) ? defaultVal : s;
    }

    /**
     * Builds a simple pagination info string for display.
     * e.g., "Showing 11-20 of 50 records"
     */
    public static String paginationInfo(int page, int pageSize, int total) {
        if (total == 0) return "No records found.";
        int from = (page - 1) * pageSize + 1;
        int to   = Math.min(page * pageSize, total);
        return String.format("Showing %d–%d of %d record%s", from, to, total, total == 1 ? "" : "s");
    }

    /**
     * Calculates the total number of pages for pagination.
     */
    public static int totalPages(int totalRecords, int pageSize) {
        if (pageSize <= 0) return 1;
        return (int) Math.ceil((double) totalRecords / pageSize);
    }
}
