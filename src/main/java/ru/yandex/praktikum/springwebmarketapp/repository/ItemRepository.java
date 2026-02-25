package ru.yandex.praktikum.springwebmarketapp.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import ru.yandex.praktikum.springwebmarketapp.model.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    @Override
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Page<Item> findAll(Specification<Item> spec, Pageable pageable);
}
