package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DevicePage extends JPanel {

    private final DeviceDAO deviceDAO = new DeviceDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{
                "ID", "Customer", "Type", "Brand", "Model",
                "Serial Number", "Operating System", "Notes"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable deviceTable = new JTable(tableModel);
    private final JTextField searchField = new JTextField(18);
    private final JLabel statusLabel = new JLabel("Click Devices to load records.");
    private List<Device> currentDevices = new ArrayList<>();

    private final String[] typeLabels = {
        "Laptop", "Desktop", "All-in-one PC", "Other"
    };

    private final String[] typeValues = {
        "LAPTOP", "DESKTOP", "ALL_IN_ONE", "OTHER"
    };

    public DevicePage() {
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
        searchButton.addActionListener(event -> loadDevices());
        searchPanel.add(searchButton);
        searchField.addActionListener(event -> loadDevices());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton addButton = new JButton("Add Device");
        addButton.addActionListener(event -> addDevice());

        JButton editButton = new JButton("Edit Device");
        editButton.addActionListener(event -> editDevice());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> {
            searchField.setText("");
            loadDevices();
        });

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(refreshButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        deviceTable.setRowHeight(26);
        deviceTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION
        );

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(deviceTable), BorderLayout.CENTER);

        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        add(statusLabel, BorderLayout.SOUTH);
    }

    public void loadDevices() {
        tableModel.setRowCount(0);

        try {
            currentDevices = deviceDAO.getDevices(searchField.getText());

            for (Device device : currentDevices) {
                tableModel.addRow(new Object[]{
                    device.getDeviceId(),
                    device.getCustomerName(),
                    friendlyType(device.getDeviceType()),
                    device.getBrand(),
                    device.getModel(),
                    device.getSerialNumber(),
                    device.getOperatingSystem(),
                    device.getNotes()
                });
            }

            statusLabel.setText("Devices found: " + currentDevices.size());

        } catch (SQLException exception) {
            statusLabel.setText("Could not load devices.");
            showError(exception);
        }
    }

    private void addDevice() {
        Device device = showDeviceForm(null);

        if (device == null) {
            return;
        }

        try {
            deviceDAO.addDevice(device);
            loadDevices();
            JOptionPane.showMessageDialog(this, "Device added successfully.");
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private void editDevice() {
        Device selectedDevice = getSelectedDevice();

        if (selectedDevice == null) {
            JOptionPane.showMessageDialog(this, "Select a device to edit.");
            return;
        }

        Device updatedDevice = showDeviceForm(selectedDevice);

        if (updatedDevice == null) {
            return;
        }

        try {
            if (deviceDAO.updateDevice(updatedDevice)) {
                loadDevices();
                JOptionPane.showMessageDialog(this, "Device updated successfully.");
            }
        } catch (SQLException exception) {
            showError(exception);
        }
    }

    private Device getSelectedDevice() {
        int selectedRow = deviceTable.getSelectedRow();

        if (selectedRow < 0) {
            return null;
        }

        int modelRow = deviceTable.convertRowIndexToModel(selectedRow);

        if (modelRow >= currentDevices.size()) {
            return null;
        }

        return currentDevices.get(modelRow);
    }

    private Device showDeviceForm(Device existingDevice) {
        JComboBox<Customer> customerCombo = new JComboBox<>();

        try {
            for (Customer customer : customerDAO.getActiveCustomers("")) {
                customerCombo.addItem(customer);
            }
        } catch (SQLException exception) {
            showError(exception);
            return null;
        }

        if (customerCombo.getItemCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Add an active customer before adding a device."
            );
            return null;
        }

        JComboBox<String> typeCombo = new JComboBox<>(typeLabels);
        JTextField brandField = new JTextField(20);
        JTextField modelField = new JTextField(20);
        JTextField serialField = new JTextField(20);
        JTextField operatingSystemField = new JTextField(20);
        JTextField notesField = new JTextField(20);

        if (existingDevice != null) {
            brandField.setText(existingDevice.getBrand());
            modelField.setText(existingDevice.getModel());
            serialField.setText(existingDevice.getSerialNumber());
            operatingSystemField.setText(existingDevice.getOperatingSystem());
            notesField.setText(existingDevice.getNotes());

            typeCombo.setSelectedIndex(
                    typeIndex(existingDevice.getDeviceType())
            );

            boolean customerFound = false;

            for (int i = 0; i < customerCombo.getItemCount(); i++) {
                Customer customer = customerCombo.getItemAt(i);

                if (customer.getCustomerId() == existingDevice.getCustomerId()) {
                    customerCombo.setSelectedIndex(i);
                    customerFound = true;
                    break;
                }
            }

            if (!customerFound) {
                JOptionPane.showMessageDialog(
                        this,
                        "This device belongs to an inactive customer. "
                        + "Reactivate that customer before editing this device."
                );
                return null;
            }
        }

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Customer:"));
        form.add(customerCombo);
        form.add(new JLabel("Device type:"));
        form.add(typeCombo);
        form.add(new JLabel("Brand:"));
        form.add(brandField);
        form.add(new JLabel("Model:"));
        form.add(modelField);
        form.add(new JLabel("Serial number (optional):"));
        form.add(serialField);
        form.add(new JLabel("Operating system (optional):"));
        form.add(operatingSystemField);
        form.add(new JLabel("Notes (optional):"));
        form.add(notesField);

        String title = existingDevice == null ? "Add Device" : "Edit Device";

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

        String brand = brandField.getText().trim();
        String model = modelField.getText().trim();

        if (brand.isEmpty() || model.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Brand and model are required."
            );
            return null;
        }

        Customer selectedCustomer =
                (Customer) customerCombo.getSelectedItem();

        if (selectedCustomer == null) {
            JOptionPane.showMessageDialog(this, "Select a customer.");
            return null;
        }

        int deviceId = existingDevice == null
                ? 0
                : existingDevice.getDeviceId();

        String deviceType = typeValues[typeCombo.getSelectedIndex()];

        return new Device(
                deviceId,
                selectedCustomer.getCustomerId(),
                selectedCustomer.getFullName(),
                deviceType,
                brand,
                model,
                serialField.getText().trim(),
                operatingSystemField.getText().trim(),
                notesField.getText().trim()
        );
    }

    private int typeIndex(String deviceType) {
        for (int i = 0; i < typeValues.length; i++) {
            if (typeValues[i].equals(deviceType)) {
                return i;
            }
        }
        return 0;
    }

    private String friendlyType(String deviceType) {
        for (int i = 0; i < typeValues.length; i++) {
            if (typeValues[i].equals(deviceType)) {
                return typeLabels[i];
            }
        }
        return deviceType;
    }

    private void showError(SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                "Database error: " + exception.getMessage(),
                "Device Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}