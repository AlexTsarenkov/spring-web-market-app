package ru.yandex.praktikum.springwebmarketapp.service;


import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.exception.OrderCreationException;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.model.OrderItem;
import ru.yandex.praktikum.springwebmarketapp.model.PaymentRequest;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderItemRepository;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Mono<Order> createNewOrder() {
        return cartService.getCart()
                .map(cart -> Order.builder()
                        .orderDate(LocalDateTime.now())
                        .totalSum(cart.getTotalPrice())
                        .items(cart.getItems().values().stream().toList())
                        .build())
                .flatMap(order -> {
                    return performPurchase(order.getTotalSum())
                            .flatMap(voidResult -> {
                                Mono<Order> createdOrder = orderRepository.save(order);

                                return createdOrder.flatMap(crOrder -> {
                                    List<OrderItem> orderItems = order.getItems().stream()
                                            .map(item -> OrderItem.builder()
                                                    .itemId(item.getId())
                                                    .orderId(crOrder.getId())
                                                    .quantity(item.getCount())
                                                    .build())
                                            .toList();

                                    return orderItemRepository.saveAll(orderItems)
                                            .collectList().thenReturn(crOrder);
                                });
                            }).doOnError(throwable -> {
                                throw new OrderCreationException(throwable);
                            });
                });
    }

    public Mono<Void> performPurchase(Double orderSum) {
        return webClient.post()
                .uri("/payment/v1/processPayment")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(new PaymentRequest("123", orderSum)), PaymentRequest.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
