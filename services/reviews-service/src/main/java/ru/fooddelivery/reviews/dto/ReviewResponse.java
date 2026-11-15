package ru.fooddelivery.reviews.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long dishId,
        String customerId,
        Integer rating,
        String comment,
        LocalDateTime createdAt
) {
}
