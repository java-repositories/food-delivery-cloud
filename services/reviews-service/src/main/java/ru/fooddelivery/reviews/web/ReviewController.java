package ru.fooddelivery.reviews.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.fooddelivery.reviews.dto.DishRatingResponse;
import ru.fooddelivery.reviews.dto.ReviewCreateRequest;
import ru.fooddelivery.reviews.dto.ReviewResponse;
import ru.fooddelivery.reviews.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(request);
    }

    @GetMapping("/dishes/{dishId}/reviews")
    public Page<ReviewResponse> getByDishId(
            @PathVariable Long dishId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return reviewService.findByDishId(dishId, pageable);
    }

    @DeleteMapping("/reviews/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reviewService.delete(id);
    }

    @GetMapping("/dishes/{dishId}/rating")
    public DishRatingResponse getDishRating(@PathVariable Long dishId) {
        return reviewService.getDishRating(dishId);
    }

    @GetMapping("/dishes/ratings")
    public List<DishRatingResponse> getDishRatings(@RequestParam List<Long> dishIds) {
        return reviewService.getDishRatings(dishIds);
    }
}
