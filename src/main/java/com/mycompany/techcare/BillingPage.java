package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BillingPage extends JPanel {

    private final JComboBox<BillingDAO.TicketOption> ticketComboBox =
            new JComboBox<>();
    private final JTextField laborField = new JTextField("0.00", 10);
    private final JTextField partsField = new JTextField("0.00", 10);
    private final JTextField discountField = new JTextField("0.00", 10);
    private final JTextField taxField = new JTextField("0.00", 10);

    private final JComboBox<BillingDAO.InvoiceChoice> invoiceComboBox =
            new JComboBox<>();
    private final JTextField paymentAmountField = new JTextField(10);
    private final JComboBox<String> paymentMethodComboBox =
            new JComboBox<>(new String[]{
                "CASH", "CARD", "UPI", "BANK_TRANSFER", "OTHER"
            });
    private final JTextField referenceField = new JTextField(12);
    private final JTextField notesField = new JTextField(12);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                "Invoice ID", "Ticket ID", "Customer", "Total",
                "Paid", "Balance", "Issued At"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable invoiceTable = new JTable(tableModel);
    private final BillingDAO billingDAO = new BillingDAO();

    public BillingPage() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createInvoicePanel(), BorderLayout.NORTH);
        add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        add(createPaymentPanel(), BorderLayout.SOUTH);

        refreshData();
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Create Invoice"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Unbilled ticket:"));
        fields.add(ticketComboBox);

        fields.add(new JLabel("Labour amount:"));
        fields.add(laborField);

        fields.add(new JLabel("Parts amount:"));
        fields.add(partsField);

        fields.add(new JLabel("Discount amount:"));
        fields.add(discountField);

        fields.add(new JLabel("Tax amount:"));
        fields.add(taxField);

        JButton createButton = new JButton("Create Invoice");
        createButton.addActionListener(event -> createInvoice());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(createButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPaymentPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Record Payment"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Invoice with balance:"));
        fields.add(invoiceComboBox);

        fields.add(new JLabel("Payment amount:"));
        fields.add(paymentAmountField);

        fields.add(new JLabel("Payment method:"));
        fields.add(paymentMethodComboBox);

        fields.add(new JLabel("Transaction reference (optional):"));
        fields.add(referenceField);

        fields.add(new JLabel("Notes (optional):"));
        fields.add(notesField);

        JButton paymentButton = new JButton("Record Payment");
        paymentButton.addActionListener(event -> recordPayment());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refreshData());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(paymentButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void createInvoice() {
        BillingDAO.TicketOption ticket =
                (BillingDAO.TicketOption) ticketComboBox.getSelectedItem();

        if (ticket == null) {
            showMessage("There are no unbilled tickets to select.");
            return;
        }

        try {
            BigDecimal labor = readAmount(laborField, "Labour amount");
            BigDecimal parts = readAmount(partsField, "Parts amount");
            BigDecimal discount = readAmount(discountField, "Discount amount");
            BigDecimal tax = readAmount(taxField, "Tax amount");

            int invoiceId = billingDAO.createInvoice(
                    ticket.ticketId(), labor, parts, discount, tax);

            showMessage("Invoice #" + invoiceId + " created successfully.");
            laborField.setText("0.00");
            partsField.setText("0.00");
            discountField.setText("0.00");
            taxField.setText("0.00");
            refreshData();

        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            showDatabaseError("Could not create the invoice.", exception);
        }
    }

    private void recordPayment() {
        BillingDAO.InvoiceChoice invoice =
                (BillingDAO.InvoiceChoice) invoiceComboBox.getSelectedItem();

        if (invoice == null) {
            showMessage("There are no invoices with an outstanding balance.");
            return;
        }

        StaffUser currentUser = AppSession.getCurrentUser();
        if (currentUser == null) {
            showMessage("Please log in again before recording a payment.");
            return;
        }

        try {
            BigDecimal amount = readAmount(
                    paymentAmountField, "Payment amount");

            if (amount.signum() <= 0) {
                showMessage("Payment amount must be greater than zero.");
                return;
            }

            billingDAO.recordPayment(
                    invoice.invoiceId(),
                    amount,
                    (String) paymentMethodComboBox.getSelectedItem(),
                    referenceField.getText(),
                    currentUser.getStaffUserId(),
                    notesField.getText()
            );

            showMessage("Payment recorded successfully.");
            paymentAmountField.setText("");
            referenceField.setText("");
            notesField.setText("");
            refreshData();

        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            showDatabaseError("Could not record the payment.", exception);
        }
    }

    private BigDecimal readAmount(JTextField field, String label) {
        try {
            BigDecimal amount = new BigDecimal(field.getText().trim());
            if (amount.signum() < 0) {
                throw new IllegalArgumentException(
                        label + " cannot be negative.");
            }
            return amount.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    label + " must be a valid amount, such as 250.00.");
        }
    }

    private void refreshData() {
        loadTickets();
        loadInvoices();
    }

    private void loadTickets() {
        ticketComboBox.removeAllItems();

        try {
            for (BillingDAO.TicketOption ticket
                    : billingDAO.findUnbilledTickets()) {
                ticketComboBox.addItem(ticket);
            }
        } catch (SQLException exception) {
            showDatabaseError("Could not load unbilled tickets.", exception);
        }
    }

    private void loadInvoices() {
        tableModel.setRowCount(0);
        invoiceComboBox.removeAllItems();

        try {
            for (BillingDAO.InvoiceRow invoice
                    : billingDAO.findAllInvoices()) {
                tableModel.addRow(new Object[]{
                    invoice.invoiceId(),
                    invoice.ticketId(),
                    invoice.customer(),
                    invoice.total(),
                    invoice.paid(),
                    invoice.balance(),
                    invoice.issuedAt()
                });
            }

            for (BillingDAO.InvoiceChoice invoice
                    : billingDAO.findInvoicesWithBalance()) {
                invoiceComboBox.addItem(invoice);
            }
        } catch (SQLException exception) {
            showDatabaseError("Could not load invoices.", exception);
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showDatabaseError(String message, SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}