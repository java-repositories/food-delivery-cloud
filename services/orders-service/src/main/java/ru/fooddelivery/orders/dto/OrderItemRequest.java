package ru.fooddelivery.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
        @NotNull(message = "ID блюда обязателен")
        Long dishId,

        @NotNull(message = "Количество обязательно")
        @Min(value = 1, message = "Минимальное количество — 1")
        Integer quantity
) {
}
