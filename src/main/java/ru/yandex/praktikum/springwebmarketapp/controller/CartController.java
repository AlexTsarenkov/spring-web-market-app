package ru.yandex.praktikum.springwebmarketapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.ItemModelAttribute;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final SessionService sessionService;
    private final ItemService itemService;


    @GetMapping("/items")
    public Mono<Rendering> getCartItems(WebSession session) {

        return sessionService.getItemQuantityMap(session)
                .map(opt -> opt.orElseGet(HashMap::new))
                .flatMap(cartItems -> {
                    Mono<List<Item>> itemsMono = itemService.findByIds(cartItems.keySet())
                            .collectList();

                    Mono<Double> totalPriceMono = itemsMono.map(items ->
                            items.stream()
                                    .mapToDouble(item ->
                                            item.getPrice() * cartItems.getOrDefault(item.getId(), 0))
                                    .sum()
                    );

                    return Mono.zip(itemsMono, totalPriceMono, (items, total) -> {
                                List<Item> resultList = items.stream().peek(item -> {
                                    item.setCount(cartItems.get(item.getId()));
                                }).filter(item -> item.getCount() > 0).toList();

                                sessionService.saveCurrentCart(session, new Cart(resultList, total));

                                return Rendering.view("cart")
                                        .modelAttribute("items", resultList)
                                        .modelAttribute("total", total)
                                        .build();
                            }
                    );
                });
    }

    @PostMapping("/items")
    public Mono<Rendering> changeCartItemQuantity(@ModelAttribute ItemModelAttribute request,
                                                  WebSession session) {
        return sessionService.changeItemQuantity(request.getId(), session,
                ItemQuantityAction.valueOf(request.getAction())).thenReturn(Rendering.redirectTo("/cart/items").build());
    }
}
