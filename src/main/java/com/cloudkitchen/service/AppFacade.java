package com.cloudkitchen.service;

import com.cloudkitchen.model.DishAvailability;
import com.cloudkitchen.model.Ingredient;
import com.cloudkitchen.model.InventorySummary;
import com.cloudkitchen.model.OrderRecord;
import com.cloudkitchen.model.RecipeItem;
import com.cloudkitchen.model.User;
import java.util.List;

public class AppFacade {
    private final AuthService authService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    private User.Session currentSession;

    public AppFacade(AuthService authService, InventoryService inventoryService, OrderService orderService) {
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    public User.Session login(String username, String password, User.Role selectedPortal) {
        currentSession = authService.login(username, password, selectedPortal);
        return currentSession;
    }

    public void logout() {
        currentSession = null;
    }

    public void registerCustomer(String fullName, String username, String password) {
        authService.registerCustomer(fullName, username, password);
    }

    public User.Session getCurrentSession() {
        return currentSession;
    }

    public InventorySummary getSummary() {
        return inventoryService.getSummary();
    }

    public List<Ingredient> getIngredients() {
        return inventoryService.getAllIngredients();
    }

    public List<Ingredient> getLowStockIngredients() {
        return inventoryService.getLowStockIngredients();
    }

    public List<DishAvailability> getDishAvailability() {
        return inventoryService.getDishAvailability();
    }

    public void restockIngredient(long ingredientId, double amount) {
        inventoryService.restockIngredient(ingredientId, amount);
    }

    public void addIngredient(String name, String unit, double quantity, double thresholdQuantity) {
        inventoryService.addIngredient(name, unit, quantity, thresholdQuantity);
    }

    public void updateIngredient(long ingredientId, String name, String unit, double quantity, double thresholdQuantity) {
        inventoryService.updateIngredient(ingredientId, name, unit, quantity, thresholdQuantity);
    }

    public void removeIngredient(long ingredientId) {
        inventoryService.removeIngredient(ingredientId);
    }

    public void addDish(String name, String category, double price, List<RecipeItem> recipeItems) {
        inventoryService.addDish(name, category, price, recipeItems);
    }

    public void updateDish(long dishId, String name, String category, double price, List<RecipeItem> recipeItems) {
        inventoryService.updateDish(dishId, name, category, price, recipeItems);
    }

    public void removeDish(long dishId) {
        inventoryService.removeDish(dishId);
    }

    public void placeOrder(long dishId, int quantity) {
        orderService.placeOrder(currentSession, dishId, quantity);
    }

    public List<OrderRecord> getRecentOrders(int limit) {
        return orderService.getRecentOrders(limit);
    }

    public List<OrderRecord> getCurrentCustomerOrders(int limit) {
        if (currentSession == null) {
            throw new IllegalStateException("Please login first.");
        }
        return orderService.getRecentOrdersForCustomer(currentSession.getUser().getId(), limit);
    }
}
