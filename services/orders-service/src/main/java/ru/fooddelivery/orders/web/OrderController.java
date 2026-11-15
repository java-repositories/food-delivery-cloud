package ru.fooddelivery.orders.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fooddelivery.orders.dto.OrderCreateRequest;
import ru.fooddelivery.orders.dto.OrderResponse;
import ru.fooddelivery.orders.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public Mono<OrderResponse> getById(@PathVariable Long id) {
        return orderService.findById(id);
    }

    @GetMapping
    public Flux<OrderResponse> getAll(
            @RequestParam(required = false) String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return orderService.findAll(customerId, page, size);
    }
}
