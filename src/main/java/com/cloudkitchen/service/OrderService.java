package com.cloudkitchen.service;

import com.cloudkitchen.db.DatabaseManager;
import com.cloudkitchen.model.Dish;
import com.cloudkitchen.model.OrderRecord;
import com.cloudkitchen.model.RecipeItem;
import com.cloudkitchen.model.User;
import com.cloudkitchen.repository.SqliteDishRepository;
import com.cloudkitchen.repository.SqliteIngredientRepository;
import com.cloudkitchen.repository.SqliteOrderRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    private final DatabaseManager databaseManager;
    private final SqliteDishRepository dishRepository;
    private final SqliteIngredientRepository ingredientRepository;
    private final SqliteOrderRepository orderRepository;

    public OrderService(
            DatabaseManager databaseManager,
            SqliteDishRepository dishRepository,
            SqliteIngredientRepository ingredientRepository,
            SqliteOrderRepository orderRepository) {
        this.databaseManager = databaseManager;
        this.dishRepository = dishRepository;
        this.ingredientRepository = ingredientRepository;
        this.orderRepository = orderRepository;
    }

    public void placeOrder(User.Session session, long dishId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero.");
        }
        if (session == null) {
            throw new IllegalArgumentException("Login session is required.");
        }

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Dish dish = dishRepository.findById(connection, dishId)
                        .orElseThrow(() -> new IllegalArgumentException("Dish not found."));

                // Validate every ingredient first so we either place the full order or reject it cleanly.
                for (RecipeItem item : dish.getRecipeItems()) {
                    double required = item.getRequiredQuantity() * quantity;
                    double available = ingredientRepository.findById(connection, item.getIngredientId())
                            .orElseThrow(() -> new IllegalStateException("Missing ingredient mapping."))
                            .getQuantity();
                    if (available < required) {
                        throw new IllegalStateException(
                                "Insufficient stock for " + item.getIngredientName() + ". Needed: " + required);
                    }
                }

                // Deduct stock only after validation succeeds for the complete recipe.
                for (RecipeItem item : dish.getRecipeItems()) {
                    double required = item.getRequiredQuantity() * quantity;
                    double available = ingredientRepository.findById(connection, item.getIngredientId())
                            .orElseThrow(() -> new IllegalStateException("Missing ingredient mapping."))
                            .getQuantity();
                    ingredientRepository.updateStock(connection, item.getIngredientId(), available - required);
                }

                orderRepository.insert(
                        connection,
                        dish.getId(),
                        session.getUser().getId(),
                        quantity,
                        dish.getPrice() * quantity,
                        LocalDateTime.now().withNano(0).toString());
                // Commit the stock update and order insert together as one transaction.
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to process order", exception);
        }
    }

    public List<OrderRecord> getRecentOrders(int limit) {
        try (Connection connection = databaseManager.getConnection()) {
            return orderRepository.findRecent(connection, limit);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load order history", exception);
        }
    }

    public List<OrderRecord> getRecentOrdersForCustomer(long customerId, int limit) {
        try (Connection connection = databaseManager.getConnection()) {
            return orderRepository.findRecentByCustomer(connection, customerId, limit);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load customer order history", exception);
        }
    }
}
