package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class CustomerPage extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"ID", "Full Name", "Phone", "Email", "Address"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable customerTable = new JTable(tableModel);
    private final JTextField searchField = new JTextField(18);
    private final JLabel statusLabel = new JLabel("Click Customers to load records.");

    public CustomerPage() {
        setLayout(new BorderLayout(10, 10));
        setBackground(java.awt.Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> loadCustomers());
        searchPanel.add(searchButton);

        searchField.addActionListener(event -> loadCustomers());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(event -> addCustomer());

        JButton editButton = new JButton("Edit");
        editButton.addActionListener(event -> editCustomer());

        JButton deactivateButton = new JButton("Deactivate");
        deactivateButton.addActionListener(event -> deactivateCustomer());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> {
            searchField.setText("");
            loadCustomers();
        });

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deactivateButton);
        actionPanel.add(refreshButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        customerTable.setRowHeight(26);
        customerTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(customerTable), BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void loadCustomers() {
        tableModel.setRowCount(0);

        try {
            List<Customer> customers =
                    customerDAO.getActiveCustomers(searchField.getText());

            for (Customer customer : customers) {
                tableModel.addRow(new Object[]{
                    customer.getCustomerId(),
                    customer.getFullName(),
                    customer.getPhone(),
                    customer.getEmail(),
                    customer.getAddress()
                });
            }

            statusLabel.setText("Active customers: " + customers.size());

        } catch (SQLException exception) {
            statusLabel.setText("Could not load customers.");
            showError(exception);
        }
    }

    private void addCustomer() {
        Customer customer = showCustomerForm(null);

        if (customer == null) {
            return;
        }

        try {
            customerDAO.addCustomer(customer);
            loadCustomers();
            JOptionPane.showMessageDialog(this, "Customer added successfully.");
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void editCustomer() {
        Customer selectedCustomer = getSelectedCustomer();

        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Select a customer to edit.");
            return;
        }

        Customer updatedCustomer = showCustomerForm(selectedCustomer);

        if (updatedCustomer == null) {
            return;
        }

        try {
            if (customerDAO.updateCustomer(updatedCustomer)) {
                loadCustomers();
                JOptionPane.showMessageDialog(this, "Customer updated successfully.");
            }
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void deactivateCustomer() {
        Customer selectedCustomer = getSelectedCustomer();

        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Select a customer first.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Deactivate " + selectedCustomer.getFullName() + "?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (customerDAO.deactivateCustomer(
                    selectedCustomer.getCustomerId())) {
                loadCustomers();
                JOptionPane.showMessageDialog(this, "Customer deactivated.");
            }
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private Customer getSelectedCustomer() {
        int selectedRow = customerTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int row = customerTable.convertRowIndexToModel(selectedRow);

        return new Customer(
                ((Number) tableModel.getValueAt(row, 0)).intValue(),
                (String) tableModel.getValueAt(row, 1),
                (String) tableModel.getValueAt(row, 2),
                (String) tableModel.getValueAt(row, 3),
                (String) tableModel.getValueAt(row, 4),
                true
        );
    }

    private Customer showCustomerForm(Customer existingCustomer) {
        JTextField nameField = new JTextField(22);
        JTextField phoneField = new JTextField(22);
        JTextField emailField = new JTextField(22);
        JTextField addressField = new JTextField(22);

        if (existingCustomer != null) {
            nameField.setText(existingCustomer.getFullName());
            phoneField.setText(existingCustomer.getPhone());
            emailField.setText(existingCustomer.getEmail());
            addressField.setText(existingCustomer.getAddress());
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Full name:"));
        form.add(nameField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Email (optional):"));
        form.add(emailField);
        form.add(new JLabel("Address (optional):"));
        form.add(addressField);

        String title = existingCustomer == null
                ? "Add Customer"
                : "Edit Customer";

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        if (nameField.getText().trim().isEmpty()
                || phoneField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Full name and phone are required."
            );
            return null;
        }

        int customerId = existingCustomer == null
                ? 0
                : existingCustomer.getCustomerId();

        return new Customer(
                customerId,
                nameField.getText().trim(),
                phoneField.getText().trim(),
                emailField.getText().trim(),
                addressField.getText().trim(),
                true
        );
    }

    private void showError(SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                "Database error: " + exception.getMessage(),
                "Customer Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}