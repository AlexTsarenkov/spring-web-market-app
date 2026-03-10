package ru.yandex.praktikum.springwebmarketapp.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderItemRepository;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;

@AllArgsConstructor
@Controller
public class OrderController {
    private final OrderService orderService;
    private final SessionService sessionService;
    private final ItemService itemService;
    private final OrderItemRepository orderItemRepository;


    @GetMapping("/orders")
    public Mono<Rendering> getAllOrders() {
        return orderService.getAllOrders()
                .flatMap(order ->
                        orderItemRepository.findByOrderId((order.getId()))
                                .flatMap(oi ->
                                        itemService.findById(oi.getItemId().longValue())
                                                .map(item -> {
                                                    item.setCount(oi.getQuantity());
                                                    return item;
                                                })
                                )
                                .collectList()
                                .map(items -> {
                                    order.setItems(items);
                                    double total = items.stream()
                                            .mapToDouble(i -> i.getPrice() * i.getCount())
                                            .sum();
                                    order.setTotalSum(total);
                                    return order;
                                })
                ).collectList()
                .map(list -> Rendering.view("orders").modelAttribute("orders", list).build());
    }

    @GetMapping({"/orders/{id}"})
    public Mono<Rendering> getOrders(@PathVariable(name = "id") Long orderId,
                                     @RequestParam(name = "newOrder", required = false, defaultValue = "false")
                                     Boolean isNew) throws Exception {

        return orderService.getOrder(orderId)
                .flatMap(order ->
                        orderItemRepository.findByOrderId((order.getId()))
                                .flatMap(oi ->
                                        itemService.findById(oi.getItemId().longValue())
                                                .map(item -> {
                                                    item.setCount(oi.getQuantity());
                                                    return item;
                                                })
                                )
                                .collectList()
                                .map(items -> {
                                    order.setItems(items);
                                    double total = items.stream()
                                            .mapToDouble(i -> i.getPrice() * i.getCount())
                                            .sum();
                                    order.setTotalSum(total);
                                    return order;
                                })
                )
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .modelAttribute("newOrder", isNew)
                        .build());
    }

    @PostMapping("/buy")
    public Mono<Rendering> buyOrder(WebSession session) {
        return orderService.createNewOrder(session)
                .switchIfEmpty(Mono.error(new Exception("order creation error")))
                .flatMap(order -> sessionService.clearItemQuantityMap(session)
                        .thenReturn(Rendering.redirectTo("/orders/" + order.getId() + "?newOrder=true").build()));
    }
}
