package ru.yandex.praktikum.springwebmarketapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.service.SessionService;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final SessionService sessionService;
    private List<Item> currentCartItems;

    @GetMapping("/items")
    public ModelAndView getCartItems(HttpServletRequest request) {
        Map<Long, Item> cartItems = sessionService.getItemQuantityMap(request).orElse(new HashMap<Long,Item>());

        //to use it in closure
        Item priceItem = Item.builder().price(0.0).build();
        List<Item> items = cartItems.values().stream()
                .filter(item -> item.getCount() > 0)
                .peek(item -> {
                    priceItem.setPrice(priceItem.getPrice() + (item.getPrice() * item.getCount()));
                })
                .toList();

        currentCartItems = items;

        ModelAndView mv = new ModelAndView("cart");
        mv.addObject("items", items);
        mv.addObject("total", priceItem.getPrice());

        return mv;
    }

    @PostMapping("/items")
    public RedirectView changeCartItemQuantity(@RequestParam(name = "id") Long itemId,
                                               @RequestParam(name = "action") String action,
                                               HttpServletRequest request) {
        sessionService.changeItemQuantity(Item.builder().id(itemId).build(), request,
                ItemQuantityAction.valueOf(action));

        return new RedirectView("/cart/items");
    }
}
