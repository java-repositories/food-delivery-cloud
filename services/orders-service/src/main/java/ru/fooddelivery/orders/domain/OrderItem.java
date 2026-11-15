package ru.fooddelivery.orders.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    private Long id;

    private Long orderId;
    private Long dishId;
    private String dishName;
    private BigDecimal price;
    private Integer quantity;
}
