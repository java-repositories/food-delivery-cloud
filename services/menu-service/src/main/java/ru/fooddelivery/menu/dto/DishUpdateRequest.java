package ru.fooddelivery.menu.dto;

import jakarta.validation.constraints.*;
import ru.fooddelivery.menu.domain.Category;

import java.math.BigDecimal;

public record DishUpdateRequest(
        @NotBlank(message = "Название блюда не может быть пустым")
        @Size(max = 120)
        String name,

        @Size(max = 500)
        String description,

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.01")
        BigDecimal price,

        @NotNull(message = "Категория обязательна")
        Category category,

        boolean available,

        @Size(max = 255)
        String imageUrl
) {
}
