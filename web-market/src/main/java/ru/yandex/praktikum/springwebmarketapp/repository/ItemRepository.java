package ru.yandex.praktikum.springwebmarketapp.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Item;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    // только пагинация
    Flux<Item> findAllBy(Pageable pageable);

    // пагинация + where
    Flux<Item> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // для подсчёта общего количества по фильтру (для Page)
    Mono<Long> countByTitleContainingIgnoreCase(String title);

    //поис по ид
    Mono<Item> findById(Long id);

}
