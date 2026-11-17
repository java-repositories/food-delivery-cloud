package ru.fooddelivery.orders.kafka;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        Long orderId,
        String customerId,
        String deliveryAddress,
        BigDecimal totalPrice,
        LocalDateTime createdAt
) {
}
