package com.mycompany.techcare;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportsDAO {

    public record StatusCount(String status, int count) {
    }

    public record FinancialSummary(
            int invoiceCount,
            BigDecimal invoiced,
            BigDecimal received,
            BigDecimal outstanding) {
    }

    public record LowStockPart(
            String sku,
            String name,
            int quantity,
            int reorderLevel) {
    }

    public List<StatusCount> getTicketStatusCounts() throws SQLException {
        String sql =
                "SELECT status, COUNT(*) AS ticket_count "
                + "FROM service_ticket "
                + "GROUP BY status "
                + "ORDER BY status";

        List<StatusCount> counts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                counts.add(new StatusCount(
                        results.getString("status"),
                        results.getInt("ticket_count")
                ));
            }
        }

        return counts;
    }

    public FinancialSummary getFinancialSummary() throws SQLException {
        String sql =
                "SELECT COUNT(*) AS invoice_count, "
                + "COALESCE(SUM(total_amount), 0) AS invoiced, "
                + "COALESCE(SUM(paid_amount), 0) AS received, "
                + "COALESCE(SUM(total_amount - paid_amount), 0) AS outstanding "
                + "FROM ("
                + "  SELECT i.invoice_id, i.total_amount, "
                + "         COALESCE(SUM(p.amount), 0) AS paid_amount "
                + "  FROM invoice i "
                + "  LEFT JOIN payment p ON p.invoice_id = i.invoice_id "
                + "  GROUP BY i.invoice_id, i.total_amount"
                + ") AS invoice_totals";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {
                return new FinancialSummary(
                        results.getInt("invoice_count"),
                        results.getBigDecimal("invoiced"),
                        results.getBigDecimal("received"),
                        results.getBigDecimal("outstanding")
                );
            }
        }

        return new FinancialSummary(
                0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public List<LowStockPart> getLowStockParts() throws SQLException {
        String sql =
                "SELECT sku, part_name, quantity_in_stock, reorder_level "
                + "FROM spare_part "
                + "WHERE is_active = TRUE "
                + "AND quantity_in_stock <= reorder_level "
                + "ORDER BY quantity_in_stock, part_name";

        List<LowStockPart> parts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                parts.add(new LowStockPart(
                        results.getString("sku"),
                        results.getString("part_name"),
                        results.getInt("quantity_in_stock"),
                        results.getInt("reorder_level")
                ));
            }
        }

        return parts;
    }
}