package ru.fooddelivery.menu.dto;

import ru.fooddelivery.menu.domain.Category;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DishResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Category category,
        boolean available,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
