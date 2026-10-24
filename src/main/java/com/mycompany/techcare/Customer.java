package com.mycompany.techcare;

public class Customer {

    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private boolean active;

    public Customer(int customerId, String fullName, String phone,
                    String email, String address, boolean active) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.active = active;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
}