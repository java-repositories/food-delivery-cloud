package ru.fooddelivery.aggregate.dto;

import java.math.BigDecimal;

public record DishDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        String imageUrl
) {
}
