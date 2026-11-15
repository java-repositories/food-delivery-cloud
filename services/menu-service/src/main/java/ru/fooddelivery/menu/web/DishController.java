package ru.fooddelivery.menu.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.fooddelivery.menu.domain.Category;
import ru.fooddelivery.menu.dto.*;
import ru.fooddelivery.menu.service.DishService;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/dishes")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping
    public Page<DishResponse> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean availableOnly,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        DishFilter filter = new DishFilter(name, category, minPrice, maxPrice, availableOnly);
        return dishService.findAll(filter, pageable);
    }

    @GetMapping("/{id}")
    public DishResponse getById(@PathVariable Long id) {
        return dishService.findById(id);
    }

    @PostMapping
    public ResponseEntity<DishResponse> create(@Valid @RequestBody DishCreateRequest request) {
        DishResponse created = dishService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public DishResponse update(@PathVariable Long id, @Valid @RequestBody DishUpdateRequest request) {
        return dishService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        dishService.delete(id);
    }
}
