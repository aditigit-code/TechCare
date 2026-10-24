package com.mycompany.techcare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public record Summary(
            int openTickets,
            int inProgress,
            int readyForPickup,
            int lowStockItems) {
    }

    public record RecentTicket(
            int ticketId,
            String customer,
            String device,
            String status,
            String receivedAt) {
    }

    public Summary getSummary() throws SQLException {
        String sql =
                "SELECT "
                + "(SELECT COUNT(*) FROM service_ticket "
                + " WHERE status NOT IN ('DELIVERED', 'CANCELLED')) AS open_count, "
                + "(SELECT COUNT(*) FROM service_ticket "
                + " WHERE status = 'IN_PROGRESS') AS progress_count, "
                + "(SELECT COUNT(*) FROM service_ticket "
                + " WHERE status = 'READY_FOR_PICKUP') AS ready_count, "
                + "(SELECT COUNT(*) FROM spare_part "
                + " WHERE is_active = TRUE "
                + " AND quantity_in_stock <= reorder_level) AS low_stock_count";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            if (result.next()) {
                return new Summary(
                        result.getInt("open_count"),
                        result.getInt("progress_count"),
                        result.getInt("ready_count"),
                        result.getInt("low_stock_count")
                );
            }
        }

        return new Summary(0, 0, 0, 0);
    }

    public List<RecentTicket> getRecentTickets() throws SQLException {
        String sql =
                "SELECT st.ticket_id, c.full_name, d.brand, d.model, "
                + "st.status, st.received_at "
                + "FROM service_ticket st "
                + "JOIN device d ON d.device_id = st.device_id "
                + "JOIN customer c ON c.customer_id = d.customer_id "
                + "ORDER BY st.received_at DESC "
                + "LIMIT 5";

        List<RecentTicket> tickets = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                tickets.add(new RecentTicket(
                        results.getInt("ticket_id"),
                        results.getString("full_name"),
                        results.getString("brand") + " "
                                + results.getString("model"),
                        results.getString("status"),
                        results.getTimestamp("received_at").toString()
                ));
            }
        }

        return tickets;
    }
}