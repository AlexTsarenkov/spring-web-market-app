package ru.yandex.praktikum.springwebmarketapp.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.repository.ItemRepository;
import ru.yandex.praktikum.springwebmarketapp.repository.specifiation.ItemSpecification;
import ru.yandex.praktikum.springwebmarketapp.utill.SortCriteria;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;

    public Page<Item> findAll(String searchString, String sortBy, Integer page, Integer pageSize) {
        // Спецификация для поиска
        Specification<Item> specification = Specification
                .where(ItemSpecification.applySearch(searchString));

        //сортировка
        Sort sort = null;
        try {
            SortCriteria criteria = SortCriteria.valueOf(sortBy);
            sort = switch (criteria) {
                case NO -> null;
                case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
                case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            };
        } catch (IllegalArgumentException e) {
            //No sort
        }

        // Пагинация по страницам
        Pageable pageable = null;
        if (sort != null) {
            pageable = PageRequest.of(page - 1, pageSize, sort);
        } else {
            pageable = PageRequest.of(page - 1, pageSize);
        }

        //Получим данные через Hibernante

        return itemRepository.findAll(specification, pageable);
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
