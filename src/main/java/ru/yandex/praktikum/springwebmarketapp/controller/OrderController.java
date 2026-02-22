package ru.yandex.praktikum.springwebmarketapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.Order;
import ru.yandex.praktikum.springwebmarketapp.service.OrderService;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;

import java.util.List;

@AllArgsConstructor
@Controller
public class OrderController {
    private final OrderService orderService;
    private final SessionService sessionService;

    @GetMapping("/orders")
    public ModelAndView getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        orders.stream()
                .forEach(order -> {
                    order.setItems(order.getOrderItems().stream()
                            .map(oi -> {
                                Item item = oi.getItem();
                                item.setCount(oi.getQuantity());
                                order.setTotalSum(order.getTotalSum() + (item.getCount() * item.getPrice()));
                                return item;
                            })
                            .toList());
                });
        ModelAndView mv = new ModelAndView("orders");
        mv.addObject("orders", orders);
        return mv;
    }

    @GetMapping({"/orders/{id}"})
    public ModelAndView getOrders(@PathVariable(name = "id") Long orderId,
                                  @RequestParam(name = "newOrder", required = false, defaultValue = "false")
                                  Boolean isNew,
                                  HttpServletRequest request) throws Exception {

        Order order = orderService.getOrder(orderId).orElseThrow(() -> new Exception("Order not found"));
        ModelAndView mv = new ModelAndView("order");
        List<Item> orderItems = order.getOrderItems().stream()
                .map(oi -> {
                    Item item = oi.getItem();
                    item.setCount(oi.getQuantity());
                    return item;
                })
                .peek(item -> order.setTotalSum(order.getTotalSum() + (item.getPrice() * item.getCount())))
                .toList();
        order.setItems(orderItems);
        //mv.addObject("items", orderItems);
        mv.addObject("newOrder", isNew);
        mv.addObject("order", order);
        return mv;
    }

    @PostMapping("/buy")
    private RedirectView buyOrder(HttpServletRequest request) throws Exception {
        Order order = orderService.createNewOrder(request).orElseThrow(() -> new Exception("order creation error"));

        if (order.getId() != null) {
            sessionService.clearItemQuantityMap(request);
        }
        return new RedirectView(String.format("/orders/%d?newOrder=true", order.getId()));
    }
}
