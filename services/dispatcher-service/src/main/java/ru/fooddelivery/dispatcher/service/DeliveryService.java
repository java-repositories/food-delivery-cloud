package ru.fooddelivery.dispatcher.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fooddelivery.dispatcher.domain.Delivery;
import ru.fooddelivery.dispatcher.domain.DeliveryStatus;
import ru.fooddelivery.dispatcher.kafka.OrderCreatedEvent;
import ru.fooddelivery.dispatcher.kafka.OrderStatusUpdatedEvent;
import ru.fooddelivery.dispatcher.repository.DeliveryRepository;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final List<String> COURIERS = List.of("Алексей К.", "Дмитрий С.", "Иван П.", "Максим В.");

    private final DeliveryRepository deliveryRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void assignCourier(OrderCreatedEvent event) {
        if (deliveryRepository.findByOrderId(event.orderId()).isPresent()) {
            return; // Идемпотентность: защита от повторной обработки дублей из Kafka
        }

        String courier = COURIERS.get(ThreadLocalRandom.current().nextInt(COURIERS.size()));
        Delivery delivery = Delivery.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .deliveryAddress(event.deliveryAddress())
                .courierName(courier)
                .status(DeliveryStatus.ASSIGNED)
                .build();

        deliveryRepository.save(delivery);
        log.info("Назначен курьер {} на заказ id={}", courier, event.orderId());

        sendStatusUpdate(event.orderId(), "COOKING", courier);
    }

    @Transactional
    public Delivery updateDeliveryStatus(Long orderId, DeliveryStatus newStatus) {
        Delivery delivery = deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Доставка для заказа id=" + orderId + " не найдена"));

        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepository.save(delivery);

        String orderStatus = switch (newStatus) {
            case ASSIGNED -> "COOKING";
            case ON_THE_WAY -> "DELIVERING";
            case DELIVERED -> "COMPLETED";
        };
        sendStatusUpdate(orderId, orderStatus, saved.getCourierName());
        return saved;
    }

    private void sendStatusUpdate(Long orderId, String orderStatus, String courierName) {
        try {
            OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(orderId, orderStatus, courierName);
            kafkaTemplate.send("orders.status-updated", String.valueOf(orderId), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            log.error("Ошибка отправки обновления статуса в Kafka для заказа id={}", orderId, e);
        }
    }
}
