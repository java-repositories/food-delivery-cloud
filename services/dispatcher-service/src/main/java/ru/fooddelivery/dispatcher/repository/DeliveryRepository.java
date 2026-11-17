package ru.fooddelivery.dispatcher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.fooddelivery.dispatcher.domain.Delivery;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}
