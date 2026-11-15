package ru.fooddelivery.reviews.dto;

import jakarta.validation.constraints.*;

public record ReviewCreateRequest(
        @NotNull(message = "ID блюда обязателен")
        Long dishId,

        @NotBlank(message = "ID клиента обязателен")
        @Size(max = 100)
        String customerId,

        @NotNull(message = "Оценка обязательна")
        @Min(value = 1, message = "Минимальная оценка — 1")
        @Max(value = 5, message = "Максимальная оценка — 5")
        Integer rating,

        @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
        String comment
) {
}
