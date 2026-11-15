package ru.fooddelivery.menu.dto;

import jakarta.validation.constraints.*;
import ru.fooddelivery.menu.domain.Category;

import java.math.BigDecimal;

public record DishCreateRequest(
        @NotBlank(message = "Название блюда не может быть пустым")
        @Size(max = 120, message = "Название блюда не должно превышать 120 символов")
        String name,

        @Size(max = 500, message = "Описание не должно превышать 500 символов")
        String description,

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.01", message = "Цена должна быть больше нуля")
        BigDecimal price,

        @NotNull(message = "Категория обязательна")
        Category category,

        Boolean available,

        @Size(max = 255)
        String imageUrl
) {
}
