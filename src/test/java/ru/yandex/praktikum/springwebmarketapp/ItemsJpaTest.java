package ru.yandex.praktikum.springwebmarketapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.data.domain.Page;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.repository.ItemRepository;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.PostgreSQLTestContainer;
import ru.yandex.praktikum.springwebmarketapp.utill.SortCriteria;

import java.util.List;

@SpringBootTest(classes = SpringWebMarketAppApplication.class)
@Testcontainers
@ImportTestcontainers(PostgreSQLTestContainer.class)
@Sql(scripts = {"/schema.sql", "/ItemsInitScript.sql"})
public class ItemsJpaTest {
    @Test
    public void itemsFetchTest(@Autowired ItemRepository itemRepository) {
        List<Item> testItems = itemRepository.findAll();
        Assertions.assertNotNull(testItems);
        Assertions.assertFalse(testItems.isEmpty());
        Assertions.assertEquals(26, testItems.size());
    }

    @Test
    public void pagingSimpleTest(@Autowired ItemService itemService) {
        Page<Item> itemPage = itemService.findAll(null, SortCriteria.NO.toString(), 1, 5);
        Assertions.assertNotNull(itemPage);
        Assertions.assertFalse(itemPage.isEmpty());
        Assertions.assertEquals(5, itemPage.getContent().size());

        Page<Item> itemPage20 = itemService.findAll(null, SortCriteria.NO.toString(), 2, 20);
        Assertions.assertNotNull(itemPage20);
        Assertions.assertFalse(itemPage20.isEmpty());
        Assertions.assertEquals(6, itemPage20.getContent().size());
    }

    @Test
    public void pagingSearchTest(@Autowired ItemService itemService) {
        Page<Item> itemPage = itemService.findAll("Смартфон X10 Pro", SortCriteria.NO.toString(),
                1, 5);
        Assertions.assertNotNull(itemPage);
        Assertions.assertFalse(itemPage.isEmpty());
        Assertions.assertEquals(1, itemPage.getContent().size());
        Assertions.assertEquals(1L, itemPage.getContent().get(0).getId());
    }

    @Test
    public void pagingSortTest(@Autowired ItemService itemService) {
        Page<Item> itemPage = itemService.findAll(null, SortCriteria.PRICE.toString(), 1, 5);
        Assertions.assertNotNull(itemPage);
        Assertions.assertFalse(itemPage.isEmpty());
        Assertions.assertEquals(5, itemPage.getContent().size());
        Item item = itemPage.getContent().get(0);
        Assertions.assertEquals(23L, item.getId());
        Assertions.assertEquals("Мышь беспроводная OfficeMate", item.getTitle());
    }

    @Test
    public void pagingTemplateCorrect(@Autowired ItemService itemService) {
        Page<Item> itemPage = itemService.findAll(null, SortCriteria.PRICE.toString(), 1, 20);
        List<List<Item>> items = itemService.prepareDataForModel(itemPage.getContent());
        Assertions.assertNotNull(items);
        Assertions.assertFalse(items.isEmpty());
        Assertions.assertEquals(7, items.size());
        Assertions.assertEquals(3, items.get(0).size());
    }
}
