package ru.fooddelivery.dispatcher.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.fooddelivery.dispatcher.domain.Delivery;
import ru.fooddelivery.dispatcher.domain.DeliveryStatus;
import ru.fooddelivery.dispatcher.repository.DeliveryRepository;
import ru.fooddelivery.dispatcher.service.DeliveryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryService deliveryService;

    @GetMapping
    public List<Delivery> getAll() {
        return deliveryRepository.findAll();
    }

    @PatchMapping("/order/{orderId}/status")
    public Delivery updateStatus(@PathVariable Long orderId, @RequestParam DeliveryStatus status) {
        return deliveryService.updateDeliveryStatus(orderId, status);
    }
}
