package com.cloudkitchen.repository;

import com.cloudkitchen.model.Ingredient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteIngredientRepository {
    public List<Ingredient> findAll(Connection connection) throws SQLException {
        String sql = """
                SELECT id, name, unit, quantity, threshold_quantity
                FROM ingredients
                WHERE active = 1
                ORDER BY name
                """;
        List<Ingredient> ingredients = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ingredients.add(mapRow(resultSet));
            }
        }
        return ingredients;
    }

    public Optional<Ingredient> findById(Connection connection, long id) throws SQLException {
        String sql = """
                SELECT id, name, unit, quantity, threshold_quantity
                FROM ingredients
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRow(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    public boolean existsByName(Connection connection, String name, Long excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) AS total FROM ingredients WHERE LOWER(name) = LOWER(?)"
                : "SELECT COUNT(*) AS total FROM ingredients WHERE LOWER(name) = LOWER(?) AND id <> ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            if (excludeId != null) {
                statement.setLong(2, excludeId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("total") > 0;
            }
        }
    }

    public void updateStock(Connection connection, long ingredientId, double quantity) throws SQLException {
        String sql = "UPDATE ingredients SET quantity = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, quantity);
            statement.setLong(2, ingredientId);
            statement.executeUpdate();
        }
    }

    public long insert(Connection connection, String name, String unit, double quantity, double thresholdQuantity)
            throws SQLException {
        String sql = """
                INSERT INTO ingredients (name, unit, quantity, threshold_quantity, active)
                VALUES (?, ?, ?, ?, 1)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, unit);
            statement.setDouble(3, quantity);
            statement.setDouble(4, thresholdQuantity);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Ingredient ID was not generated.");
            }
        }
    }

    public void deactivate(Connection connection, long ingredientId) throws SQLException {
        String sql = "UPDATE ingredients SET active = 0 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ingredientId);
            statement.executeUpdate();
        }
    }

    public void updateDetails(
            Connection connection,
            long ingredientId,
            String name,
            String unit,
            double quantity,
            double thresholdQuantity) throws SQLException {
        String sql = """
                UPDATE ingredients
                SET name = ?, unit = ?, quantity = ?, threshold_quantity = ?
                WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, unit);
            statement.setDouble(3, quantity);
            statement.setDouble(4, thresholdQuantity);
            statement.setLong(5, ingredientId);
            statement.executeUpdate();
        }
    }

    private Ingredient mapRow(ResultSet resultSet) throws SQLException {
        return new Ingredient(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("unit"),
                resultSet.getDouble("quantity"),
                resultSet.getDouble("threshold_quantity"));
    }
}
