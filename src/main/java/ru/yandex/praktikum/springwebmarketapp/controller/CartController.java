package ru.yandex.praktikum.springwebmarketapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import ru.yandex.praktikum.springwebmarketapp.model.Item;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @GetMapping("/items")
    public ModelAndView getCartItems(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Map<Long, Item> cartItems = (Map<Long, Item>) session.getAttribute("ITEM_QUANTITY_MAP");

        //so i can use it in closure
        Item priceItem = Item.builder().price(0.0).build();
        List<Item> items = cartItems.values().stream()
                .filter(item -> item.getCount() > 0)
                .peek(item -> {
                    priceItem.setPrice(priceItem.getPrice() + (item.getPrice() * item.getCount()));
                })
                .toList();

        ModelAndView mv = new ModelAndView("cart");
        mv.addObject("items", items);
        mv.addObject("total", priceItem.getPrice());

        return mv;
    }
}
