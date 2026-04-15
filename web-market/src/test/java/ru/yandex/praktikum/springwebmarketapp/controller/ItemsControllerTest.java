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
import ru.yandex.praktikum.springwebmarketapp.service.SecurityService;
import ru.yandex.praktikum.springwebmarketapp.util.ItemQuantityAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ItemsControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private ItemService itemService;

    @Mock
    private CartService cartService;

    @Mock
    private SecurityService securityService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(
                new ItemsController(itemService, cartService, securityService)).build();
    }

    @Test
    @DisplayName("POST /items plus")
    void postItemByIdRedirects() {
        when(cartService.changeItemQuantity(eq(5L), eq(ItemQuantityAction.PLUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items/5")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "5").with("action", "PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(cartService).changeItemQuantity(eq(5L), eq(ItemQuantityAction.PLUS));
    }

    @Test
    @DisplayName("POST /items with query")
    void postItemsRedirectsWithParams() {
        when(cartService.changeItemQuantity(eq(1L), eq(ItemQuantityAction.MINUS)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "1")
                        .with("action", "MINUS")
                        .with("search", "abc")
                        .with("sort", "ALPHA")
                        .with("pageNumber", "2")
                        .with("pageSize", "10"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().value("Location", loc ->
                        assertThat(loc).contains("/items", "search=abc", "sort=ALPHA", "pageNumber=2", "pageSize=10"));
    }
}
