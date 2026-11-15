package ru.fooddelivery.aggregate.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.fooddelivery.aggregate.dto.DishDto;
import ru.fooddelivery.aggregate.dto.DishPageDto;
import ru.fooddelivery.aggregate.exception.DishNotFoundException;
import ru.fooddelivery.aggregate.exception.ExternalServiceException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MenuServiceClient {

    private final WebClient menuWebClient;

    public Mono<DishDto> getDishById(Long id) {
        return menuWebClient.get()
                .uri("/api/v1/dishes/{id}", id)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new DishNotFoundException(id)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ExternalServiceException("Ошибка сервиса меню", null)))
                .bodyToMono(DishDto.class)
                .onErrorMap(ex -> !(ex instanceof DishNotFoundException),
                        ex -> new ExternalServiceException("Сервис меню недоступен", ex));
    }

    public Mono<DishPageDto> getDishes(String category, int page, int size) {
        return menuWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/dishes")
                        .queryParamIfPresent("category", Optional.ofNullable(category))
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(DishPageDto.class)
                .onErrorMap(ex -> new ExternalServiceException("Сервис меню недоступен", ex));
    }
}
