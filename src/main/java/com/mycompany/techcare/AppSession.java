package com.mycompany.techcare;

public final class AppSession {

    private static StaffUser currentUser;

    private AppSession() {
    }

    public static void setCurrentUser(StaffUser user) {
        currentUser = user;
    }

    public static StaffUser getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = null;
    }
}