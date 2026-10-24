package com.mycompany.techcare;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getActiveCustomers(String searchText)
            throws SQLException {

        List<Customer> customers = new ArrayList<>();
        String search = searchText == null ? "" : searchText.trim();
        String pattern = "%" + search + "%";

        String sql = """
                SELECT customer_id, full_name, phone, email, address, is_active
                FROM customer
                WHERE is_active = 1
                  AND (full_name LIKE ?
                       OR phone LIKE ?
                       OR COALESCE(email, '') LIKE ?)
                ORDER BY customer_id DESC
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(new Customer(
                            resultSet.getInt("customer_id"),
                            resultSet.getString("full_name"),
                            resultSet.getString("phone"),
                            resultSet.getString("email"),
                            resultSet.getString("address"),
                            resultSet.getBoolean("is_active")
                    ));
                }
            }
        }

        return customers;
    }

    public void addCustomer(Customer customer) throws SQLException {
        String sql = """
                INSERT INTO customer (full_name, phone, email, address)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getFullName().trim());
            statement.setString(2, customer.getPhone().trim());
            statement.setString(3, optionalText(customer.getEmail()));
            statement.setString(4, optionalText(customer.getAddress()));
            statement.executeUpdate();
        }
    }

    public boolean updateCustomer(Customer customer) throws SQLException {
        String sql = """
                UPDATE customer
                SET full_name = ?, phone = ?, email = ?, address = ?
                WHERE customer_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getFullName().trim());
            statement.setString(2, customer.getPhone().trim());
            statement.setString(3, optionalText(customer.getEmail()));
            statement.setString(4, optionalText(customer.getAddress()));
            statement.setInt(5, customer.getCustomerId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean deactivateCustomer(int customerId) throws SQLException {
        String sql = "UPDATE customer SET is_active = 0 WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);
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