package ru.yandex.praktikum.springwebmarketapp.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class OrderService {
    private final SessionService sessionService;
    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Optional<Order> createNewOrder(HttpServletRequest request) {
        Map<Long, Item> cartItems = sessionService.getItemQuantityMap(request).orElse(new HashMap<>());

        Order order = Order.builder()
                .orderDate(LocalDateTime.now())
                .totalSum(0.0)
                .orderItems(new ArrayList<>())
                .build();

        cartItems.values().stream()
                .filter(item -> item.getCount() > 0)
                .forEach(item -> {
                    order.setTotalSum(order.getTotalSum() + (item.getPrice() * item.getCount()));
                    order.addItem(item, item.getCount());
                });

        return Optional.ofNullable(orderRepository.save(order));
    }

    public Optional<Order> getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
