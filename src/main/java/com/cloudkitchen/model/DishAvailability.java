package com.cloudkitchen.model;

import java.util.List;

public class DishAvailability {
    private final Dish dish;
    private final boolean available;
    private final int maxServingsPossible;
    private final List<IngredientRequirementStatus> ingredientStatuses;

    public DishAvailability(
            Dish dish,
            boolean available,
            int maxServingsPossible,
            List<IngredientRequirementStatus> ingredientStatuses) {
        this.dish = dish;
        this.available = available;
        this.maxServingsPossible = maxServingsPossible;
        this.ingredientStatuses = List.copyOf(ingredientStatuses);
    }

    public Dish getDish() {
        return dish;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getMaxServingsPossible() {
        return maxServingsPossible;
    }

    public List<IngredientRequirementStatus> getIngredientStatuses() {
        return ingredientStatuses;
    }
}
