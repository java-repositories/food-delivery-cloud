package ru.fooddelivery.aggregate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fooddelivery.aggregate.client.MenuServiceClient;
import ru.fooddelivery.aggregate.client.ReviewsServiceClient;
import ru.fooddelivery.aggregate.dto.AggregatedDishResponse;
import ru.fooddelivery.aggregate.dto.DishDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuAggregateService {

    private final MenuServiceClient menuServiceClient;
    private final ReviewsServiceClient reviewsServiceClient;

    public Mono<AggregatedDishResponse> getAggregatedDishById(Long id) {
        return Mono.zip(
                        menuServiceClient.getDishById(id),
                        reviewsServiceClient.getDishRating(id)
                )
                .map(tuple -> AggregatedDishResponse.of(tuple.getT1(), tuple.getT2()));
    }

    public Flux<AggregatedDishResponse> getAggregatedMenu(String category, int page, int size) {
        return menuServiceClient.getDishes(category, page, size)
                .flatMapMany(dishPage -> {
                    List<DishDto> dishes = dishPage.content();
                    if (dishes == null || dishes.isEmpty()) {
                        return Flux.empty();
                    }
                    List<Long> dishIds = dishes.stream().map(DishDto::id).toList();
                    return reviewsServiceClient.getDishRatings(dishIds)
                            .flatMapMany(ratingsMap -> Flux.fromIterable(dishes)
                                    .map(dish -> AggregatedDishResponse.of(dish, ratingsMap.get(dish.id()))));
                });
    }
}
