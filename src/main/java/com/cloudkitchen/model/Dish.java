package com.cloudkitchen.model;

import java.util.List;

public class Dish {
    private final long id;
    private final String name;
    private final String category;
    private final double price;
    private final boolean active;
    private final List<RecipeItem> recipeItems;

    public Dish(long id, String name, String category, double price, boolean active, List<RecipeItem> recipeItems) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.active = active;
        this.recipeItems = List.copyOf(recipeItems);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public List<RecipeItem> getRecipeItems() {
        return recipeItems;
    }
}
