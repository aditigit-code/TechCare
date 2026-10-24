package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class TechnicianPage extends JPanel {

    private final JTextField usernameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JTextField specializationField = new JTextField(20);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                "Technician ID", "Staff ID", "Username", "Email",
                "Specialization", "Employment", "Login Active"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable technicianTable = new JTable(tableModel);
    private final TechnicianDAO technicianDAO = new TechnicianDAO();

    public TechnicianPage() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createFormPanel(), BorderLayout.NORTH);
        add(new JScrollPane(technicianTable), BorderLayout.CENTER);

        technicianTable.setSelectionMode(
                javax.swing.ListSelectionModel.SINGLE_SELECTION);

        loadTechnicians();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(
                "Add Technician Account"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("Username:"));
        fields.add(usernameField);

        fields.add(new JLabel("Email (optional):"));
        fields.add(emailField);

        fields.add(new JLabel("Temporary password:"));
        fields.add(passwordField);

        fields.add(new JLabel("Specialization (optional):"));
        fields.add(specializationField);

        JButton addButton = new JButton("Add Technician");
        addButton.addActionListener(event -> addTechnician());

        JButton toggleButton = new JButton("Activate / Deactivate Selected");
        toggleButton.addActionListener(event -> toggleSelectedTechnician());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> loadTechnicians());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(toggleButton);
        buttons.add(addButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addTechnician() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String specialization = specializationField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter a username.");
            usernameField.requestFocusInWindow();
            Arrays.fill(password, '\0');
            return;
        }

        if (password.length < 8) {
            JOptionPane.showMessageDialog(
                    this, "The password must be at least 8 characters.");
            passwordField.requestFocusInWindow();
            Arrays.fill(password, '\0');
            return;
        }

        try {
            technicianDAO.createTechnician(
                    username, email, password, specialization);

            JOptionPane.showMessageDialog(
                    this, "Technician account created successfully.");

            usernameField.setText("");
            emailField.setText("");
            passwordField.setText("");
            specializationField.setText("");

            loadTechnicians();

        } catch (SQLException | GeneralSecurityException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not create the technician account.\n\n"
                    + exception.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    private void toggleSelectedTechnician() {
        int selectedRow = technicianTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this, "Select a technician from the table first.");
            return;
        }

        int modelRow = technicianTable.convertRowIndexToModel(selectedRow);
        int technicianId =
                (Integer) tableModel.getValueAt(modelRow, 0);
        String status =
                (String) tableModel.getValueAt(modelRow, 5);

        boolean activate = !"ACTIVE".equalsIgnoreCase(status);
        String action = activate ? "activate" : "deactivate";

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to " + action + " this technician?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            technicianDAO.setActive(technicianId, activate);
            loadTechnicians();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not update the technician.\n\n"
                    + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void loadTechnicians() {
        tableModel.setRowCount(0);

        try {
            for (TechnicianDAO.TechnicianRow technician
                    : technicianDAO.findAll()) {
                tableModel.addRow(new Object[]{
                    technician.technicianId(),
                    technician.staffUserId(),
                    technician.username(),
                    technician.email(),
                    technician.specialization(),
                    technician.employmentStatus(),
                    technician.loginActive() ? "Yes" : "No"
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load technicians.\n\n"
                    + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}