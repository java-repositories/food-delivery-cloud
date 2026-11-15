package ru.fooddelivery.menu.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.util.StringUtils;
import ru.fooddelivery.menu.domain.Dish;
import ru.fooddelivery.menu.dto.DishFilter;

import java.util.ArrayList;
import java.util.List;

public class DishRepositoryImpl implements DishRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Dish> findAllByFilter(DishFilter filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Dish> query = cb.createQuery(Dish.class);
        Root<Dish> root = query.from(Dish.class);
        List<Predicate> predicates = buildPredicates(filter, cb, root);
        query.where(predicates.toArray(new Predicate[0]));

        if (pageable.getSort().isSorted()) {
            query.orderBy(QueryUtils.toOrders(pageable.getSort(), root, cb));
        }

        TypedQuery<Dish> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<Dish> dishes = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Dish> countRoot = countQuery.from(Dish.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(dishes, pageable, total);
    }

    private List<Predicate> buildPredicates(DishFilter filter, CriteriaBuilder cb, Root<Dish> root) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter == null) {
            return predicates;
        }
        if (StringUtils.hasText(filter.name())) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.name().toLowerCase() + "%"));
        }
        if (filter.category() != null) {
            predicates.add(cb.equal(root.get("category"), filter.category()));
        }
        if (filter.minPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
        }
        if (Boolean.TRUE.equals(filter.availableOnly())) {
            predicates.add(cb.isTrue(root.get("available")));
        }
        return predicates;
    }
}
