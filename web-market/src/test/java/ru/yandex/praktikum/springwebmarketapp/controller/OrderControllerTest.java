package ru.yandex.praktikum.springwebmarketapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.exception.OrderCreationException;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.OrderItemService;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private OrderService orderService;

    @Mock
    private OrderItemService orderItemService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(
                new OrderController(orderService, cartService, itemService, orderItemService)).build();
    }

    @Test
    @DisplayName("POST /buy - success")
    void buyRedirectsAfterSuccess() {
        Order created = Order.builder().id(42L).orderDate(LocalDateTime.now()).totalSum(10.0).build();
        when(orderService.createNewOrder()).thenReturn(Mono.just(created));
        when(cartService.deleteCartFromRedis()).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc ->
                        assertThat(loc).contains("/orders/42", "newOrder=true"));

        verify(cartService).deleteCartFromRedis();
    }

    @Test
    @DisplayName("POST /buy - payment ex")
    void buyOnOrderCreationExceptionWithoutViewResolverIsServerError() {
        when(orderService.createNewOrder()).thenReturn(Mono.error(
                new OrderCreationException(new RuntimeException("payment failed"))));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
