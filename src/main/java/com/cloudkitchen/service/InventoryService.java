package com.cloudkitchen.service;

import com.cloudkitchen.db.DatabaseManager;
import com.cloudkitchen.model.Dish;
import com.cloudkitchen.model.DishAvailability;
import com.cloudkitchen.model.Ingredient;
import com.cloudkitchen.model.IngredientRequirementStatus;
import com.cloudkitchen.model.InventorySummary;
import com.cloudkitchen.model.RecipeItem;
import com.cloudkitchen.repository.SqliteDishRepository;
import com.cloudkitchen.repository.SqliteIngredientRepository;
import com.cloudkitchen.repository.SqliteOrderRepository;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryService {
    private final DatabaseManager databaseManager;
    private final SqliteIngredientRepository ingredientRepository;
    private final SqliteDishRepository dishRepository;
    private final SqliteOrderRepository orderRepository;

    public InventoryService(
            DatabaseManager databaseManager,
            SqliteIngredientRepository ingredientRepository,
            SqliteDishRepository dishRepository,
            SqliteOrderRepository orderRepository) {
        this.databaseManager = databaseManager;
        this.ingredientRepository = ingredientRepository;
        this.dishRepository = dishRepository;
        this.orderRepository = orderRepository;
    }

    public List<Ingredient> getAllIngredients() {
        try (Connection connection = databaseManager.getConnection()) {
            return ingredientRepository.findAll(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load ingredients", exception);
        }
    }

    public List<Ingredient> getLowStockIngredients() {
        return getAllIngredients().stream()
                .filter(Ingredient::isLowStock)
                .sorted(Comparator.comparing(Ingredient::getQuantity))
                .toList();
    }

    public List<Dish> getAllDishes() {
        try (Connection connection = databaseManager.getConnection()) {
            return dishRepository.findAll(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load dishes", exception);
        }
    }

    public List<DishAvailability> getDishAvailability() {
        List<Ingredient> ingredients = getAllIngredients();
        Map<Long, Ingredient> ingredientsById = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            ingredientsById.put(ingredient.getId(), ingredient);
        }
        return getAllDishes().stream()
                .map(dish -> calculateAvailability(dish, ingredientsById))
                .toList();
    }

    public void restockIngredient(long ingredientId, double amountToAdd) {
        if (amountToAdd <= 0) {
            throw new IllegalArgumentException("Restock quantity must be greater than zero.");
        }
        try (Connection connection = databaseManager.getConnection()) {
            Ingredient ingredient = ingredientRepository.findById(connection, ingredientId)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found."));
            ingredientRepository.updateStock(connection, ingredientId, ingredient.getQuantity() + amountToAdd);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update stock", exception);
        }
    }

    public InventorySummary getSummary() {
        List<Ingredient> ingredients = getAllIngredients();
        List<DishAvailability> dishes = getDishAvailability();
        try (Connection connection = databaseManager.getConnection()) {
            return new InventorySummary(
                    ingredients.size(),
                    (int) ingredients.stream().filter(Ingredient::isLowStock).count(),
                    (int) dishes.stream().filter(DishAvailability::isAvailable).count(),
                    dishes.size(),
                    orderRepository.countAll(connection),
                    orderRepository.totalRevenue(connection));
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to build dashboard summary", exception);
        }
    }

    public void addIngredient(String name, String unit, double quantity, double thresholdQuantity) {
        if (name == null || name.isBlank() || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Ingredient name and unit are required.");
        }
        if (quantity < 0 || thresholdQuantity < 0) {
            throw new IllegalArgumentException("Ingredient quantities cannot be negative.");
        }
        try (Connection connection = databaseManager.getConnection()) {
            if (ingredientRepository.existsByName(connection, name.trim(), null)) {
                throw new IllegalArgumentException("An ingredient with that name already exists.");
            }
            ingredientRepository.insert(connection, name.trim(), unit.trim(), quantity, thresholdQuantity);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to add ingredient", exception);
        }
    }

    public void updateIngredient(long ingredientId, String name, String unit, double quantity, double thresholdQuantity) {
        if (name == null || name.isBlank() || unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Ingredient name and unit are required.");
        }
        if (quantity < 0 || thresholdQuantity < 0) {
            throw new IllegalArgumentException("Ingredient quantities cannot be negative.");
        }
        try (Connection connection = databaseManager.getConnection()) {
            ingredientRepository.findById(connection, ingredientId)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient not found."));
            if (ingredientRepository.existsByName(connection, name.trim(), ingredientId)) {
                throw new IllegalArgumentException("Another ingredient with that name already exists.");
            }
            ingredientRepository.updateDetails(connection, ingredientId, name.trim(), unit.trim(), quantity, thresholdQuantity);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update ingredient", exception);
        }
    }

    public void removeIngredient(long ingredientId) {
        try (Connection connection = databaseManager.getConnection()) {
            ingredientRepository.deactivate(connection, ingredientId);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to remove ingredient", exception);
        }
    }

    public void addDish(String name, String category, double price, List<RecipeItem> recipeItems) {
        if (name == null || name.isBlank() || category == null || category.isBlank()) {
            throw new IllegalArgumentException("Dish name and category are required.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Dish price cannot be negative.");
        }
        if (recipeItems == null || recipeItems.isEmpty()) {
            throw new IllegalArgumentException("A dish recipe must include at least one ingredient.");
        }
        try (Connection connection = databaseManager.getConnection()) {
            if (dishRepository.existsByName(connection, name.trim(), null)) {
                throw new IllegalArgumentException("A dish with that name already exists.");
            }
            connection.setAutoCommit(false);
            try {
                long dishId = dishRepository.insert(connection, name.trim(), category.trim(), price);
                for (RecipeItem recipeItem : recipeItems) {
                    dishRepository.insertRecipeItem(
                            connection,
                            dishId,
                            recipeItem.getIngredientId(),
                            recipeItem.getRequiredQuantity());
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to add dish", exception);
        }
    }

    public void updateDish(long dishId, String name, String category, double price, List<RecipeItem> recipeItems) {
        if (name == null || name.isBlank() || category == null || category.isBlank()) {
            throw new IllegalArgumentException("Dish name and category are required.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Dish price cannot be negative.");
        }
        if (recipeItems == null || recipeItems.isEmpty()) {
            throw new IllegalArgumentException("A dish recipe must include at least one ingredient.");
        }
        try (Connection connection = databaseManager.getConnection()) {
            if (dishRepository.existsByName(connection, name.trim(), dishId)) {
                throw new IllegalArgumentException("Another dish with that name already exists.");
            }
            connection.setAutoCommit(false);
            try {
                dishRepository.updateDetails(connection, dishId, name.trim(), category.trim(), price);
                dishRepository.deleteRecipeItems(connection, dishId);
                for (RecipeItem recipeItem : recipeItems) {
                    dishRepository.insertRecipeItem(
                            connection,
                            dishId,
                            recipeItem.getIngredientId(),
                            recipeItem.getRequiredQuantity());
                }
                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update dish", exception);
        }
    }

    public void removeDish(long dishId) {
        try (Connection connection = databaseManager.getConnection()) {
            dishRepository.deactivate(connection, dishId);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to remove dish", exception);
        }
    }

    private DishAvailability calculateAvailability(Dish dish, Map<Long, Ingredient> ingredientsById) {
        int maxServings = Integer.MAX_VALUE;
        // A dish is available only when every recipe ingredient can satisfy its required quantity.
        List<IngredientRequirementStatus> requirementStatuses = dish.getRecipeItems().stream()
                .map(item -> {
                    Ingredient ingredient = ingredientsById.get(item.getIngredientId());
                    double available = ingredient == null ? 0 : ingredient.getQuantity();
                    return new IngredientRequirementStatus(
                            item.getIngredientName(),
                            item.getUnit(),
                            available,
                            item.getRequiredQuantity(),
                            available >= item.getRequiredQuantity());
                })
                .toList();
        // The smallest ingredient ratio determines how many servings can be prepared right now.
        for (IngredientRequirementStatus status : requirementStatuses) {
            if (status.getRequiredQuantity() > 0) {
                maxServings = Math.min(
                        maxServings,
                        (int) Math.floor(status.getAvailableQuantity() / status.getRequiredQuantity()));
            }
        }
        boolean available = requirementStatuses.stream().allMatch(IngredientRequirementStatus::isSufficient);
        return new DishAvailability(
                dish,
                available,
                maxServings == Integer.MAX_VALUE ? 0 : Math.max(maxServings, 0),
                requirementStatuses);
    }
}
