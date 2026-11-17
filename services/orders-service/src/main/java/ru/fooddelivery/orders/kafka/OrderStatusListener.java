package ru.fooddelivery.orders.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.fooddelivery.orders.repository.OrderRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusListener {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders.status-updated", groupId = "orders-service-group")
    public void onStatusUpdated(String message) {
        try {
            OrderStatusUpdatedEvent event = objectMapper.readValue(message, OrderStatusUpdatedEvent.class);
            orderRepository.findById(event.orderId())
                    .flatMap(order -> {
                        order.setStatus(event.newStatus());
                        return orderRepository.save(order);
                    })
                    .doOnSuccess(updated -> log.info("Статус заказа id={} обновлен на {}", event.orderId(), event.newStatus()))
                    .subscribe();
        } catch (Exception e) {
            log.error("Ошибка десериализации события статуса заказа: {}", message, e);
        }
    }
}
