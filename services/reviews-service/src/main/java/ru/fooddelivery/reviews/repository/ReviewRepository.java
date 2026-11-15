package ru.fooddelivery.reviews.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.fooddelivery.reviews.domain.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAllByDishId(Long dishId, Pageable pageable);
    boolean existsByDishIdAndCustomerId(Long dishId, String customerId);
}
