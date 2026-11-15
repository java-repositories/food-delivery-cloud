package ru.fooddelivery.menu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fooddelivery.menu.domain.Dish;

public interface DishRepository extends JpaRepository<Dish, Long>, DishRepositoryCustom {
}
