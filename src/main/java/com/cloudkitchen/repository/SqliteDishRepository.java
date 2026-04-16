package com.cloudkitchen.repository;

import com.cloudkitchen.model.Dish;
import com.cloudkitchen.model.RecipeItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SqliteDishRepository {
    public List<Dish> findAll(Connection connection) throws SQLException {
        String sql = """
                SELECT d.id AS dish_id,
                       d.name AS dish_name,
                       d.category,
                       d.price,
                       d.active,
                       i.id AS ingredient_id,
                       i.name AS ingredient_name,
                       i.unit,
                       ri.required_quantity
                FROM dishes d
                JOIN recipe_items ri ON ri.dish_id = d.id
                JOIN ingredients i ON i.id = ri.ingredient_id
                WHERE d.active = 1
                ORDER BY d.name, i.name
                """;
        Map<Long, DishBuilder> dishes = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long dishId = resultSet.getLong("dish_id");
                String dishName = resultSet.getString("dish_name");
                String category = resultSet.getString("category");
                double price = resultSet.getDouble("price");
                boolean active = resultSet.getInt("active") == 1;
                DishBuilder builder = dishes.computeIfAbsent(dishId, unused ->
                        new DishBuilder(dishId, dishName, category, price, active));
                builder.recipeItems.add(new RecipeItem(
                        resultSet.getLong("ingredient_id"),
                        resultSet.getString("ingredient_name"),
                        resultSet.getString("unit"),
                        resultSet.getDouble("required_quantity")));
            }
        }
        return dishes.values().stream().map(DishBuilder::build).toList();
    }

    public Optional<Dish> findById(Connection connection, long id) throws SQLException {
        return findAll(connection).stream()
                .filter(dish -> dish.getId() == id)
                .findFirst();
    }

    public boolean existsByName(Connection connection, String name, Long excludeId) throws SQLException {
        String sql = excludeId == null
                ? "SELECT COUNT(*) AS total FROM dishes WHERE LOWER(name) = LOWER(?)"
                : "SELECT COUNT(*) AS total FROM dishes WHERE LOWER(name) = LOWER(?) AND id <> ?";
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

    public long insert(Connection connection, String name, String category, double price) throws SQLException {
        String sql = "INSERT INTO dishes (name, category, price, active) VALUES (?, ?, ?, 1)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setDouble(3, price);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
                throw new SQLException("Dish ID was not generated.");
            }
        }
    }

    public void insertRecipeItem(Connection connection, long dishId, long ingredientId, double requiredQuantity)
            throws SQLException {
        String sql = "INSERT INTO recipe_items (dish_id, ingredient_id, required_quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishId);
            statement.setLong(2, ingredientId);
            statement.setDouble(3, requiredQuantity);
            statement.executeUpdate();
        }
    }

    public void deactivate(Connection connection, long dishId) throws SQLException {
        String sql = "UPDATE dishes SET active = 0 WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishId);
            statement.executeUpdate();
        }
    }

    public void updateDetails(Connection connection, long dishId, String name, String category, double price)
            throws SQLException {
        String sql = "UPDATE dishes SET name = ?, category = ?, price = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setDouble(3, price);
            statement.setLong(4, dishId);
            statement.executeUpdate();
        }
    }

    public void deleteRecipeItems(Connection connection, long dishId) throws SQLException {
        String sql = "DELETE FROM recipe_items WHERE dish_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishId);
            statement.executeUpdate();
        }
    }

    private static class DishBuilder {
        private final long id;
        private final String name;
        private final String category;
        private final double price;
        private final boolean active;
        private final List<RecipeItem> recipeItems = new ArrayList<>();

        private DishBuilder(long id, String name, String category, double price, boolean active) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.active = active;
        }

        private Dish build() {
            return new Dish(id, name, category, price, active, recipeItems);
        }
    }
}
