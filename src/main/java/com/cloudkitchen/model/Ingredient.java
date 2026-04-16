package com.cloudkitchen.model;

public class Ingredient {
    private final long id;
    private final String name;
    private final String unit;
    private final double quantity;
    private final double thresholdQuantity;

    public Ingredient(long id, String name, String unit, double quantity, double thresholdQuantity) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
        this.thresholdQuantity = thresholdQuantity;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getThresholdQuantity() {
        return thresholdQuantity;
    }

    public boolean isLowStock() {
        return quantity <= thresholdQuantity;
    }
}
