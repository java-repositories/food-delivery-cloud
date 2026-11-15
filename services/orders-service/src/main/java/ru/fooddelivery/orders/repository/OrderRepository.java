package ru.fooddelivery.orders.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fooddelivery.orders.domain.Order;

public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Flux<Order> findAllByCustomerId(String customerId, Pageable pageable);
    Flux<Order> findAllBy(Pageable pageable);
}
