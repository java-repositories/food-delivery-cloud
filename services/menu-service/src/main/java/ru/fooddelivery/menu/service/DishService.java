package ru.fooddelivery.menu.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fooddelivery.menu.domain.Dish;
import ru.fooddelivery.menu.dto.*;
import ru.fooddelivery.menu.exception.DishNotFoundException;
import ru.fooddelivery.menu.mapper.DishMapper;
import ru.fooddelivery.menu.repository.DishRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DishService {

    private final DishRepository dishRepository;
    private final DishMapper dishMapper;

    public Page<DishResponse> findAll(DishFilter filter, Pageable pageable) {
        return dishRepository.findAllByFilter(filter, pageable)
                .map(dishMapper::toResponse);
    }

    public DishResponse findById(Long id) {
        return dishRepository.findById(id)
                .map(dishMapper::toResponse)
                .orElseThrow(() -> new DishNotFoundException(id));
    }

    @Transactional
    public DishResponse create(DishCreateRequest request) {
        Dish dish = dishMapper.toEntity(request);
        Dish saved = dishRepository.save(dish);
        return dishMapper.toResponse(saved);
    }

    @Transactional
    public DishResponse update(Long id, DishUpdateRequest request) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new DishNotFoundException(id));
        dishMapper.updateEntity(dish, request);
        return dishMapper.toResponse(dish);
    }

    @Transactional
    public void delete(Long id) {
        if (!dishRepository.existsById(id)) {
            throw new DishNotFoundException(id);
        }
        dishRepository.deleteById(id);
    }
}
