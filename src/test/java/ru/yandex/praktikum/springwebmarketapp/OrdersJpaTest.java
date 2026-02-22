package ru.yandex.praktikum.springwebmarketapp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.PostgreSQLTestContainer;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootTest(classes = SpringWebMarketAppApplication.class)
@Testcontainers
@ImportTestcontainers(PostgreSQLTestContainer.class)
@Sql(scripts = {"/schema.sql", "/ItemsInitScript.sql"})
public class OrdersJpaTest {

    @Test
    void createOrderTest(@Autowired OrderService orderService) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();

        request = new MockHttpServletRequest();
        session = new MockHttpSession();

        Item item1 = Item.builder()
                .id(1L)
                .title("Item 1")
                .description("Description 1")
                .price(100.00)
                .count(5)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .title("Item 2")
                .description("Description 2")
                .price(250.00)
                .count(4)
                .build();

        Item item3 = Item.builder()
                .id(3L)
                .title("Item 3")
                .description("Description 3")
                .price(250.00)
                .count(0)
                .build();

        Map<Long, Item> cartItems = Map.of(1L, item1, 2L, item2, 3L, item3);

        session.setAttribute(SessionService.SESSION_ITEM_QUANTITY, cartItems);
        request.setSession(session);

        Optional<Order> createdOrder = orderService.createNewOrder(request);

        Assertions.assertTrue(createdOrder.isPresent());

        Order order = createdOrder.get();

        Assertions.assertNotNull(order.getId());
        Assertions.assertEquals(2, order.getOrderItems().size());
        Assertions.assertEquals(1500.00, order.getTotalSum());

        Optional<Order> dbOrder = orderService.getOrder(order.getId());
        Assertions.assertTrue(dbOrder.isPresent());
        Assertions.assertEquals(order.getId(), dbOrder.get().getId());

        List<Order> orders = orderService.getAllOrders();
        Assertions.assertEquals(1, orders.size());
    }
}
