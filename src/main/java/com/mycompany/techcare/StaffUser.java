package com.mycompany.techcare;

public class StaffUser {

    private final int staffUserId;
    private final String username;
    private final String role;

    public StaffUser(int staffUserId, String username, String role) {
        this.staffUserId = staffUserId;
        this.username = username;
        this.role = role;
    }

    public int getStaffUserId() {
        return staffUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}