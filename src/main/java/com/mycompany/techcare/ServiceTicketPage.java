package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class ServiceTicketPage extends JPanel {

    private final JComboBox<DeviceOption> deviceComboBox = new JComboBox<>();
    private final JComboBox<CategoryOption> categoryComboBox = new JComboBox<>();
    private final JComboBox<String> priorityComboBox =
            new JComboBox<>(new String[]{"LOW", "NORMAL", "HIGH", "URGENT"});
    private final JTextArea issueTextArea = new JTextArea(3, 30);

    private final JComboBox<ServiceTicketDAO.TechnicianChoice>
            technicianComboBox = new JComboBox<>();

    private final JComboBox<String> statusComboBox = new JComboBox<>(
            new String[]{
                "RECEIVED",
                "DIAGNOSING",
                "AWAITING_APPROVAL",
                "IN_PROGRESS",
                "QUALITY_CHECK",
                "READY_FOR_PICKUP",
                "DELIVERED",
                "ON_HOLD",
                "CANCELLED"
            });

    private final JTextField reasonField = new JTextField(24);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                "Ticket ID", "Customer", "Device", "Category", "Priority",
                "Status", "Technician", "Technician ID", "Received"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable ticketTable = new JTable(tableModel);
    private final ServiceTicketDAO ticketDAO = new ServiceTicketDAO();

    public ServiceTicketPage() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createTicketForm(), BorderLayout.NORTH);
        add(new JScrollPane(ticketTable), BorderLayout.CENTER);
        add(createUpdatePanel(), BorderLayout.SOUTH);

        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.getColumnModel().getColumn(7).setMinWidth(0);
        ticketTable.getColumnModel().getColumn(7).setMaxWidth(0);
        ticketTable.getColumnModel().getColumn(7).setPreferredWidth(0);
        ticketTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedTicketIntoControls();
            }
        });

        loadOptions();
        loadTickets();
    }

    private JPanel createTicketForm() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Create Service Ticket"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Device:"));
        fields.add(deviceComboBox);

        fields.add(new JLabel("Service category:"));
        fields.add(categoryComboBox);

        fields.add(new JLabel("Priority:"));
        fields.add(priorityComboBox);

        fields.add(new JLabel("Reported issue:"));
        fields.add(new JScrollPane(issueTextArea));

        JButton createButton = new JButton("Create Ticket");
        createButton.addActionListener(event -> createTicket());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> {
            loadOptions();
            loadTickets();
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(createButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createUpdatePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
                "Assign Technician or Update Status"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Technician:"));
        fields.add(technicianComboBox);

        fields.add(new JLabel("New status:"));
        fields.add(statusComboBox);

        fields.add(new JLabel("Reason (optional):"));
        fields.add(reasonField);

        JButton assignButton = new JButton("Save Assignment");
        assignButton.addActionListener(event -> assignTechnician());

        JButton statusButton = new JButton("Update Status");
        statusButton.addActionListener(event -> updateStatus());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(assignButton);
        buttons.add(statusButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void loadOptions() {
        deviceComboBox.removeAllItems();
        categoryComboBox.removeAllItems();
        technicianComboBox.removeAllItems();

        technicianComboBox.addItem(
                new ServiceTicketDAO.TechnicianChoice(null, "Unassigned"));

        String deviceSql =
                "SELECT d.device_id, c.full_name, d.brand, d.model "
                + "FROM device d "
                + "JOIN customer c ON c.customer_id = d.customer_id "
                + "ORDER BY c.full_name, d.brand, d.model";

        String categorySql =
                "SELECT category_id, category_name "
                + "FROM service_category WHERE is_active = 1 "
                + "ORDER BY category_name";

        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement statement =
                         connection.prepareStatement(deviceSql);
                 ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    String label = results.getString("full_name")
                            + " — " + results.getString("brand")
                            + " " + results.getString("model");

                    deviceComboBox.addItem(new DeviceOption(
                            results.getInt("device_id"), label));
                }
            }

            try (PreparedStatement statement =
                         connection.prepareStatement(categorySql);
                 ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    categoryComboBox.addItem(new CategoryOption(
                            results.getInt("category_id"),
                            results.getString("category_name")));
                }
            }

            for (ServiceTicketDAO.TechnicianChoice technician
                    : ticketDAO.findActiveTechnicians()) {
                technicianComboBox.addItem(technician);
            }

        } catch (SQLException exception) {
            showError("Could not load ticket options.", exception);
        }
    }

    private void loadTickets() {
        tableModel.setRowCount(0);

        String sql =
                "SELECT st.ticket_id, c.full_name, d.brand, d.model, "
                + "sc.category_name, st.priority, st.status, "
                + "COALESCE(su.username, 'Unassigned') AS technician_name, "
                + "st.assigned_technician_id, st.received_at "
                + "FROM service_ticket st "
                + "JOIN device d ON d.device_id = st.device_id "
                + "JOIN customer c ON c.customer_id = d.customer_id "
                + "JOIN service_category sc ON sc.category_id = st.category_id "
                + "LEFT JOIN technician_profile tp "
                + "ON tp.technician_id = st.assigned_technician_id "
                + "LEFT JOIN staff_user su ON su.staff_user_id = tp.staff_user_id "
                + "ORDER BY st.ticket_id DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                String device = results.getString("brand")
                        + " " + results.getString("model");

                Object technicianId = results.getObject(
                        "assigned_technician_id");

                tableModel.addRow(new Object[]{
                    results.getInt("ticket_id"),
                    results.getString("full_name"),
                    device,
                    results.getString("category_name"),
                    results.getString("priority"),
                    results.getString("status"),
                    results.getString("technician_name"),
                    technicianId == null ? null : ((Number) technicianId).intValue(),
                    results.getTimestamp("received_at")
                });
            }
        } catch (SQLException exception) {
            showError("Could not load service tickets.", exception);
        }
    }

    private void createTicket() {
        DeviceOption device = (DeviceOption) deviceComboBox.getSelectedItem();
        CategoryOption category =
                (CategoryOption) categoryComboBox.getSelectedItem();
        String issue = issueTextArea.getText().trim();

        if (device == null) {
            JOptionPane.showMessageDialog(
                    this, "Add a device before creating a ticket.");
            return;
        }

        if (category == null) {
            JOptionPane.showMessageDialog(
                    this, "There are no active service categories.");
            return;
        }

        if (issue.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Enter a description of the reported issue.");
            return;
        }

        StaffUser currentUser = AppSession.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(
                    this, "Please log in again before creating a ticket.");
            return;
        }

        ServiceTicket ticket = new ServiceTicket(
                device.id,
                category.id,
                currentUser.getStaffUserId(),
                (String) priorityComboBox.getSelectedItem(),
                issue
        );

        try {
            int ticketId = ticketDAO.createTicket(ticket);
            JOptionPane.showMessageDialog(
                    this, "Ticket #" + ticketId + " was created.");
            issueTextArea.setText("");
            priorityComboBox.setSelectedItem("NORMAL");
            loadTickets();
        } catch (SQLException exception) {
            showError("Could not create the ticket.", exception);
        }
    }

    private void assignTechnician() {
        int ticketId = getSelectedTicketId();
        if (ticketId < 0) {
            return;
        }

        StaffUser currentUser = AppSession.getCurrentUser();
        ServiceTicketDAO.TechnicianChoice technician =
                (ServiceTicketDAO.TechnicianChoice)
                        technicianComboBox.getSelectedItem();

        if (currentUser == null || technician == null) {
            JOptionPane.showMessageDialog(
                    this, "Select a technician and make sure you are logged in.");
            return;
        }

        try {
            ticketDAO.assignTechnician(
                    ticketId,
                    technician.technicianId(),
                    currentUser.getStaffUserId(),
                    reasonField.getText()
            );

            reasonField.setText("");
            loadTickets();
            JOptionPane.showMessageDialog(
                    this, "Ticket assignment was updated.");
        } catch (SQLException exception) {
            showError("Could not update the ticket assignment.", exception);
        }
    }

    private void updateStatus() {
        int ticketId = getSelectedTicketId();
        if (ticketId < 0) {
            return;
        }

        StaffUser currentUser = AppSession.getCurrentUser();
        if (currentUser == null) {
            JOptionPane.showMessageDialog(
                    this, "Please log in again before updating a ticket.");
            return;
        }

        String newStatus = (String) statusComboBox.getSelectedItem();

        try {
            ticketDAO.updateStatus(
                    ticketId,
                    newStatus,
                    currentUser.getStaffUserId(),
                    reasonField.getText()
            );

            reasonField.setText("");
            loadTickets();
            JOptionPane.showMessageDialog(
                    this, "Ticket status was updated.");
        } catch (SQLException exception) {
            showError("Could not update the ticket status.", exception);
        }
    }

    private int getSelectedTicketId() {
        int selectedRow = ticketTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this, "Select a ticket from the table first.");
            return -1;
        }

        int modelRow = ticketTable.convertRowIndexToModel(selectedRow);
        return (Integer) tableModel.getValueAt(modelRow, 0);
    }

    private void loadSelectedTicketIntoControls() {
        int selectedRow = ticketTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int modelRow = ticketTable.convertRowIndexToModel(selectedRow);
        statusComboBox.setSelectedItem(tableModel.getValueAt(modelRow, 5));

        Object selectedTechnicianId =
                tableModel.getValueAt(modelRow, 7);
        Integer currentId = selectedTechnicianId == null
                ? null
                : (Integer) selectedTechnicianId;

        for (int i = 0; i < technicianComboBox.getItemCount(); i++) {
            ServiceTicketDAO.TechnicianChoice option =
                    technicianComboBox.getItemAt(i);

            if (Objects.equals(option.technicianId(), currentId)) {
                technicianComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void showError(String message, SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                message + "\n\n" + exception.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class DeviceOption {
        private final int id;
        private final String label;

        private DeviceOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class CategoryOption {
        private final int id;
        private final String name;

        private CategoryOption(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}