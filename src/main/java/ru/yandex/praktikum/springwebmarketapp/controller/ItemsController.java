package ru.yandex.praktikum.springwebmarketapp.controller;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.ItemModelAttribute;
import ru.yandex.praktikum.springwebmarketapp.model.Paging;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping({"/items", "/"})
public class ItemsController {
    private final ItemService itemService;
    private final SessionService sessionService;

    @GetMapping
    public Mono<Rendering> getItems(@RequestParam(name = "search", required = false) String searchString,
                                    @RequestParam(name = "sort", required = false, defaultValue = "NO") String sort,
                                    @RequestParam(name = "pageNumber", required = false, defaultValue = "1") int pageNum,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "5") int pageSize,
                                    WebSession session) {

        log.debug("Getting items for search string: {}", searchString);
        return Mono.zip(
                        itemService.findAll(searchString, sort, pageNum, pageSize),           // Mono<Page<Item>>
                        sessionService.getItemQuantityMap(session)                             // Mono<Optional<Map<Long, Item>>>
                                .map(opt -> opt.orElseGet(HashMap::new))
                )
                .map(tuple -> {
                    Page<Item> itemPage = tuple.getT1();
                    Map<Long, Integer> itemQuantityMap = tuple.getT2();

                    List<Item> items = itemPage.getContent();

                    //проставляем count из сессии
                    if (!itemQuantityMap.isEmpty()) {
                        items.forEach(item -> item.setCount(
                                itemQuantityMap.containsKey(item.getId())
                                        ? itemQuantityMap.get(item.getId())
                                        : 0));
                    }

                    return Rendering.view("items")
                            .modelAttribute("items",
                                    itemService.prepareDataForModel(items))
                            .modelAttribute("search", searchString)
                            .modelAttribute("sort", sort)
                            .modelAttribute("paging", Paging.builder()
                                    .pageSize(pageSize)
                                    .pageNumber(pageNum)
                                    .hasPrevious(!itemPage.isFirst())
                                    .hasNext(itemPage.hasNext())
                                    .build())
                            .build();
                });
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getItem(@PathVariable Long id, WebSession session) {
        return Mono.zip(sessionService.getItemQuantityMap(session), itemService.findById(id))
                .map(tuple -> {
                    Map<Long, Integer> itemQuantityMap = tuple.getT1().orElseGet(HashMap::new);
                    Item item = tuple.getT2();

                    if (itemQuantityMap.containsKey(id)) {
                        item.setCount(itemQuantityMap.get(id));
                    }

                    return Rendering.view("item")
                            .modelAttribute("item", item)
                            .build();
                });
    }

    @PostMapping("/{id}")
    public Mono<Rendering> updateItem(@ModelAttribute ItemModelAttribute request,
                                      WebSession session) {

        return sessionService.changeItemQuantity(request.getId(), session, ItemQuantityAction.valueOf(request.getAction()))
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

        return sessionService.changeItemQuantity(request.getId(), session, ItemQuantityAction.valueOf(request.getAction()))
                .thenReturn(Rendering.redirectTo(redirectUrl).build());
    }
}
