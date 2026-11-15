package ru.fooddelivery.aggregate.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.fooddelivery.aggregate.dto.AggregatedDishResponse;
import ru.fooddelivery.aggregate.service.MenuAggregateService;

@RestController
@RequestMapping("/api/v1/menu-aggregate")
@RequiredArgsConstructor
public class MenuAggregateController {

    private final MenuAggregateService aggregateService;

    @GetMapping("/dishes/{id}")
    public Mono<AggregatedDishResponse> getDishById(@PathVariable Long id) {
        return aggregateService.getAggregatedDishById(id);
    }

    @GetMapping("/dishes")
    public Flux<AggregatedDishResponse> getMenu(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return aggregateService.getAggregatedMenu(category, page, size);
    }
}
