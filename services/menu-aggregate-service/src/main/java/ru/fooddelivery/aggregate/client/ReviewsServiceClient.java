package ru.fooddelivery.aggregate.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.fooddelivery.aggregate.dto.DishRatingDto;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewsServiceClient {

    private final WebClient reviewsWebClient;

    @CircuitBreaker(name = "reviewsService", fallbackMethod = "fallbackRating")
    @Retry(name = "reviewsService")
    public Mono<DishRatingDto> getDishRating(Long dishId) {
        return reviewsWebClient.get()
                .uri("/api/v1/dishes/{dishId}/rating", dishId)
                .retrieve()
                .bodyToMono(DishRatingDto.class);
    }

    @CircuitBreaker(name = "reviewsService", fallbackMethod = "fallbackRatingsMap")
    @Retry(name = "reviewsService")
    public Mono<Map<Long, DishRatingDto>> getDishRatings(List<Long> dishIds) {
        if (dishIds == null || dishIds.isEmpty()) {
            return Mono.just(Collections.emptyMap());
        }
        return reviewsWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/dishes/ratings")
                        .queryParam("dishIds", dishIds)
                        .build())
                .retrieve()
                .bodyToFlux(DishRatingDto.class)
                .collectMap(DishRatingDto::dishId, Function.identity());
    }

    private Mono<DishRatingDto> fallbackRating(Long dishId, Throwable ex) {
        log.warn("Fallback сработал для рейтинга блюда id={}. Причина: {}", dishId, ex.getMessage());
        return Mono.just(DishRatingDto.empty(dishId));
    }

    private Mono<Map<Long, DishRatingDto>> fallbackRatingsMap(List<Long> dishIds, Throwable ex) {
        log.warn("Fallback сработал для списка рейтингов {}. Причина: {}", dishIds, ex.getMessage());
        Map<Long, DishRatingDto> fallbackMap = dishIds.stream()
                .collect(Collectors.toMap(Function.identity(), DishRatingDto::empty));
        return Mono.just(fallbackMap);
    }
}
