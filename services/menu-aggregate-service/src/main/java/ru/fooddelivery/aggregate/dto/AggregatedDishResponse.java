package ru.fooddelivery.aggregate.dto;

import java.math.BigDecimal;

public record AggregatedDishResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        String imageUrl,
        BigDecimal averageRating,
        BigDecimal weightedRating,
        Integer reviewsCount
) {
    public static AggregatedDishResponse of(DishDto dish, DishRatingDto rating) {
        DishRatingDto safeRating = rating != null ? rating : DishRatingDto.empty(dish.id());
        return new AggregatedDishResponse(
                dish.id(),
                dish.name(),
                dish.description(),
                dish.price(),
                dish.category(),
                dish.available(),
                dish.imageUrl(),
                safeRating.averageRating(),
                safeRating.weightedRating(),
                safeRating.reviewsCount()
        );
    }
}
