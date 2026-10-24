package com.mycompany.techcare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/techcare?serverTimezone=UTC";
    private static final String USER = "root";

    // Kept in memory only; the password is not written into the source code.
    private static char[] password;

    public static Connection getConnection() throws SQLException {
        if (password == null) {
            JPasswordField passwordField = new JPasswordField();

            int result = JOptionPane.showConfirmDialog(
                    null,
                    passwordField,
                    "Enter your MySQL root password",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                throw new SQLException("MySQL password entry was cancelled.");
            }

            password = passwordField.getPassword();
        }

        return DriverManager.getConnection(URL, USER, new String(password));
    }
}