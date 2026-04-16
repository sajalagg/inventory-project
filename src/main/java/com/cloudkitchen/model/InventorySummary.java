package com.cloudkitchen.model;

public class InventorySummary {
    private final int totalIngredients;
    private final int lowStockIngredients;
    private final int availableDishes;
    private final int totalDishes;
    private final int totalOrders;
    private final double totalRevenue;

    public InventorySummary(
            int totalIngredients,
            int lowStockIngredients,
            int availableDishes,
            int totalDishes,
            int totalOrders,
            double totalRevenue) {
        this.totalIngredients = totalIngredients;
        this.lowStockIngredients = lowStockIngredients;
        this.availableDishes = availableDishes;
        this.totalDishes = totalDishes;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalIngredients() {
        return totalIngredients;
    }

    public int getLowStockIngredients() {
        return lowStockIngredients;
    }

    public int getAvailableDishes() {
        return availableDishes;
    }

    public int getTotalDishes() {
        return totalDishes;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
