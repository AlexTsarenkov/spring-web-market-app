package ru.yandex.praktikum.springwebmarketapp.repository;

import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import ru.yandex.praktikum.springwebmarketapp.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Override
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<Order> findById(Long aLong);

    @Override
    List<Order> findAll();
}
