package ru.fooddelivery.orders.dto;

import ru.fooddelivery.orders.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerId,
        String deliveryAddress,
        OrderStatus status,
        BigDecimal totalPrice,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
