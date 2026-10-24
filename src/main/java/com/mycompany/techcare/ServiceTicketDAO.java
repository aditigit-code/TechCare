package com.mycompany.techcare;

import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceTicketDAO {

    public record TechnicianChoice(Integer technicianId, String username) {
        @Override
        public String toString() {
            return username;
        }
    }

    public int createTicket(ServiceTicket ticket) throws SQLException {
        String ticketSql =
                "INSERT INTO service_ticket "
                + "(device_id, category_id, created_by, status, priority, reported_issue) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        String historySql =
                "INSERT INTO ticket_status_history "
                + "(ticket_id, old_status, new_status, changed_by, reason) "
                + "VALUES (?, NULL, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int ticketId;

                try (PreparedStatement statement = connection.prepareStatement(
                        ticketSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, ticket.getDeviceId());
                    statement.setInt(2, ticket.getCategoryId());
                    statement.setInt(3, ticket.getCreatedBy());
                    statement.setString(4, "RECEIVED");
                    statement.setString(5, ticket.getPriority());
                    statement.setString(6, ticket.getReportedIssue());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                    "Creating the ticket did not return an ID.");
                        }
                        ticketId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(historySql)) {
                    statement.setInt(1, ticketId);
                    statement.setString(2, "RECEIVED");
                    statement.setInt(3, ticket.getCreatedBy());
                    statement.setString(4, "Ticket created");
                    statement.executeUpdate();
                }

                connection.commit();
                ticket.setTicketId(ticketId);
                return ticketId;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    public List<TechnicianChoice> findActiveTechnicians() throws SQLException {
        String sql =
                "SELECT tp.technician_id, su.username "
                + "FROM technician_profile tp "
                + "JOIN staff_user su ON su.staff_user_id = tp.staff_user_id "
                + "WHERE tp.employment_status = 'ACTIVE' "
                + "AND su.is_active = TRUE "
                + "ORDER BY su.username";

        List<TechnicianChoice> technicians = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                technicians.add(new TechnicianChoice(
                        results.getInt("technician_id"),
                        results.getString("username")
                ));
            }
        }

        return technicians;
    }

    public void assignTechnician(
            int ticketId,
            Integer technicianId,
            int assignedBy,
            String reason) throws SQLException {

        String updateSql =
                "UPDATE service_ticket "
                + "SET assigned_technician_id = ? "
                + "WHERE ticket_id = ?";

        String historySql =
                "INSERT INTO ticket_assignment_history "
                + "(ticket_id, technician_id, assigned_by, reason) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    if (technicianId == null) {
                        statement.setNull(1, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(1, technicianId);
                    }
                    statement.setInt(2, ticketId);

                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("Ticket not found.");
                    }
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(historySql)) {
                    statement.setInt(1, ticketId);
                    if (technicianId == null) {
                        statement.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(2, technicianId);
                    }
                    statement.setInt(3, assignedBy);
                    setOptionalText(statement, 4, reason);
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

    public void updateStatus(
            int ticketId,
            String newStatus,
            int changedBy,
            String reason) throws SQLException {

        String selectSql =
                "SELECT status FROM service_ticket "
                + "WHERE ticket_id = ? FOR UPDATE";

        String updateSql =
                "UPDATE service_ticket SET status = ? "
                + "WHERE ticket_id = ?";

        String historySql =
                "INSERT INTO ticket_status_history "
                + "(ticket_id, old_status, new_status, changed_by, reason) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                String oldStatus;

                try (PreparedStatement statement =
                             connection.prepareStatement(selectSql)) {
                    statement.setInt(1, ticketId);

                    try (ResultSet results = statement.executeQuery()) {
                        if (!results.next()) {
                            throw new SQLException("Ticket not found.");
                        }
                        oldStatus = results.getString("status");
                    }
                }

                if (oldStatus.equals(newStatus)) {
                    connection.commit();
                    return;
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(updateSql)) {
                    statement.setString(1, newStatus);
                    statement.setInt(2, ticketId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(historySql)) {
                    statement.setInt(1, ticketId);
                    statement.setString(2, oldStatus);
                    statement.setString(3, newStatus);
                    statement.setInt(4, changedBy);
                    setOptionalText(statement, 5, reason);
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

    private void setOptionalText(
            PreparedStatement statement,
            int parameterIndex,
            String value) throws SQLException {

        if (value == null || value.isBlank()) {
            statement.setNull(parameterIndex, java.sql.Types.VARCHAR);
        } else {
            statement.setString(parameterIndex, value.trim());
        }
    }
}