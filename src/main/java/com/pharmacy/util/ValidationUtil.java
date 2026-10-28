package com.pharmacy.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * ValidationUtil — Server-Side Input Validation
 * -----------------------------------------------
 * All form inputs are validated on the server using this utility.
 * Never trust data sent from the browser — validate everything here.
 *
 * Methods return error strings (or null if valid) so callers can
 * collect and display specific messages to users.
 */
public class ValidationUtil {

    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

    // Email regex — basic RFC-5322 compatible pattern
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Phone: 7–15 digits, optional +, spaces, dashes, parentheses
    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9\\s\\-().]{7,20}$");

    private ValidationUtil() {}

    // ─────────────────────────────────────────────
    // String helpers
    // ─────────────────────────────────────────────

    /** Returns null if valid, error message if blank/null. */
    public static String requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required.";
        }
        return null;
    }

    /** Returns trimmed parameter from request, or empty string if missing. */
    public static String getParam(HttpServletRequest req, String name) {
        String val = req.getParameter(name);
        return (val == null) ? "" : val.trim();
    }

    /** Returns null if value does not exceed maxLength, else error. */
    public static String maxLength(String value, int max, String fieldName) {
        if (value != null && value.length() > max) {
            return fieldName + " must not exceed " + max + " characters.";
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // Email / Phone
    // ─────────────────────────────────────────────

    /** Returns null if email is valid or empty (optional field), else error. */
    public static String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) return null; // optional
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Invalid email address format.";
        }
        return null;
    }

    /** Returns null if phone is valid or empty (optional field), else error. */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return null; // optional
        if (!PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Invalid phone number. Use digits, spaces, +, -, ( ) only (7–20 characters).";
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // Numeric helpers
    // ─────────────────────────────────────────────

    /** Parses a positive integer from a string parameter. Returns -1 on failure. */
    public static int parsePositiveInt(String value) {
        try {
            int v = Integer.parseInt(value.trim());
            return (v > 0) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Parses a non-negative integer. Returns -1 on failure. */
    public static int parseNonNegativeInt(String value) {
        try {
            int v = Integer.parseInt(value.trim());
            return (v >= 0) ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /** Parses a non-negative BigDecimal. Returns null on failure. */
    public static BigDecimal parseNonNegativeDecimal(String value) {
        try {
            BigDecimal bd = new BigDecimal(value.trim());
            return (bd.compareTo(BigDecimal.ZERO) >= 0) ? bd : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Parses a BigDecimal with a default fallback value if null, empty, or invalid. */
    public static BigDecimal parseBigDecimal(String value, BigDecimal defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /** Returns error string if value is null or negative. */
    public static String requirePositiveDecimal(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return fieldName + " must be a positive number.";
        }
        return null;
    }

    /** Returns error string if value is null or negative. */
    public static String requireNonNegativeDecimal(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return fieldName + " must be zero or a positive number.";
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // Date helpers
    // ─────────────────────────────────────────────

    /** Parses a LocalDate from yyyy-MM-dd string. Returns null if invalid/empty. */
    public static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Returns error if date string is not a valid yyyy-MM-dd date. */
    public static String validateDate(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required.";
        }
        if (parseDate(value) == null) {
            return fieldName + " must be a valid date (YYYY-MM-DD).";
        }
        return null;
    }

    /** Returns error if expiry date is not after manufacturing date. */
    public static String validateExpiryAfterManufacturing(LocalDate mfgDate, LocalDate expiryDate) {
        if (mfgDate != null && expiryDate != null && !expiryDate.isAfter(mfgDate)) {
            return "Expiry date must be after the manufacturing date.";
        }
        return null;
    }

    /** Returns error if expiry date is in the past. */
    public static String validateExpiryNotPast(LocalDate expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(LocalDate.now())) {
            return "Expiry date cannot be in the past for new stock.";
        }
        return null;
    }

    // ─────────────────────────────────────────────
    // HTML encoding — XSS prevention
    // ─────────────────────────────────────────────

    /**
     * Escapes HTML special characters to prevent XSS.
     * Use this when inserting user input into HTML output outside of JSTL.
     */
    public static String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&#x27;");
    }

    // ─────────────────────────────────────────────
    // ID validation
    // ─────────────────────────────────────────────

    /** Returns -1 if the parameter is not a valid positive integer ID. */
    public static int parseId(HttpServletRequest req, String paramName) {
        String val = req.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) return -1;
        try {
            int id = Integer.parseInt(val.trim());
            return (id > 0) ? id : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
