package ru.fooddelivery.orders.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.fooddelivery.orders.domain.OutboxEvent;

public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, Long> {
    Flux<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(String status);
}
