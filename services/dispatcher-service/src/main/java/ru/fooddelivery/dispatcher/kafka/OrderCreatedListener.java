package ru.fooddelivery.dispatcher.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.fooddelivery.dispatcher.service.DeliveryService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {

    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "orders.created", groupId = "dispatcher-service-group")
    public void onOrderCreated(String message) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(message, OrderCreatedEvent.class);
            deliveryService.assignCourier(event);
        } catch (Exception e) {
            log.error("Ошибка обработки события создания заказа: {}", message, e);
        }
    }
}
