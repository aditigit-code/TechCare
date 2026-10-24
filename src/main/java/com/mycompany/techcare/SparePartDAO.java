package com.mycompany.techcare;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SparePartDAO {

    public record SparePartRow(
            int partId,
            String sku,
            String partName,
            String category,
            int quantityInStock,
            int reorderLevel,
            BigDecimal unitCost,
            BigDecimal sellingPrice,
            boolean active) {
    }

    public List<SparePartRow> findAll() throws SQLException {
        String sql =
                "SELECT part_id, sku, part_name, category, "
                + "quantity_in_stock, reorder_level, unit_cost, "
                + "selling_price, is_active "
                + "FROM spare_part "
                + "ORDER BY is_active DESC, part_name";

        List<SparePartRow> parts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {
                parts.add(new SparePartRow(
                        results.getInt("part_id"),
                        results.getString("sku"),
                        results.getString("part_name"),
                        results.getString("category"),
                        results.getInt("quantity_in_stock"),
                        results.getInt("reorder_level"),
                        results.getBigDecimal("unit_cost"),
                        results.getBigDecimal("selling_price"),
                        results.getBoolean("is_active")
                ));
            }
        }

        return parts;
    }

    public void addPart(
            String sku,
            String partName,
            String category,
            int quantity,
            int reorderLevel,
            BigDecimal unitCost,
            BigDecimal sellingPrice) throws SQLException {

        String sql =
                "INSERT INTO spare_part "
                + "(sku, part_name, category, quantity_in_stock, "
                + "reorder_level, unit_cost, selling_price) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, sku.trim());
            statement.setString(2, partName.trim());
            setOptionalText(statement, 3, category);
            statement.setInt(4, quantity);
            statement.setInt(5, reorderLevel);
            statement.setBigDecimal(6, unitCost);
            statement.setBigDecimal(7, sellingPrice);
            statement.executeUpdate();
        }
    }

    public void updatePart(
            int partId,
            String sku,
            String partName,
            String category,
            int quantity,
            int reorderLevel,
            BigDecimal unitCost,
            BigDecimal sellingPrice) throws SQLException {

        String sql =
                "UPDATE spare_part "
                + "SET sku = ?, part_name = ?, category = ?, "
                + "quantity_in_stock = ?, reorder_level = ?, "
                + "unit_cost = ?, selling_price = ? "
                + "WHERE part_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, sku.trim());
            statement.setString(2, partName.trim());
            setOptionalText(statement, 3, category);
            statement.setInt(4, quantity);
            statement.setInt(5, reorderLevel);
            statement.setBigDecimal(6, unitCost);
            statement.setBigDecimal(7, sellingPrice);
            statement.setInt(8, partId);
            statement.executeUpdate();
        }
    }

    public void setActive(int partId, boolean active) throws SQLException {
        String sql =
                "UPDATE spare_part SET is_active = ? WHERE part_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBoolean(1, active);
            statement.setInt(2, partId);
            statement.executeUpdate();
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