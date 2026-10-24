package com.mycompany.techcare;

import java.sql.Connection;
import javax.swing.JOptionPane;

public class DatabaseCheck {

    public static void main(String[] args) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Connected to the techcare database successfully!"
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Connection failed: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}