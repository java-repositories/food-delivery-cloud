package ru.fooddelivery.aggregate.dto;

import java.math.BigDecimal;

public record DishRatingDto(
        Long dishId,
        BigDecimal averageRating,
        BigDecimal weightedRating,
        Integer reviewsCount
) {
    public static DishRatingDto empty(Long dishId) {
        return new DishRatingDto(dishId, BigDecimal.ZERO, BigDecimal.ZERO, 0);
    }
}
