package ru.yandex.praktikum.springwebmarketapp.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.ItemModelAttribute;
import ru.yandex.praktikum.springwebmarketapp.model.Paging;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.SecurityService;
import ru.yandex.praktikum.springwebmarketapp.util.ItemQuantityAction;

import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping({"/items"})
public class ItemsController {
    private final ItemService itemService;
    private final CartService cartService;
    private final SecurityService securityService;

    @GetMapping
    public Mono<Rendering> getItems(@RequestParam(name = "search", required = false) String searchString,
                                    @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                    @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize
    ) {

        log.debug("Getting items for search string: {}", searchString);


        return securityService.getCurrentUserKey().flatMap(user -> {
            return Mono.zip(
                            itemService.findAll(searchString, sort, pageNum, pageSize),           // Mono<Page<Item>>
                            cartService.getCart()
                    )
                    .map(tuple -> {
                        Page<Item> itemPage = tuple.getT1();
                        Map<Long, Item> cartItems = tuple.getT2().getItems();

                        List<Item> items = itemPage.getContent();

                        //проставляем count из сессии
                        if (!cartItems.isEmpty()) {
                            items.forEach(item -> item.setCount(
                                    cartItems.containsKey(item.getId())
                                            ? cartItems.get(item.getId()).getCount()
                                            : 0));
                        }

                        return Rendering.view("items")
                                .modelAttribute("items",
                                        itemService.groupItemsByRows(items))
                                .modelAttribute("search", searchString)
                                .modelAttribute("sort", sort)
                                .modelAttribute("isAnonUser", user.equals(SecurityService.ANONYMOUS_USER_KEY))
                                .modelAttribute("paging", Paging.builder()
                                        .pageSize(pageSize)
                                        .pageNumber(pageNum)
                                        .hasPrevious(!itemPage.isFirst())
                                        .hasNext(itemPage.hasNext())
                                        .build())
                                .build();
                    });
        });
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getItem(@PathVariable Long id, WebSession session) {
        return securityService.getCurrentUserKey().flatMap(user -> {
            return Mono.zip(cartService.getCart(), itemService.findById(id))
                    .map(tuple -> {
                        Map<Long, Item> cartItems = tuple.getT1().getItems();
                        Item item = tuple.getT2();

                        if (cartItems.containsKey(id)) {
                            item.setCount(cartItems.get(id).getCount());
                        }

                        return Rendering.view("item")
                                .modelAttribute("item", item)
                                .modelAttribute("isAnonUser", user.equals(SecurityService.ANONYMOUS_USER_KEY))
                                .build();
                    });
        });
    }

    @PostMapping("/{id}")
    public Mono<Rendering> updateItem(@ModelAttribute ItemModelAttribute request,
                                      WebSession session) {

        return cartService.changeItemQuantity(request.getId(), ItemQuantityAction.valueOf(request.getAction()))
                .thenReturn(Rendering.redirectTo("/items/" + request.getId()).build());
    }

    @PostMapping
    public Mono<Rendering> changeItemQuantity(@ModelAttribute ItemModelAttribute request,
                                              WebSession session) {

        String redirectUrl = UriComponentsBuilder.fromPath("/items")
                .queryParamIfPresent("search",
                        (request.getSearch().isBlank()) ? java.util.Optional.empty() :
                                java.util.Optional.of(request.getSearch()))
                .queryParam("sort", request.getSort())
                .queryParam("pageNumber", request.getPageNumber())
                .queryParam("pageSize", request.getPageSize())
                .build()
                .encode()          // важно
                .toUriString();

        return cartService.changeItemQuantity(request.getId(), ItemQuantityAction.valueOf(request.getAction()))
                .thenReturn(Rendering.redirectTo(redirectUrl).build());
    }
}
