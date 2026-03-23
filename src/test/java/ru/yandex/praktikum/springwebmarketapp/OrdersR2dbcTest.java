package ru.yandex.praktikum.springwebmarketapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.server.MockWebSession;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.model.OrderItem;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderItemRepository;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.PostgreSQLTestContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SpringWebMarketAppApplication.class)
@Testcontainers
@ImportTestcontainers(PostgreSQLTestContainer.class)
class OrdersR2dbcTest {

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    OrderService orderService;

    @Autowired
    SessionService sessionService;

    @Autowired
    OrderItemRepository orderItemRepository;

    @BeforeEach
    void initDb() throws IOException {
        String schemaSql = readClasspathSql("schema.sql");
        String dataSql = readClasspathSql("ItemsInitScript.sql");

        databaseClient.sql(schemaSql).then().block();
        databaseClient.sql(dataSql).then().block();
    }

    private String readClasspathSql(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test
    void createOrderReactiveTest() {
        // Подготовим корзину в сессии
        MockWebSession session = new MockWebSession();

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

        double totalPrice = 5 * 100.00 + 4 * 250.00; // 1500.00
        Cart cart = new Cart(List.of(item1, item2), totalPrice);

        // Сохраняем Cart в сессию через SessionService
        sessionService.saveCurrentCart(session, cart).block();

        // Создаём заказ
        Order order = orderService.createNewOrder(session).block();
        assertNotNull(order);
        assertNotNull(order.getId());

        // Проверяем, что OrderItem реально сохранились в БД
        List<OrderItem> orderItems = orderItemRepository
                .findByOrderId(order.getId())
                .collectList()
                .block();

        assertNotNull(orderItems);
        assertEquals(2, orderItems.size());

        // totalSum должен быть 1500.00 (с учётом округления в геттере)
        assertEquals(1500.00, order.getTotalSum(), 0.001);

        // Проверяем, что заказ можно прочитать обратно из сервиса
        Order dbOrder = orderService.getOrder(order.getId()).block();
        assertNotNull(dbOrder);
        assertEquals(order.getId(), dbOrder.getId());

        // Проверяем общее количество заказов
        List<Order> orders = orderService.getAllOrders()
                .collectList()
                .block();
        assertNotNull(orders);
        assertEquals(1, orders.size());
    }
}