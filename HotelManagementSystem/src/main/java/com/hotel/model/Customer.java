package com.hotel.model;

import java.io.Serializable;

/**
 * Customer — Entity model for hotel guests.
 *
 * Implements Serializable for Week 6: object serialization.
 * Demonstrates Week 1: Encapsulation with private fields and getters/setters.
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class Customer implements Serializable {

    // serialVersionUID required for stable serialization (Week 6)
    private static final long serialVersionUID = 1001L;

    // ---- Encapsulation: private fields ----
    private int customerId;
    private String fullName;
    private String email;
    private String phone;
    private String idProofType;
    private String idProofNumber;
    private String address;
    private String nationality;
    private String createdAt;

    // ============================================================
    // CONSTRUCTORS (Overloading — Week 1)
    // ============================================================

    /** Default constructor for JavaFX TableView and serialization */
    public Customer() {
        this.nationality = "Indian";
    }

    /** Minimal constructor for quick customer creation */
    public Customer(String fullName, String email, String phone) {
        this();  // Delegates to default, sets nationality
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    /** Full constructor for loading from database */
    public Customer(int customerId, String fullName, String email, String phone,
                    String idProofType, String idProofNumber,
                    String address, String nationality) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.idProofType = idProofType;
        this.idProofNumber = idProofNumber;
        this.address = address;
        this.nationality = (nationality != null) ? nationality : "Indian";
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    /** Returns true if all mandatory fields are filled. */
    public boolean isValid() {
        return fullName != null && !fullName.trim().isEmpty()
            && email != null && email.contains("@")
            && phone != null && phone.length() >= 10
            && idProofType != null && !idProofType.isEmpty()
            && idProofNumber != null && !idProofNumber.trim().isEmpty();
    }

    /** Returns a short display label for dropdowns and table cells. */
    public String getDisplayLabel() {
        return fullName + " (" + phone + ")";
    }

    // ============================================================
    // GETTERS & SETTERS (Encapsulation — Week 1)
    // ============================================================

    public int getCustomerId()               { return customerId; }
    public void setCustomerId(int id)        { this.customerId = id; }

    public String getFullName()              { return fullName; }
    public void setFullName(String n)        { this.fullName = n; }

    public String getEmail()                 { return email; }
    public void setEmail(String e)           { this.email = e; }

    public String getPhone()                 { return phone; }
    public void setPhone(String p)           { this.phone = p; }

    public String getIdProofType()           { return idProofType; }
    public void setIdProofType(String t)     { this.idProofType = t; }

    public String getIdProofNumber()         { return idProofNumber; }
    public void setIdProofNumber(String n)   { this.idProofNumber = n; }

    public String getAddress()               { return address; }
    public void setAddress(String a)         { this.address = a; }

    public String getNationality()           { return nationality; }
    public void setNationality(String n)     { this.nationality = n; }

    public String getCreatedAt()             { return createdAt; }
    public void setCreatedAt(String c)       { this.createdAt = c; }

    @Override
    public String toString() {
        return fullName + " | " + email + " | " + phone;
    }
}
