package ru.fooddelivery.orders.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.fooddelivery.orders.exception.DishUnavailableException;

@Component
@RequiredArgsConstructor
public class MenuClient {

    private final WebClient menuWebClient;

    public Mono<MenuDishDto> getDishById(Long dishId) {
        return menuWebClient.get()
                .uri("/api/v1/dishes/{id}", dishId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new DishUnavailableException("Блюдо с id=" + dishId + " не найдено в меню")))
                .bodyToMono(MenuDishDto.class)
                .flatMap(dish -> {
                    if (!dish.available()) {
                        return Mono.error(new DishUnavailableException("Блюдо '" + dish.name() + "' временно недоступно для заказа"));
                    }
                    return Mono.just(dish);
                });
    }
}
