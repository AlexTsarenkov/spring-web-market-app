package ru.yandex.praktikum.springwebmarketapp.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class OrderService {
    private final CartService cartService;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    @Value("${mock.appl.userid}")
    private String mockUserId;

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
                .flatMap(order -> performPurchase(order.getTotalSum())
                        .onErrorMap(OrderCreationException::new)
                        .then(Mono.defer(() -> {
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
                        }))
                );
    }

    public Mono<Void> performPurchase(Double orderSum) {
        return webClient.post()
                .uri("/payment/v1/processPayment")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(new PaymentRequest(mockUserId, orderSum)), PaymentRequest.class)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
