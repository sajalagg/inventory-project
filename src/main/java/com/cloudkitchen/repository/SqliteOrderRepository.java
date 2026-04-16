package com.cloudkitchen.repository;

import com.cloudkitchen.model.OrderRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqliteOrderRepository {
    public void insert(Connection connection, long dishId, long customerId, int quantity, double totalPrice, String orderedAt)
            throws SQLException {
        String sql = """
                INSERT INTO orders (dish_id, customer_id, quantity, total_price, ordered_at)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishId);
            statement.setLong(2, customerId);
            statement.setInt(3, quantity);
            statement.setDouble(4, totalPrice);
            statement.setString(5, orderedAt);
            statement.executeUpdate();
        }
    }

    public List<OrderRecord> findRecent(Connection connection, int limit) throws SQLException {
        String sql = """
                SELECT o.id, d.name AS dish_name, u.full_name AS customer_name, o.quantity, o.total_price, o.ordered_at
                FROM orders o
                JOIN dishes d ON d.id = o.dish_id
                JOIN users u ON u.id = o.customer_id
                ORDER BY o.ordered_at DESC
                LIMIT ?
                """;
        return findOrders(connection, sql, limit, null);
    }

    public List<OrderRecord> findRecentByCustomer(Connection connection, long customerId, int limit) throws SQLException {
        String sql = """
                SELECT o.id, d.name AS dish_name, u.full_name AS customer_name, o.quantity, o.total_price, o.ordered_at
                FROM orders o
                JOIN dishes d ON d.id = o.dish_id
                JOIN users u ON u.id = o.customer_id
                WHERE o.customer_id = ?
                ORDER BY o.ordered_at DESC
                LIMIT ?
                """;
        return findOrders(connection, sql, limit, customerId);
    }

    private List<OrderRecord> findOrders(Connection connection, String sql, int limit, Long customerId) throws SQLException {
        List<OrderRecord> records = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (customerId == null) {
                statement.setInt(1, limit);
            } else {
                statement.setLong(1, customerId);
                statement.setInt(2, limit);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(new OrderRecord(
                            resultSet.getLong("id"),
                            resultSet.getString("dish_name"),
                            resultSet.getString("customer_name"),
                            resultSet.getInt("quantity"),
                            resultSet.getDouble("total_price"),
                            LocalDateTime.parse(resultSet.getString("ordered_at"))));
                }
            }
        }
        return records;
    }

    public int countAll(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_orders FROM orders";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt("total_orders") : 0;
        }
    }

    public double totalRevenue(Connection connection) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_price), 0) AS total_revenue FROM orders";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getDouble("total_revenue") : 0.0;
        }
    }
}
