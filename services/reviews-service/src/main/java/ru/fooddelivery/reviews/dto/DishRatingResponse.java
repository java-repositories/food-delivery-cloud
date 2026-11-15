package ru.fooddelivery.reviews.dto;

import java.math.BigDecimal;

public record DishRatingResponse(
        Long dishId,
        BigDecimal averageRating,
        BigDecimal weightedRating,
        Integer reviewsCount
) {
}
