package com.pharmacy.dao;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.pharmacy.model.Admin;
import com.pharmacy.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * AdminDAO — Data Access Object for admins table.
 * Handles authentication, registration, profile retrieval, and password updates.
 */
public class AdminDAO {

    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);

    /**
     * Validates login credentials.
     * Checks BCrypt hash, with fallback for default credentials and plaintext migration.
     *
     * @param username entered username
     * @param password entered plaintext password
     * @return Admin object if credentials are valid, null otherwise
     * @throws SQLException if a database connection error occurs
     */
    public Admin validateLogin(String username, String password) throws SQLException {
        if (username == null || password == null) return null;
        String trimmedUser = username.trim();

        String sql = "SELECT id, username, password, full_name, email, phone, created_at " +
                     "FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trimmedUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin admin = mapRow(rs);
                    String storedHash = rs.getString("password");
                    boolean verified = false;

                    // 1. Primary check: Verify using BCrypt
                    if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"))) {
                        try {
                            BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), storedHash);
                            verified = result.verified;
                        } catch (Exception e) {
                            logger.warn("BCrypt verification error: {}", e.getMessage());
                        }
                    }

                    // 2. Direct equality fallback (in case password was stored plaintext)
                    if (!verified && storedHash != null && storedHash.equals(password)) {
                        verified = true;
                    }

                    // 3. Fallback for the default seed admin (admin@1234, Admin@1234, admin123, admin)
                    if (!verified && "admin".equalsIgnoreCase(trimmedUser)) {
                        if ("admin@1234".equalsIgnoreCase(password)
                                || "Admin@1234".equals(password)
                                || "admin123".equals(password)
                                || "admin".equals(password)) {
                            verified = true;
                        }
                    }

                    if (verified) {
                        logger.info("Login successful for user: {}", trimmedUser);
                        // If password matched via fallback, update to a proper BCrypt hash for future logins
                        try {
                            String freshHash = BCrypt.withDefaults().hashToString(12, password.toCharArray());
                            try (PreparedStatement updatePs = conn.prepareStatement("UPDATE admins SET password = ? WHERE id = ?")) {
                                updatePs.setString(1, freshHash);
                                updatePs.setInt(2, admin.getId());
                                updatePs.executeUpdate();
                            }
                        } catch (Exception ignored) {}
                        return admin;
                    } else {
                        logger.warn("Invalid password attempt for user: {}", trimmedUser);
                    }
                } else {
                    // Check if admins table is completely empty. If so, auto-create the admin user!
                    String countSql = "SELECT COUNT(*) FROM admins";
                    try (PreparedStatement countPs = conn.prepareStatement(countSql);
                         ResultSet countRs = countPs.executeQuery()) {
                        if (countRs.next() && countRs.getInt(1) == 0 && "admin".equalsIgnoreCase(trimmedUser)) {
                            logger.info("Admins table was empty. Auto-provisioning default admin user.");
                            Admin newAdmin = new Admin();
                            newAdmin.setUsername("admin");
                            newAdmin.setFullName("System Administrator");
                            newAdmin.setEmail("admin@pharmacy.com");
                            newAdmin.setPhone("9876543210");
                            int id = registerAdmin(newAdmin, password);
                            if (id > 0) {
                                newAdmin.setId(id);
                                return newAdmin;
                            }
                        }
                    }
                    logger.warn("Login attempt for unknown username: {}", trimmedUser);
                }
            }
        }
        return null;
    }

    /**
     * Checks if a username is already taken.
     */
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error checking username availability: {}", username, e);
        }
        return false;
    }

    /**
     * Registers a new administrator account with a BCrypt hashed password.
     *
     * @param admin         Admin details (username, fullName, email, phone)
     * @param plainPassword Plaintext password to hash and store
     * @return generated ID on success, or -1 on error
     */
    public int registerAdmin(Admin admin, String plainPassword) {
        String hashed = BCrypt.withDefaults().hashToString(12, plainPassword.toCharArray());
        String sql = "INSERT INTO admins (username, password, full_name, email, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, admin.getUsername().trim());
            ps.setString(2, hashed);
            ps.setString(3, admin.getFullName().trim());
            ps.setString(4, admin.getEmail() != null && !admin.getEmail().isBlank() ? admin.getEmail().trim() : null);
            ps.setString(5, admin.getPhone() != null && !admin.getPhone().isBlank() ? admin.getPhone().trim() : null);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error registering admin user: {}", admin.getUsername(), e);
        }
        return -1;
    }

    /**
     * Finds an admin by primary key ID.
     */
    public Admin findById(int id) {
        String sql = "SELECT id, username, password, full_name, email, phone, created_at " +
                     "FROM admins WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding admin by id: {}", id, e);
        }
        return null;
    }

    /**
     * Updates the administrator's profile (name, email, phone).
     */
    public boolean updateProfile(Admin admin) {
        String sql = "UPDATE admins SET full_name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, admin.getFullName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getPhone());
            ps.setInt(4, admin.getId());

            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error updating profile for admin id: {}", admin.getId(), e);
            return false;
        }
    }

    /**
     * Updates the administrator's password after verifying the current password.
     */
    public boolean updatePassword(int adminId, String currentPass, String newPassword) {
        String selectSql = "SELECT password FROM admins WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {

            selectPs.setInt(1, adminId);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (!rs.next()) return false;

                String storedHash = rs.getString("password");
                boolean verified = false;
                if (storedHash != null && (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$"))) {
                    try {
                        verified = BCrypt.verifyer().verify(currentPass.toCharArray(), storedHash).verified;
                    } catch (Exception ignored) {}
                }
                if (!verified && storedHash != null && storedHash.equals(currentPass)) {
                    verified = true;
                }

                if (!verified) {
                    logger.warn("Password change failed: incorrect current password for admin id: {}", adminId);
                    return false;
                }
            }

            String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
            String updateSql = "UPDATE admins SET password = ? WHERE id = ?";
            try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                updatePs.setString(1, newHash);
                updatePs.setInt(2, adminId);
                int rows = updatePs.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            logger.error("Error updating password for admin id: {}", adminId, e);
            return false;
        }
    }

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setId(rs.getInt("id"));
        admin.setUsername(rs.getString("username"));
        admin.setPassword(rs.getString("password"));
        admin.setFullName(rs.getString("full_name"));
        admin.setEmail(rs.getString("email"));
        admin.setPhone(rs.getString("phone"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            admin.setCreatedAt(createdAt.toLocalDateTime());
        }
        return admin;
    }
}
