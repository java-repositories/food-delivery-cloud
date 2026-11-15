package ru.fooddelivery.orders.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long dishId,
        String dishName,
        BigDecimal price,
        Integer quantity
) {
}
