package com.mycompany.techcare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDAO {

    public List<Device> getDevices(String searchText) throws SQLException {
        List<Device> devices = new ArrayList<>();
        String search = searchText == null ? "" : searchText.trim();
        String pattern = "%" + search + "%";

        String sql = """
                SELECT d.device_id, d.customer_id, c.full_name AS customer_name,
                       d.device_type, d.brand, d.model, d.serial_number,
                       d.operating_system, d.notes
                FROM device d
                JOIN customer c ON c.customer_id = d.customer_id
                WHERE c.full_name LIKE ?
                   OR d.device_type LIKE ?
                   OR d.brand LIKE ?
                   OR d.model LIKE ?
                   OR COALESCE(d.serial_number, '') LIKE ?
                ORDER BY d.device_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 1; i <= 5; i++) {
                statement.setString(i, pattern);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    devices.add(new Device(
                            resultSet.getInt("device_id"),
                            resultSet.getInt("customer_id"),
                            resultSet.getString("customer_name"),
                            resultSet.getString("device_type"),
                            resultSet.getString("brand"),
                            resultSet.getString("model"),
                            resultSet.getString("serial_number"),
                            resultSet.getString("operating_system"),
                            resultSet.getString("notes")
                    ));
                }
            }
        }

        return devices;
    }

    public void addDevice(Device device) throws SQLException {
        String sql = """
                INSERT INTO device (
                    customer_id, device_type, brand, model,
                    serial_number, operating_system, notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, device.getCustomerId());
            statement.setString(2, device.getDeviceType());
            statement.setString(3, device.getBrand().trim());
            statement.setString(4, device.getModel().trim());
            statement.setString(5, optionalText(device.getSerialNumber()));
            statement.setString(6, optionalText(device.getOperatingSystem()));
            statement.setString(7, optionalText(device.getNotes()));

            statement.executeUpdate();
        }
    }

    public boolean updateDevice(Device device) throws SQLException {
        String sql = """
                UPDATE device
                SET customer_id = ?, device_type = ?, brand = ?, model = ?,
                    serial_number = ?, operating_system = ?, notes = ?
                WHERE device_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, device.getCustomerId());
            statement.setString(2, device.getDeviceType());
            statement.setString(3, device.getBrand().trim());
            statement.setString(4, device.getModel().trim());
            statement.setString(5, optionalText(device.getSerialNumber()));
            statement.setString(6, optionalText(device.getOperatingSystem()));
            statement.setString(7, optionalText(device.getNotes()));
            statement.setInt(8, device.getDeviceId());

            return statement.executeUpdate() > 0;
        }
    }

    private String optionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}