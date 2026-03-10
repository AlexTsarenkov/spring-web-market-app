package ru.yandex.praktikum.springwebmarketapp.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.yandex.praktikum.springwebmarketapp.model.Order;

public interface OrderRepository extends R2dbcRepository<Order, Long> {

}
