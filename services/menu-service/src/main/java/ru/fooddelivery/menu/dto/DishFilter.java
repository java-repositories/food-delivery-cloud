package ru.fooddelivery.menu.dto;

import ru.fooddelivery.menu.domain.Category;
import java.math.BigDecimal;

public record DishFilter(
        String name,
        Category category,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Boolean availableOnly
) {
}
