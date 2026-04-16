package com.cloudkitchen.app;

import com.cloudkitchen.model.DishAvailability;
import com.cloudkitchen.model.InventorySummary;
import com.cloudkitchen.model.RecipeItem;
import com.cloudkitchen.model.User;
import com.cloudkitchen.service.AppFacade;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SmokeTest {
    public static void main(String[] args) throws Exception {
        Path databasePath = Path.of("/tmp", "cloud_kitchen_smoke.db");
        Files.deleteIfExists(databasePath);

        AppFacade facade = new ApplicationBootstrap().bootstrap(databasePath);

        facade.registerCustomer("Test Customer", "testguest", "guest123");
        facade.login("aarav", "cust123", User.Role.CUSTOMER);
        InventorySummary before = facade.getSummary();
        List<DishAvailability> dishes = facade.getDishAvailability();
        if (dishes.isEmpty()) {
            throw new IllegalStateException("Expected seeded dishes to be available.");
        }

        DishAvailability firstDish = dishes.get(0);
        facade.placeOrder(firstDish.getDish().getId(), 1);

        InventorySummary after = facade.getSummary();
        if (after.getTotalOrders() != before.getTotalOrders() + 1) {
            throw new IllegalStateException("Order count did not increase after placing an order.");
        }
        if (after.getTotalRevenue() <= before.getTotalRevenue()) {
            throw new IllegalStateException("Revenue did not increase after placing an order.");
        }
        if (facade.getCurrentCustomerOrders(10).isEmpty()) {
            throw new IllegalStateException("Customer order history should contain the placed order.");
        }

        facade.logout();
        facade.login("admin", "admin123", User.Role.ADMIN);
        if (facade.getRecentOrders(10).isEmpty()) {
            throw new IllegalStateException("Admin should be able to see all orders.");
        }

        facade.addIngredient("Saffron Milk", "ml", 200, 50);
        if (facade.getIngredients().stream().noneMatch(ingredient -> ingredient.getName().equals("Saffron Milk"))) {
            throw new IllegalStateException("Admin ingredient creation failed.");
        }

        long saffronId = facade.getIngredients().stream()
                .filter(ingredient -> ingredient.getName().equals("Saffron Milk"))
                .findFirst()
                .orElseThrow()
                .getId();

        facade.updateIngredient(saffronId, "Saffron Essence", "ml", 240, 60);
        if (facade.getIngredients().stream().noneMatch(ingredient -> ingredient.getName().equals("Saffron Essence"))) {
            throw new IllegalStateException("Admin ingredient update failed.");
        }

        long updatedIngredientId = facade.getIngredients().stream()
                .filter(ingredient -> ingredient.getName().equals("Saffron Essence"))
                .findFirst()
                .orElseThrow()
                .getId();

        facade.addDish(
                "Royal Saffron Rice",
                "Chef Specials",
                399,
                List.of(new RecipeItem(updatedIngredientId, "Saffron Essence", "ml", 250)));
        DishAvailability createdDish = facade.getDishAvailability().stream()
                .filter(dish -> dish.getDish().getName().equals("Royal Saffron Rice"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Admin dish creation failed."));
        if (createdDish.isAvailable()) {
            throw new IllegalStateException("Dish should be out of stock when required ingredient quantity is insufficient.");
        }

        facade.updateDish(
                createdDish.getDish().getId(),
                "Royal Saffron Rice Deluxe",
                "Chef Signature",
                429,
                List.of(new RecipeItem(updatedIngredientId, "Saffron Essence", "ml", 120)));
        DishAvailability updatedDish = facade.getDishAvailability().stream()
                .filter(dish -> dish.getDish().getName().equals("Royal Saffron Rice Deluxe"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Admin dish update failed."));
        if (!updatedDish.isAvailable()) {
            throw new IllegalStateException("Updated dish should be available after recipe quantity reduction.");
        }

        facade.removeDish(updatedDish.getDish().getId());
        if (facade.getDishAvailability().stream().anyMatch(dish -> dish.getDish().getName().contains("Royal Saffron Rice"))) {
            throw new IllegalStateException("Dish removal failed.");
        }

        facade.removeIngredient(updatedIngredientId);
        if (facade.getIngredients().stream().anyMatch(ingredient -> ingredient.getName().equals("Saffron Essence"))) {
            throw new IllegalStateException("Ingredient removal failed.");
        }

        System.out.println("Smoke test passed.");
        System.out.println("Seeded ingredients: " + after.getTotalIngredients());
        System.out.println("Seeded dishes: " + after.getTotalDishes());
        System.out.println("Orders after test: " + after.getTotalOrders());
        System.out.println("Revenue after test: " + after.getTotalRevenue());
    }
}
