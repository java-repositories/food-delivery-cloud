package ru.fooddelivery.reviews.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException(Long dishId, String customerId) {
        super("Пользователь " + customerId + " уже оставил отзыв на блюдо id=" + dishId);
    }
}
