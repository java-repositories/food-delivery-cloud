package ru.fooddelivery.reviews.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fooddelivery.reviews.domain.DishRating;
import ru.fooddelivery.reviews.domain.Review;
import ru.fooddelivery.reviews.dto.DishRatingResponse;
import ru.fooddelivery.reviews.dto.ReviewCreateRequest;
import ru.fooddelivery.reviews.dto.ReviewResponse;
import ru.fooddelivery.reviews.exception.DuplicateReviewException;
import ru.fooddelivery.reviews.exception.ReviewNotFoundException;
import ru.fooddelivery.reviews.repository.DishRatingRepository;
import ru.fooddelivery.reviews.repository.ReviewRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private static final double PRIOR_MEAN = 4.0;
    private static final int CONFIDENCE_WEIGHT = 5;

    private final ReviewRepository reviewRepository;
    private final DishRatingRepository dishRatingRepository;

    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        if (reviewRepository.existsByDishIdAndCustomerId(request.dishId(), request.customerId())) {
            throw new DuplicateReviewException(request.dishId(), request.customerId());
        }

        Review review = Review.builder()
                .dishId(request.dishId())
                .customerId(request.customerId())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    public Page<ReviewResponse> findByDishId(Long dishId, Pageable pageable) {
        return reviewRepository.findAllByDishId(dishId, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public void delete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException(id);
        }
        reviewRepository.deleteById(id);
    }

    public DishRatingResponse getDishRating(Long dishId) {
        return dishRatingRepository.findById(dishId)
                .map(this::toRatingResponse)
                .orElseGet(() -> new DishRatingResponse(dishId, BigDecimal.ZERO, BigDecimal.ZERO, 0));
    }

    public List<DishRatingResponse> getDishRatings(List<Long> dishIds) {
        Map<Long, DishRating> ratingsMap = dishRatingRepository.findAllByDishIdIn(dishIds).stream()
                .collect(Collectors.toMap(DishRating::getDishId, Function.identity()));

        return dishIds.stream()
                .map(id -> ratingsMap.containsKey(id)
                        ? toRatingResponse(ratingsMap.get(id))
                        : new DishRatingResponse(id, BigDecimal.ZERO, BigDecimal.ZERO, 0))
                .toList();
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getDishId(),
                review.getCustomerId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private DishRatingResponse toRatingResponse(DishRating rating) {
        int count = rating.getReviewsCount();
        double avg = rating.getAverageRating().doubleValue();
        double weighted = (count * avg + CONFIDENCE_WEIGHT * PRIOR_MEAN) / (count + CONFIDENCE_WEIGHT);

        return new DishRatingResponse(
                rating.getDishId(),
                rating.getAverageRating(),
                BigDecimal.valueOf(weighted).setScale(2, RoundingMode.HALF_UP),
                count
        );
    }
}
