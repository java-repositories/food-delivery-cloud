package ru.fooddelivery.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fooddelivery.orders.client.MenuClient;
import ru.fooddelivery.orders.domain.Order;
import ru.fooddelivery.orders.domain.OrderItem;
import ru.fooddelivery.orders.domain.OrderStatus;
import ru.fooddelivery.orders.domain.OutboxEvent;
import ru.fooddelivery.orders.dto.*;
import ru.fooddelivery.orders.exception.OrderNotFoundException;
import ru.fooddelivery.orders.kafka.OrderCreatedEvent;
import ru.fooddelivery.orders.repository.OrderItemRepository;
import ru.fooddelivery.orders.repository.OrderRepository;
import ru.fooddelivery.orders.repository.OutboxEventRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuClient menuClient;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Mono<OrderResponse> createOrder(OrderCreateRequest request) {
        // Группируем одинаковые dishId и суммируем их количество
        Map<Long, Integer> dishQuantities = request.items().stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::dishId,
                        OrderItemRequest::quantity,
                        Integer::sum
                ));

        return Flux.fromIterable(dishQuantities.entrySet())
                .flatMap(entry -> menuClient.getDishById(entry.getKey())
                        .map(dish -> OrderItem.builder()
                                .dishId(dish.id())
                                .dishName(dish.name())
                                .price(dish.price())
                                .quantity(entry.getValue())
                                .build()))
                .collectList()
                .flatMap(items -> {
                    BigDecimal totalPrice = items.stream()
                            .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Order order = Order.builder()
                            .customerId(request.customerId())
                            .deliveryAddress(request.deliveryAddress())
                            .status(OrderStatus.NEW)
                            .totalPrice(totalPrice)
                            .build();

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                items.forEach(item -> item.setOrderId(savedOrder.getId()));
                                return orderItemRepository.saveAll(items)
                                        .collectList()
                                        .flatMap(savedItems -> saveOutboxEvent(savedOrder)
                                                .thenReturn(mapToResponse(savedOrder, savedItems)));
                            });
                });
    }

    private Mono<OutboxEvent> saveOutboxEvent(Order savedOrder) {
        OrderCreatedEvent eventPayload = new OrderCreatedEvent(
                savedOrder.getId(),
                savedOrder.getCustomerId(),
                savedOrder.getDeliveryAddress(),
                savedOrder.getTotalPrice(),
                savedOrder.getCreatedAt()
        );
        try {
            OutboxEvent outbox = OutboxEvent.builder()
                    .aggregateType("ORDER")
                    .aggregateId(String.valueOf(savedOrder.getId()))
                    .eventType("ORDER_CREATED")
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .status("NEW")
                    .build();
            return outboxEventRepository.save(outbox);
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    public Mono<OrderResponse> findById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(this::enrichWithItems);
    }

    public Flux<OrderResponse> findAll(String customerId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Flux<Order> orders = (customerId != null && !customerId.isBlank())
                ? orderRepository.findAllByCustomerId(customerId, pageable)
                : orderRepository.findAllBy(pageable);

        return orders.concatMap(this::enrichWithItems);
    }

    private Mono<OrderResponse> enrichWithItems(Order order) {
        return orderItemRepository.findAllByOrderId(order.getId())
                .collectList()
                .map(items -> mapToResponse(order, items));
    }

    private OrderResponse mapToResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(i -> new OrderItemResponse(i.getDishId(), i.getDishName(), i.getPrice(), i.getQuantity()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getDeliveryAddress(),
                order.getStatus(),
                order.getTotalPrice(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
