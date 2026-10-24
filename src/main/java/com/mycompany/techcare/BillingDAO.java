package com.mycompany.techcare;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {

    public record TicketOption(int ticketId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record InvoiceChoice(int invoiceId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public record InvoiceRow(
            int invoiceId,
            int ticketId,
            String customer,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal balance,
            String issuedAt) {
    }

    public List<TicketOption> findUnbilledTickets() throws SQLException {
        String sql =
                "SELECT st.ticket_id, c.full_name, d.brand, d.model "
                + "FROM service_ticket st "
                + "JOIN device d ON d.device_id = st.device_id "
                + "JOIN customer c ON c.customer_id = d.customer_id "
                + "LEFT JOIN invoice i ON i.ticket_id = st.ticket_id "
                + "WHERE i.invoice_id IS NULL "
                + "ORDER BY st.ticket_id DESC";

        List<TicketOption> tickets = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                String label = "Ticket #" + results.getInt("ticket_id")
                        + " — " + results.getString("full_name")
                        + " — " + results.getString("brand")
                        + " " + results.getString("model");

                tickets.add(new TicketOption(
                        results.getInt("ticket_id"), label));
            }
        }

        return tickets;
    }

    public int createInvoice(
            int ticketId,
            BigDecimal labor,
            BigDecimal parts,
            BigDecimal discount,
            BigDecimal tax) throws SQLException {

        BigDecimal total = labor.add(parts).subtract(discount).add(tax)
                .setScale(2, RoundingMode.HALF_UP);

        if (total.signum() < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot make the invoice total negative.");
        }

        String sql =
                "INSERT INTO invoice "
                + "(ticket_id, labor_amount, parts_amount, discount_amount, "
                + "tax_amount, total_amount) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, ticketId);
            statement.setBigDecimal(2, labor);
            statement.setBigDecimal(3, parts);
            statement.setBigDecimal(4, discount);
            statement.setBigDecimal(5, tax);
            statement.setBigDecimal(6, total);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException(
                            "Creating the invoice did not return an ID.");
                }
                return keys.getInt(1);
            }
        }
    }

    public List<InvoiceRow> findAllInvoices() throws SQLException {
        String sql =
                "SELECT i.invoice_id, i.ticket_id, c.full_name, "
                + "i.total_amount, COALESCE(SUM(p.amount), 0) AS paid_amount, "
                + "i.total_amount - COALESCE(SUM(p.amount), 0) AS balance, "
                + "i.issued_at "
                + "FROM invoice i "
                + "JOIN service_ticket st ON st.ticket_id = i.ticket_id "
                + "JOIN device d ON d.device_id = st.device_id "
                + "JOIN customer c ON c.customer_id = d.customer_id "
                + "LEFT JOIN payment p ON p.invoice_id = i.invoice_id "
                + "GROUP BY i.invoice_id, i.ticket_id, c.full_name, "
                + "i.total_amount, i.issued_at "
                + "ORDER BY i.invoice_id DESC";

        List<InvoiceRow> invoices = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                invoices.add(new InvoiceRow(
                        results.getInt("invoice_id"),
                        results.getInt("ticket_id"),
                        results.getString("full_name"),
                        results.getBigDecimal("total_amount"),
                        results.getBigDecimal("paid_amount"),
                        results.getBigDecimal("balance"),
                        results.getString("issued_at")
                ));
            }
        }

        return invoices;
    }

    public List<InvoiceChoice> findInvoicesWithBalance()
            throws SQLException {

        List<InvoiceChoice> choices = new ArrayList<>();

        for (InvoiceRow invoice : findAllInvoices()) {
            if (invoice.balance().signum() > 0) {
                String label = "Invoice #" + invoice.invoiceId()
                        + " — Ticket #" + invoice.ticketId()
                        + " — Balance: " + invoice.balance();
                choices.add(new InvoiceChoice(invoice.invoiceId(), label));
            }
        }

        return choices;
    }

    public void recordPayment(
            int invoiceId,
            BigDecimal amount,
            String method,
            String reference,
            int receivedBy,
            String notes) throws SQLException {

        if (amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero.");
        }

        String invoiceSql =
                "SELECT total_amount FROM invoice "
                + "WHERE invoice_id = ? FOR UPDATE";

        String paidSql =
                "SELECT COALESCE(SUM(amount), 0) "
                + "FROM payment WHERE invoice_id = ?";

        String paymentSql =
                "INSERT INTO payment "
                + "(invoice_id, amount, payment_method, "
                + "transaction_reference, received_by, notes) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                BigDecimal total;

                try (PreparedStatement statement =
                             connection.prepareStatement(invoiceSql)) {
                    statement.setInt(1, invoiceId);
                    try (ResultSet results = statement.executeQuery()) {
                        if (!results.next()) {
                            throw new SQLException("Invoice not found.");
                        }
                        total = results.getBigDecimal("total_amount");
                    }
                }

                BigDecimal alreadyPaid;

                try (PreparedStatement statement =
                             connection.prepareStatement(paidSql)) {
                    statement.setInt(1, invoiceId);
                    try (ResultSet results = statement.executeQuery()) {
                        results.next();
                        alreadyPaid = results.getBigDecimal(1);
                    }
                }

                BigDecimal balance = total.subtract(alreadyPaid);
                if (amount.compareTo(balance) > 0) {
                    throw new SQLException(
                            "Payment is greater than the remaining balance of "
                            + balance + ".");
                }

                try (PreparedStatement statement =
                             connection.prepareStatement(paymentSql)) {
                    statement.setInt(1, invoiceId);
                    statement.setBigDecimal(2, amount);
                    statement.setString(3, method);
                    setOptionalText(statement, 4, reference);
                    statement.setInt(5, receivedBy);
                    setOptionalText(statement, 6, notes);
                    statement.executeUpdate();
                }

                connection.commit();

            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void setOptionalText(
            PreparedStatement statement,
            int index,
            String value) throws SQLException {

        if (value == null || value.isBlank()) {
            statement.setNull(index, java.sql.Types.VARCHAR);
        } else {
            statement.setString(index, value.trim());
        }
    }
}