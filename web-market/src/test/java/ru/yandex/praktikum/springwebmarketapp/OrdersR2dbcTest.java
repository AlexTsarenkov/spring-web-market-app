package ru.yandex.praktikum.springwebmarketapp;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.model.OrderItem;
import ru.yandex.praktikum.springwebmarketapp.repository.CartRedisRepository;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderItemRepository;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.PostgreSQLTestContainer;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.RedisTestContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = SpringWebMarketAppApplication.class)
@Testcontainers
@ImportTestcontainers({PostgreSQLTestContainer.class, RedisTestContainer.class})
class OrdersR2dbcTest {

    private static final MockWebServer PAYMENT_SERVER;

    static {
        try {
            PAYMENT_SERVER = new MockWebServer();
            PAYMENT_SERVER.start();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        TestDynamicProperties.registerRedis(registry);
        TestDynamicProperties.registerPaymentBaseUrl(registry, PAYMENT_SERVER);
    }

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    RedisTemplate<String, Cart> redisTemplate;

    @BeforeEach
    void initDb() throws IOException {
        String schemaSql = readClasspathSql("schema.sql");
        String dataSql = readClasspathSql("ItemsInitScript.sql");

        databaseClient.sql(schemaSql).then().block();
        databaseClient.sql(dataSql).then().block();

        redisTemplate.delete(CartRedisRepository.REDIS_CART_ID);

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

        double totalPrice = 5 * 100.00 + 4 * 250.00;
        Map<Long, Item> cartItems = new HashMap<>();
        cartItems.put(1L, item1);
        cartItems.put(2L, item2);
        Cart cart = new Cart(cartItems, totalPrice);
        redisTemplate.opsForValue().set(CartRedisRepository.REDIS_CART_ID, cart);

        PAYMENT_SERVER.enqueue(new MockResponse().setResponseCode(204));
    }

    @Test
    void createOrderReactiveTest() {
        Order order = orderService.createNewOrder().block();
        assertNotNull(order);
        assertNotNull(order.getId());

        List<OrderItem> orderItems = orderItemRepository
                .findByOrderId(order.getId())
                .collectList()
                .block();

        assertNotNull(orderItems);
        assertEquals(2, orderItems.size());
        Map<Long, Integer> qtyByItemId = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getItemId, OrderItem::getQuantity));
        assertEquals(5, qtyByItemId.get(1L));
        assertEquals(4, qtyByItemId.get(2L));

        Order dbOrder = orderService.getOrder(order.getId()).block();
        assertNotNull(dbOrder);
        assertEquals(order.getId(), dbOrder.getId());

        List<Order> orders = orderService.getAllOrders()
                .collectList()
                .block();
        assertNotNull(orders);
        assertEquals(1, orders.size());
    }

    private String readClasspathSql(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
