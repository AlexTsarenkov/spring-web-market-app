package ru.yandex.praktikum.springwebmarketapp.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.model.OrderItem;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderItemRepository;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {
    private final SessionService sessionService;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;

    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Mono<Order> createNewOrder(WebSession session) {
        return sessionService.getCartItems(session)
                .map(opt -> opt.orElseGet(() -> new Cart(List.of(), 0.0)))
                .map(cart -> Order.builder()
                        .orderDate(LocalDateTime.now())
                        .totalSum(cart.getTotalPrice())
                        .items(cart.getItems())
                        .build())
                .flatMap(order -> {
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
                });
    }

    public Mono<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
