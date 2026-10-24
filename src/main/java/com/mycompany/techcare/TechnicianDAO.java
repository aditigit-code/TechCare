package com.mycompany.techcare;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TechnicianDAO {

    public record TechnicianRow(
            int technicianId,
            int staffUserId,
            String username,
            String email,
            String specialization,
            String employmentStatus,
            boolean loginActive) {
    }

    public void createTechnician(
            String username,
            String email,
            char[] password,
            String specialization)
            throws SQLException, GeneralSecurityException {

        String passwordHash = PasswordUtil.hashPassword(password);

        String insertStaffSql =
                "INSERT INTO staff_user "
                + "(username, email, password_hash, role, is_active) "
                + "VALUES (?, ?, ?, 'TECHNICIAN', TRUE)";

        String insertProfileSql =
                "INSERT INTO technician_profile "
                + "(staff_user_id, specialization, employment_status) "
                + "VALUES (?, ?, 'ACTIVE')";

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int staffUserId;

                try (PreparedStatement statement = connection.prepareStatement(
                        insertStaffSql,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, username.trim());

                    if (email == null || email.isBlank()) {
                        statement.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        statement.setString(2, email.trim());
                    }

                    statement.setString(3, passwordHash);
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Creating the staff account did not return an ID.");
                        }
                        staffUserId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(insertProfileSql)) {
                    statement.setInt(1, staffUserId);

                    if (specialization == null || specialization.isBlank()) {
                        statement.setNull(2, java.sql.Types.VARCHAR);
                    } else {
                        statement.setString(2, specialization.trim());
                    }

                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public List<TechnicianRow> findAll() throws SQLException {
        String sql =
                "SELECT tp.technician_id, su.staff_user_id, su.username, "
                + "su.email, tp.specialization, tp.employment_status, su.is_active "
                + "FROM technician_profile tp "
                + "JOIN staff_user su ON su.staff_user_id = tp.staff_user_id "
                + "ORDER BY su.username";

        List<TechnicianRow> technicians = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                technicians.add(new TechnicianRow(
                        results.getInt("technician_id"),
                        results.getInt("staff_user_id"),
                        results.getString("username"),
                        results.getString("email"),
                        results.getString("specialization"),
                        results.getString("employment_status"),
                        results.getBoolean("is_active")
                ));
            }
        }

        return technicians;
    }

    public void setActive(int technicianId, boolean active)
            throws SQLException {

        String sql =
                "UPDATE technician_profile tp "
                + "JOIN staff_user su ON su.staff_user_id = tp.staff_user_id "
                + "SET tp.employment_status = ?, su.is_active = ? "
                + "WHERE tp.technician_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, active ? "ACTIVE" : "INACTIVE");
            statement.setBoolean(2, active);
            statement.setInt(3, technicianId);
            statement.executeUpdate();
        }
    }
}