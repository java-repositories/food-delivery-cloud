package ru.fooddelivery.orders.kafka;

import ru.fooddelivery.orders.domain.OrderStatus;

public record OrderStatusUpdatedEvent(
        Long orderId,
        OrderStatus newStatus,
        String courierName
) {
}
