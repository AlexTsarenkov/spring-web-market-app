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
import ru.yandex.praktikum.springwebmarketapp.exception.OrderCreationException;
import ru.yandex.praktikum.springwebmarketapp.service.CartService;
import ru.yandex.praktikum.springwebmarketapp.service.ItemService;
import ru.yandex.praktikum.springwebmarketapp.service.OrderItemService;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;

@AllArgsConstructor
@Controller
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final ItemService itemService;
    private final OrderItemService orderItemService;


    @GetMapping("/orders")
    public Mono<Rendering> getAllOrders() {
        return orderService.getAllOrders()
                .flatMap(order ->
                        orderItemService.getOrderItems((order.getId()))
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
                        orderItemService.getOrderItems((order.getId()))
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
        return orderService.createNewOrder()
                .switchIfEmpty(Mono.error(new Exception("order creation error")))
                .flatMap(order -> {
                    cartService.deleteCartFromRedis();
                    return Mono.fromSupplier(() -> Rendering.redirectTo("/orders/" + order.getId() + "?newOrder=true").build());
                })
                .onErrorResume(OrderCreationException.class, ex ->
                        Mono.fromSupplier(
                                () -> Rendering.view("error-page")
                                        .modelAttribute("code", "ORDER_CREATION_ERROR")
                                        .modelAttribute("message", ex.getMessage()).build()));
    }
}
