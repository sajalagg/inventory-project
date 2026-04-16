package com.cloudkitchen.model;

public class RecipeItem {
    private final long ingredientId;
    private final String ingredientName;
    private final String unit;
    private final double requiredQuantity;

    public RecipeItem(long ingredientId, String ingredientName, String unit, double requiredQuantity) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.requiredQuantity = requiredQuantity;
    }

    public long getIngredientId() {
        return ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }
}
