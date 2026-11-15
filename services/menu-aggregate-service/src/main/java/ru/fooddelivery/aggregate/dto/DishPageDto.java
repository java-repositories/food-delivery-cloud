package ru.fooddelivery.aggregate.dto;

import java.util.List;

public record DishPageDto(
        List<DishDto> content,
        int number,
        int size,
        long totalElements,
        int totalPages
) {
}
