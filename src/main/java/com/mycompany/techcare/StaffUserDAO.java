package com.mycompany.techcare;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffUserDAO {

    public StaffUser authenticate(String username, char[] password)
            throws SQLException, GeneralSecurityException {

        String sql = """
                SELECT staff_user_id, username, password_hash, role, is_active
                FROM staff_user
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                if (!resultSet.getBoolean("is_active")) {
                    return null;
                }

                String storedHash = resultSet.getString("password_hash");

                if (!PasswordUtil.verifyPassword(password, storedHash)) {
                    return null;
                }

                return new StaffUser(
                        resultSet.getInt("staff_user_id"),
                        resultSet.getString("username"),
                        resultSet.getString("role")
                );
            }
        }
    }
}