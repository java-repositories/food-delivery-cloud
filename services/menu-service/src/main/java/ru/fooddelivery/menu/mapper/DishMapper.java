package ru.fooddelivery.menu.mapper;

import org.mapstruct.*;
import ru.fooddelivery.menu.domain.Dish;
import ru.fooddelivery.menu.dto.DishCreateRequest;
import ru.fooddelivery.menu.dto.DishResponse;
import ru.fooddelivery.menu.dto.DishUpdateRequest;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DishMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "available", expression = "java(request.available() != null ? request.available() : true)")
    Dish toEntity(DishCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Dish dish, DishUpdateRequest request);

    DishResponse toResponse(Dish dish);
}
