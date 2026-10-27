package com.pharmacy.model;

import java.time.LocalDateTime;

/**
 * Admin — represents an administrator account
 */
public class Admin {

    private int id;
    private String username;
    private String password;   // BCrypt hash — never expose plaintext
    private String fullName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Admin() {}

    public Admin(int id, String username, String fullName, String email, String phone) {
        this.id       = id;
        this.username = username;
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
    }

    // Getters and Setters
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public String getUsername()               { return username; }
    public void setUsername(String username)  { this.username = username; }

    public String getPassword()               { return password; }
    public void setPassword(String password)  { this.password = password; }

    public String getFullName()               { return fullName; }
    public void setFullName(String fullName)  { this.fullName = fullName; }

    public String getEmail()                  { return email; }
    public void setEmail(String email)        { this.email = email; }

    public String getPhone()                  { return phone; }
    public void setPhone(String phone)        { this.phone = phone; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime t) { this.createdAt = t; }

    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }

    @Override
    public String toString() {
        return "Admin{id=" + id + ", username='" + username + "', fullName='" + fullName + "'}";
    }
}
