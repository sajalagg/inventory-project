package com.cloudkitchen.model;

import java.time.LocalDateTime;

public class OrderRecord {
    private final long id;
    private final String dishName;
    private final String customerName;
    private final int quantity;
    private final double totalPrice;
    private final LocalDateTime orderedAt;

    public OrderRecord(
            long id,
            String dishName,
            String customerName,
            int quantity,
            double totalPrice,
            LocalDateTime orderedAt) {
        this.id = id;
        this.dishName = dishName;
        this.customerName = customerName;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.orderedAt = orderedAt;
    }

    public long getId() {
        return id;
    }

    public String getDishName() {
        return dishName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getOrderedAt() {
        return orderedAt;
    }
}
