package ru.yandex.praktikum.springwebmarketapp.repository.specifiation;

import org.springframework.data.jpa.domain.Specification;
import ru.yandex.praktikum.springwebmarketapp.model.Item;

public class ItemSpecification {

    public static Specification<Item> applySearch(String searchString) {
        return ((root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            String searchPattern = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern)
            );
        });
    }
}
