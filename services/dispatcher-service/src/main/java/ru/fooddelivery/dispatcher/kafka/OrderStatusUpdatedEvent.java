package ru.fooddelivery.dispatcher.kafka;

public record OrderStatusUpdatedEvent(
        Long orderId,
        String newStatus,
        String courierName
) {
}
