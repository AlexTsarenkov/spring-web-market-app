package ru.yandex.praktikum.springwebmarketapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.ItemModelAttribute;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.util.ItemQuantityAction;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final ItemService itemService;


    @GetMapping("/items")
    public Mono<Rendering> getCartItems() {
        return cartService.isPurchaseAvailable().flatMap(isAvailable -> {
            return cartService.getCart()
                    .flatMap(cart -> {
                        return Mono.fromSupplier(() -> Rendering.view("cart")
                                .modelAttribute("items", cart.getItems().values())
                                .modelAttribute("total", cart.getTotalPrice())
                                .modelAttribute("isBuyDisabled", !isAvailable)
                                .build());
                    });

        });
    }

    @PostMapping("/items")
    public Mono<Rendering> changeCartItemQuantity(@ModelAttribute ItemModelAttribute request) {
        return cartService.changeItemQuantity(request.getId(),
                ItemQuantityAction.valueOf(request.getAction())).thenReturn(Rendering.redirectTo("/cart/items").build());
    }
}
