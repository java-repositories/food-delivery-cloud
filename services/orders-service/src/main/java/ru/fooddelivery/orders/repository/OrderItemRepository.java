package ru.fooddelivery.orders.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fooddelivery.orders.domain.OrderItem;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItem, Long> {
    Flux<OrderItem> findAllByOrderId(Long orderId);
}
