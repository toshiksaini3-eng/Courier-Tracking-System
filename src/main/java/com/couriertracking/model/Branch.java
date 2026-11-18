package com.couriertracking.model;

import java.sql.Timestamp;

/**
 * Branch entity representing physical courier hubs and delivery centers.
 * Demonstrates OOP Encapsulation with private fields and standard accessors.
 */
public class Branch {
    private int id;
    private String branchCode;
    private String branchName;
    private String city;
    private String state;
    private String contactPhone;
    private String address;
    private boolean active;
    private Timestamp createdAt;

    public Branch() {
        this.active = true;
    }

    public Branch(int id, String branchCode, String branchName, String city, String state, 
                  String contactPhone, String address, boolean active) {
        this.id = id;
        this.branchCode = branchCode;
        this.branchName = branchName;
        this.city = city;
        this.state = state;
        this.contactPhone = contactPhone;
        this.address = address;
        this.active = active;
    }

    public Branch(String branchCode, String branchName, String city, String state, 
                  String contactPhone, String address) {
        this(0, branchCode, branchName, city, state, contactPhone, address, true);
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", branchCode, branchName, city);
    }
}
