package ru.fooddelivery.orders.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.fooddelivery.orders.repository.OutboxEventRepository;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    public static final String TOPIC_ORDERS_CREATED = "orders.created";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("NEW")
                .flatMap(event -> Mono.fromFuture(
                                kafkaTemplate.send(TOPIC_ORDERS_CREATED, event.getAggregateId(), event.getPayload())
                        )
                        .flatMap(result -> {
                            event.setStatus("SENT");
                            event.setProcessedAt(LocalDateTime.now());
                            return outboxEventRepository.save(event);
                        })
                        .doOnSuccess(saved -> log.info("Событие Outbox id={} отправлено в Kafka", saved.getId()))
                        .onErrorResume(ex -> {
                            log.error("Ошибка отправки Outbox id={}: {}", event.getId(), ex.getMessage());
                            return Mono.empty();
                        }))
                .subscribe();
    }
}
