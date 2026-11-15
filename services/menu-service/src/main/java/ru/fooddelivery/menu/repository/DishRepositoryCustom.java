package ru.fooddelivery.menu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.fooddelivery.menu.domain.Dish;
import ru.fooddelivery.menu.dto.DishFilter;

public interface DishRepositoryCustom {
    Page<Dish> findAllByFilter(DishFilter filter, Pageable pageable);
}
