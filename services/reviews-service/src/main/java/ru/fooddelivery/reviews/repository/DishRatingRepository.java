package ru.fooddelivery.reviews.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fooddelivery.reviews.domain.DishRating;

import java.util.Collection;
import java.util.List;

public interface DishRatingRepository extends JpaRepository<DishRating, Long> {
    List<DishRating> findAllByDishIdIn(Collection<Long> dishIds);
}
