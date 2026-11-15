package ru.fooddelivery.orders.exception;

public class DishUnavailableException extends RuntimeException {
    public DishUnavailableException(String message) {
        super(message);
    }
}
