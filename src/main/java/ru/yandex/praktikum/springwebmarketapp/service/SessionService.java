package ru.yandex.praktikum.springwebmarketapp.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SessionService {
    public static final String SESSION_ITEM_QUANTITY = "ITEM_QUANTITY_MAP";

    public void changeItemQuantity(Item item,
                                   HttpServletRequest request,
                                   ItemQuantityAction action) {
        HttpSession session = request.getSession(true);
        Map<Long, Item> itemQuantityMap =
                (Map<Long, Item>) session.getAttribute(SESSION_ITEM_QUANTITY);
        if (itemQuantityMap == null) {
            itemQuantityMap = new HashMap<>();
        }
        if (itemQuantityMap.containsKey(item.getId())) {
            int itemQuantity = itemQuantityMap.get(item.getId()).getCount();

            itemQuantity = switch (action) {
                case PLUS -> itemQuantity + 1;
                case MINUS -> itemQuantity >= 1 ? itemQuantity - 1 : 0;
            };

            itemQuantityMap.get(item.getId()).setCount(itemQuantity);

        } else {
            if (action.equals(ItemQuantityAction.PLUS)) {
                item.setCount(item.getCount() + 1);
                itemQuantityMap.put(item.getId(), item);
            }
        }
        session.setAttribute(SESSION_ITEM_QUANTITY, itemQuantityMap);
    }

    public Optional<Map<Long, Item>> getItemQuantityMap(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return Optional.ofNullable((Map<Long, Item>) session.getAttribute(SESSION_ITEM_QUANTITY));
    }

    public void clearItemQuantityMap(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.removeAttribute(SESSION_ITEM_QUANTITY);
    }
}
