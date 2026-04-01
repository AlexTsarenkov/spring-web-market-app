package ru.yandex.praktikum.springwebmarketapp.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.util.ItemQuantityAction;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(new CartController(cartService, itemService)).build();
    }

    @Test
    @DisplayName("POST /cart/items plus")
    void postCartItemsRedirects() {
        when(cartService.changeItemQuantity(eq(3L), eq(ItemQuantityAction.PLUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/cart/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "3").with("action", "PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart/items");

        verify(cartService).changeItemQuantity(eq(3L), eq(ItemQuantityAction.PLUS));
    }
}
