package ru.fooddelivery.reviews.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(Long id) {
        super("Отзыв с id=" + id + " не найден");
    }
}
