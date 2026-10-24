package com.mycompany.techcare;

import java.awt.GridLayout;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StaffAccountSetup {

    public static void main(String[] args) {
        JTextField usernameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JPasswordField confirmField = new JPasswordField(20);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Admin username:"));
        form.add(usernameField);
        form.add(new JLabel("Email (optional):"));
        form.add(emailField);
        form.add(new JLabel("Password (at least 8 characters):"));
        form.add(passwordField);
        form.add(new JLabel("Confirm password:"));
        form.add(confirmField);

        int choice = JOptionPane.showConfirmDialog(
                null,
                form,
                "Create the first TechCare admin",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        char[] password = passwordField.getPassword();
        char[] confirmation = confirmField.getPassword();

        try {
            if (username.isEmpty()) {
                throw new IllegalArgumentException("Username is required.");
            }

            if (password.length < 8) {
                throw new IllegalArgumentException(
                        "Use a password with at least 8 characters."
                );
            }

            if (!Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException(
                        "The passwords do not match."
                );
            }

            createFirstAdmin(username, email, password);

            JOptionPane.showMessageDialog(
                    null,
                    "Admin account created successfully for " + username + "."
            );

        } catch (IllegalArgumentException | SQLException
                | GeneralSecurityException exception) {
            JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    "Setup Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');
        }
    }

    private static void createFirstAdmin(
            String username,
            String email,
            char[] password
    ) throws SQLException, GeneralSecurityException {

        try (Connection connection = DatabaseConnection.getConnection()) {
            String countSql = "SELECT COUNT(*) FROM staff_user";

            try (PreparedStatement countStatement =
                         connection.prepareStatement(countSql);
                 ResultSet resultSet = countStatement.executeQuery()) {

                resultSet.next();

                if (resultSet.getInt(1) > 0) {
                    throw new SQLException(
                            "A staff account already exists. "
                            + "This setup is only for the first admin."
                    );
                }
            }

            String insertSql = """
                    INSERT INTO staff_user (username, email, password_hash, role)
                    VALUES (?, ?, ?, 'ADMIN')
                    """;

            try (PreparedStatement insertStatement =
                         connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, username);
                insertStatement.setString(
                        2,
                        email.isEmpty() ? null : email
                );
                insertStatement.setString(
                        3,
                        PasswordUtil.hashPassword(password)
                );

                insertStatement.executeUpdate();
            }
        }
    }
}