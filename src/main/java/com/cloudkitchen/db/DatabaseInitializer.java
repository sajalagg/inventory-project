package com.cloudkitchen.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DatabaseInitializer {
    private static final List<String> SCHEMA_STATEMENTS = List.of(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK (role IN ('ADMIN', 'CUSTOMER'))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS ingredients (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                unit TEXT NOT NULL,
                quantity REAL NOT NULL CHECK (quantity >= 0),
                threshold_quantity REAL NOT NULL CHECK (threshold_quantity >= 0),
                active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS dishes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                category TEXT NOT NULL,
                price REAL NOT NULL CHECK (price >= 0),
                active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS recipe_items (
                dish_id INTEGER NOT NULL,
                ingredient_id INTEGER NOT NULL,
                required_quantity REAL NOT NULL CHECK (required_quantity > 0),
                PRIMARY KEY (dish_id, ingredient_id),
                FOREIGN KEY (dish_id) REFERENCES dishes(id) ON DELETE CASCADE,
                FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                dish_id INTEGER NOT NULL,
                customer_id INTEGER NOT NULL DEFAULT 2,
                quantity INTEGER NOT NULL CHECK (quantity > 0),
                total_price REAL NOT NULL CHECK (total_price >= 0),
                ordered_at TEXT NOT NULL,
                FOREIGN KEY (dish_id) REFERENCES dishes(id),
                FOREIGN KEY (customer_id) REFERENCES users(id)
            )
            """);

    public void initialize(Connection connection) throws SQLException {
        // Create missing tables first, then apply lightweight migrations for older databases.
        for (String statement : SCHEMA_STATEMENTS) {
            connection.createStatement().execute(statement);
        }
        migrateOrdersTable(connection);
        migrateIngredientsTable(connection);
        // Seed demo data only when the database is empty so repeat runs keep existing records.
        if (isEmpty(connection, "users")) {
            seedUsers(connection);
        }
        if (isEmpty(connection, "ingredients")) {
            seedIngredients(connection);
        }
        if (isEmpty(connection, "dishes")) {
            seedDishes(connection);
        }
        if (isEmpty(connection, "recipe_items")) {
            seedRecipeItems(connection);
        }
    }

    private void migrateOrdersTable(Connection connection) throws SQLException {
        if (!columnExists(connection, "orders", "customer_id")) {
            connection.createStatement().execute("ALTER TABLE orders ADD COLUMN customer_id INTEGER NOT NULL DEFAULT 2");
        }
    }

    private void migrateIngredientsTable(Connection connection) throws SQLException {
        if (!columnExists(connection, "ingredients", "active")) {
            connection.createStatement().execute("ALTER TABLE ingredients ADD COLUMN active INTEGER NOT NULL DEFAULT 1");
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmpty(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT COUNT(*) AS row_count FROM " + tableName;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt("row_count") == 0;
        }
    }

    private void seedIngredients(Connection connection) throws SQLException {
        insertIngredient(connection, "Basmati Rice", "g", 6000, 1500);
        insertIngredient(connection, "Cooking Oil", "ml", 3500, 700);
        insertIngredient(connection, "Chicken", "g", 5000, 1200);
        insertIngredient(connection, "Mixed Vegetables", "g", 3200, 800);
        insertIngredient(connection, "Paneer", "g", 2500, 600);
        insertIngredient(connection, "Soy Sauce", "ml", 1200, 250);
        insertIngredient(connection, "Spices", "g", 1800, 400);
        insertIngredient(connection, "Onion", "g", 2600, 500);
    }

    private void seedDishes(Connection connection) throws SQLException {
        insertDish(connection, "Classic Chicken Biryani", "Rice Bowls", 249);
        insertDish(connection, "Veg Fried Rice", "Rice Bowls", 179);
        insertDish(connection, "Paneer Tikka Bowl", "Signature Bowls", 229);
        insertDish(connection, "Spicy Chicken Rice Box", "Meal Boxes", 269);
    }

    private void seedRecipeItems(Connection connection) throws SQLException {
        insertRecipeItem(connection, 1, 1, 220);
        insertRecipeItem(connection, 1, 2, 20);
        insertRecipeItem(connection, 1, 3, 180);
        insertRecipeItem(connection, 1, 7, 12);
        insertRecipeItem(connection, 1, 8, 40);

        insertRecipeItem(connection, 2, 1, 180);
        insertRecipeItem(connection, 2, 2, 15);
        insertRecipeItem(connection, 2, 4, 110);
        insertRecipeItem(connection, 2, 6, 18);
        insertRecipeItem(connection, 2, 8, 25);

        insertRecipeItem(connection, 3, 5, 180);
        insertRecipeItem(connection, 3, 2, 18);
        insertRecipeItem(connection, 3, 7, 15);
        insertRecipeItem(connection, 3, 8, 45);

        insertRecipeItem(connection, 4, 1, 200);
        insertRecipeItem(connection, 4, 3, 160);
        insertRecipeItem(connection, 4, 2, 16);
        insertRecipeItem(connection, 4, 7, 10);
        insertRecipeItem(connection, 4, 6, 15);
    }

    private void seedUsers(Connection connection) throws SQLException {
        insertUser(connection, "admin", "admin123", "Kitchen Administrator", "ADMIN");
        insertUser(connection, "aarav", "cust123", "Aarav Sharma", "CUSTOMER");
        insertUser(connection, "diya", "cust123", "Diya Mehta", "CUSTOMER");
    }

    private void insertIngredient(Connection connection, String name, String unit, double quantity, double threshold)
            throws SQLException {
        String sql = """
                INSERT INTO ingredients (name, unit, quantity, threshold_quantity)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, unit);
            statement.setDouble(3, quantity);
            statement.setDouble(4, threshold);
            statement.executeUpdate();
        }
    }

    private void insertDish(Connection connection, String name, String category, double price) throws SQLException {
        String sql = "INSERT INTO dishes (name, category, price, active) VALUES (?, ?, ?, 1)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, category);
            statement.setDouble(3, price);
            statement.executeUpdate();
        }
    }

    private void insertUser(Connection connection, String username, String password, String fullName, String role)
            throws SQLException {
        String sql = """
                INSERT INTO users (username, password, full_name, role)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, fullName);
            statement.setString(4, role);
            statement.executeUpdate();
        }
    }

    private void insertRecipeItem(Connection connection, long dishId, long ingredientId, double quantity)
            throws SQLException {
        String sql = """
                INSERT INTO recipe_items (dish_id, ingredient_id, required_quantity)
                VALUES (?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishId);
            statement.setLong(2, ingredientId);
            statement.setDouble(3, quantity);
            statement.executeUpdate();
        }
    }
}
