package ru.yandex.praktikum.springwebmarketapp.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.repository.ItemRepository;
import ru.yandex.praktikum.springwebmarketapp.utill.SortCriteria;

import java.util.ArrayList;
import java.util.List;

import static ru.yandex.praktikum.springwebmarketapp.utill.SortCriteria.PRICE;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Mono<Page<Item>> findAll(String searchString, String sortBy, Integer page, Integer pageSize) {

        //сортировка
        Sort sort = null;
        try {
            SortCriteria criteria = SortCriteria.valueOf(sortBy);
            sort = switch (criteria) {
                case NO -> Sort.unsorted();
                case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
                case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            };
        } catch (IllegalArgumentException e) {
            //No sort
        }

        // Пагинация по страницам
        Pageable pageable;
        if (sort != null) {
            pageable = PageRequest.of(page - 1, pageSize, sort);
        } else {
            pageable = PageRequest.of(page - 1, pageSize);
        }

        Flux<Item> itemsFlux;
        Mono<Long> countMono;

        //Получим данные через Hibernante
        if (searchString == null || searchString.isBlank()) {
            itemsFlux = itemRepository.findAllBy(pageable);
            countMono = itemRepository.count();
        } else {
            itemsFlux = itemRepository.findByTitleContainingIgnoreCase(searchString, pageable);
            countMono = itemRepository.countByTitleContainingIgnoreCase(searchString);
        }

        return Mono.zip(itemsFlux.collectList(), countMono,
                (items, total) -> new PageImpl<>(items, pageable, total));
    }

    public Mono<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Flux<Item> findByIds(Iterable<Long> ids) {
        return itemRepository.findAllById(ids);
    }

    public List<List<Item>> prepareDataForModel(List<Item> items) {
        //Разложим в соответствии с шаблоном
        List<List<Item>> result = new ArrayList<>();
        int chunkSize = 3;
        for (int i = 0; i < items.size(); i += chunkSize) {
            List<Item> chunk = items.subList(i, Math.min(i + chunkSize, items.size()));
            List<Item> toAdd = new ArrayList<>();
            toAdd.addAll(chunk);
            if (toAdd.size() < 3) {
                while (toAdd.size() < 3) {
                    toAdd.add(createPlaceholderItem());
                }
            }
            result.add(toAdd);
        }
        return result;
    }

    private Item createPlaceholderItem() {
        return Item.builder()
                .id(-1L)
                .title("")
                .description("")
                .imgPath("")
                .price(0.0)
                .count(0)
                .build();
    }
}
