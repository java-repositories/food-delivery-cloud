package ru.fooddelivery.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank(message = "ID клиента обязателен")
        @Size(max = 100)
        String customerId,

        @NotBlank(message = "Адрес доставки обязателен")
        @Size(max = 300)
        String deliveryAddress,

        @NotEmpty(message = "Заказ должен содержать хотя бы одно блюдо")
        @Valid
        List<OrderItemRequest> items
) {
}
