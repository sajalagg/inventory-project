package com.cloudkitchen.model;

public class IngredientRequirementStatus {
    private final String ingredientName;
    private final String unit;
    private final double availableQuantity;
    private final double requiredQuantity;
    private final boolean sufficient;

    public IngredientRequirementStatus(
            String ingredientName,
            String unit,
            double availableQuantity,
            double requiredQuantity,
            boolean sufficient) {
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.availableQuantity = availableQuantity;
        this.requiredQuantity = requiredQuantity;
        this.sufficient = sufficient;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getUnit() {
        return unit;
    }

    public double getAvailableQuantity() {
        return availableQuantity;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public boolean isSufficient() {
        return sufficient;
    }
}
