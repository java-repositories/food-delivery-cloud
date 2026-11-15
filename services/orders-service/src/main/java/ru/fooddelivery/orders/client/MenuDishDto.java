package ru.fooddelivery.orders.client;

import java.math.BigDecimal;

public record MenuDishDto(
        Long id,
        String name,
        BigDecimal price,
        boolean available
) {
}
