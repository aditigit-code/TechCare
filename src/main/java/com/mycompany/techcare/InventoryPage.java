package com.mycompany.techcare;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class InventoryPage extends JPanel {

    private final JTextField skuField = new JTextField(18);
    private final JTextField nameField = new JTextField(18);
    private final JTextField categoryField = new JTextField(18);
    private final JTextField quantityField = new JTextField("0", 18);
    private final JTextField reorderField = new JTextField("0", 18);
    private final JTextField unitCostField = new JTextField("0.00", 18);
    private final JTextField sellingPriceField = new JTextField("0.00", 18);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                "Part ID", "SKU", "Part Name", "Category", "In Stock",
                "Reorder Level", "Unit Cost", "Selling Price", "Active"
            }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable partTable = new JTable(tableModel);
    private final SparePartDAO partDAO = new SparePartDAO();

    public InventoryPage() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createFormPanel(), BorderLayout.NORTH);
        add(new JScrollPane(partTable), BorderLayout.CENTER);

        partTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        partTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                loadSelectedPartIntoForm();
            }
        });

        loadParts();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Spare Parts"));

        JPanel fields = new JPanel(new GridLayout(0, 2, 8, 8));
        fields.add(new JLabel("SKU:"));
        fields.add(skuField);

        fields.add(new JLabel("Part name:"));
        fields.add(nameField);

        fields.add(new JLabel("Category (optional):"));
        fields.add(categoryField);

        fields.add(new JLabel("Quantity in stock:"));
        fields.add(quantityField);

        fields.add(new JLabel("Reorder level:"));
        fields.add(reorderField);

        fields.add(new JLabel("Unit cost:"));
        fields.add(unitCostField);

        fields.add(new JLabel("Selling price:"));
        fields.add(sellingPriceField);

        JButton addButton = new JButton("Add New Part");
        addButton.addActionListener(event -> addPart());

        JButton updateButton = new JButton("Update Selected");
        updateButton.addActionListener(event -> updateSelectedPart());

        JButton activeButton = new JButton("Activate / Deactivate");
        activeButton.addActionListener(event -> toggleSelectedPart());

        JButton clearButton = new JButton("Clear Form");
        clearButton.addActionListener(event -> clearForm());

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> loadParts());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refreshButton);
        buttons.add(clearButton);
        buttons.add(activeButton);
        buttons.add(updateButton);
        buttons.add(addButton);

        panel.add(fields, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private void addPart() {
        try {
            PartInput input = readForm();
            partDAO.addPart(
                    input.sku,
                    input.name,
                    input.category,
                    input.quantity,
                    input.reorderLevel,
                    input.unitCost,
                    input.sellingPrice
            );

            JOptionPane.showMessageDialog(this, "Part added successfully.");
            clearForm();
            loadParts();

        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            showDatabaseError("Could not add the part.", exception);
        }
    }

    private void updateSelectedPart() {
        int partId = getSelectedPartId();
        if (partId < 0) {
            return;
        }

        try {
            PartInput input = readForm();
            partDAO.updatePart(
                    partId,
                    input.sku,
                    input.name,
                    input.category,
                    input.quantity,
                    input.reorderLevel,
                    input.unitCost,
                    input.sellingPrice
            );

            JOptionPane.showMessageDialog(this, "Part updated successfully.");
            clearForm();
            loadParts();

        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            showDatabaseError("Could not update the part.", exception);
        }
    }

    private void toggleSelectedPart() {
        int selectedRow = partTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Select a part from the table first.");
            return;
        }

        int modelRow = partTable.convertRowIndexToModel(selectedRow);
        int partId = (Integer) tableModel.getValueAt(modelRow, 0);
        boolean currentlyActive =
                "Yes".equals(tableModel.getValueAt(modelRow, 8));

        try {
            partDAO.setActive(partId, !currentlyActive);
            clearForm();
            loadParts();
        } catch (SQLException exception) {
            showDatabaseError("Could not change the part's active status.",
                    exception);
        }
    }

    private PartInput readForm() {
        String sku = skuField.getText().trim();
        String name = nameField.getText().trim();

        if (sku.isEmpty()) {
            throw new IllegalArgumentException("Enter a SKU.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter a part name.");
        }

        int quantity = parseNonNegativeInteger(
                quantityField.getText(), "Quantity");
        int reorderLevel = parseNonNegativeInteger(
                reorderField.getText(), "Reorder level");
        BigDecimal unitCost = parseNonNegativeMoney(
                unitCostField.getText(), "Unit cost");
        BigDecimal sellingPrice = parseNonNegativeMoney(
                sellingPriceField.getText(), "Selling price");

        return new PartInput(
                sku,
                name,
                categoryField.getText().trim(),
                quantity,
                reorderLevel,
                unitCost,
                sellingPrice
        );
    }

    private int parseNonNegativeInteger(String value, String fieldName) {
        try {
            int number = Integer.parseInt(value.trim());
            if (number < 0) {
                throw new IllegalArgumentException(
                        fieldName + " cannot be negative.");
            }
            return number;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must be a whole number.");
        }
    }

    private BigDecimal parseNonNegativeMoney(String value, String fieldName) {
        try {
            BigDecimal amount = new BigDecimal(value.trim());
            if (amount.signum() < 0) {
                throw new IllegalArgumentException(
                        fieldName + " cannot be negative.");
            }
            return amount;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    fieldName + " must be a valid amount, such as 250.00.");
        }
    }

    private void loadParts() {
        tableModel.setRowCount(0);

        try {
            for (SparePartDAO.SparePartRow part : partDAO.findAll()) {
                tableModel.addRow(new Object[]{
                    part.partId(),
                    part.sku(),
                    part.partName(),
                    part.category(),
                    part.quantityInStock(),
                    part.reorderLevel(),
                    part.unitCost(),
                    part.sellingPrice(),
                    part.active() ? "Yes" : "No"
                });
            }
        } catch (SQLException exception) {
            showDatabaseError("Could not load inventory.", exception);
        }
    }

    private void loadSelectedPartIntoForm() {
        int selectedRow = partTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        int row = partTable.convertRowIndexToModel(selectedRow);
        skuField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 2)));

        Object category = tableModel.getValueAt(row, 3);
        categoryField.setText(category == null ? "" : category.toString());

        quantityField.setText(
                String.valueOf(tableModel.getValueAt(row, 4)));
        reorderField.setText(
                String.valueOf(tableModel.getValueAt(row, 5)));
        unitCostField.setText(
                String.valueOf(tableModel.getValueAt(row, 6)));
        sellingPriceField.setText(
                String.valueOf(tableModel.getValueAt(row, 7)));
    }

    private int getSelectedPartId() {
        int selectedRow = partTable.getSelectedRow();
        if (selectedRow < 0) {
            showMessage("Select a part from the table first.");
            return -1;
        }

        int row = partTable.convertRowIndexToModel(selectedRow);
        return (Integer) tableModel.getValueAt(row, 0);
    }

    private void clearForm() {
        partTable.clearSelection();
        skuField.setText("");
        nameField.setText("");
        categoryField.setText("");
        quantityField.setText("0");
        reorderField.setText("0");
        unitCostField.setText("0.00");
        sellingPriceField.setText("0.00");
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

    private static class PartInput {
        private final String sku;
        private final String name;
        private final String category;
        private final int quantity;
        private final int reorderLevel;
        private final BigDecimal unitCost;
        private final BigDecimal sellingPrice;

        private PartInput(
                String sku,
                String name,
                String category,
                int quantity,
                int reorderLevel,
                BigDecimal unitCost,
                BigDecimal sellingPrice) {
            this.sku = sku;
            this.name = name;
            this.category = category;
            this.quantity = quantity;
            this.reorderLevel = reorderLevel;
            this.unitCost = unitCost;
            this.sellingPrice = sellingPrice;
        }
    }
}