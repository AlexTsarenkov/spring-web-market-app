package ru.yandex.praktikum.springwebmarketapp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SessionService {
    public static final String SESSION_ITEM_QUANTITY = "ITEM_QUANTITY_MAP";
    public static final String SESSION_CART_ITEMS = "CART_ITEMS";

    public Mono<Void> changeItemQuantity(Long itemId,
                                         WebSession session,
                                         ItemQuantityAction action) {

        Map<Long, Integer> itemQuantityMap =
                Optional.ofNullable(session.<Map<Long, Integer>>getAttribute(SESSION_ITEM_QUANTITY))
                        .orElseGet(() -> {
                            Map<Long, Integer> map = new HashMap<>();
                            session.getAttributes().put(SESSION_ITEM_QUANTITY, map);
                            return map;
                        });
        if (itemQuantityMap.containsKey(itemId)) {
            int itemQuantity = itemQuantityMap.get(itemId);

            itemQuantity = switch (action) {
                case PLUS -> itemQuantity + 1;
                case MINUS -> itemQuantity >= 1 ? itemQuantity - 1 : 0;
            };

            itemQuantityMap.put(itemId, itemQuantity);

        } else {
            if (action.equals(ItemQuantityAction.PLUS)) {
                itemQuantityMap.put(itemId, 1);
            }
        }
        session.getAttributes().put(SESSION_ITEM_QUANTITY, itemQuantityMap);

        return Mono.empty();
    }

    public Mono<Optional<Map<Long, Integer>>> getItemQuantityMap(WebSession session) {
        return Mono.fromSupplier(() ->
                Optional.ofNullable(session.getAttribute(SESSION_ITEM_QUANTITY))
        );
    }

    public Mono<Void> clearItemQuantityMap(WebSession session) {
        session.getAttributes().remove(SESSION_ITEM_QUANTITY);
        session.getAttributes().remove(SESSION_CART_ITEMS);
        return Mono.empty();
    }

    public Mono<Void> saveCurrentCart(WebSession session, Cart cart) {
        session.getAttributes().put(SESSION_CART_ITEMS, cart);
        return Mono.empty();
    }

    public Mono<Optional<Cart>> getCartItems(WebSession session) {
        Cart cart = session.getAttribute(SESSION_CART_ITEMS);
        if (cart == null) {
            return Mono.fromSupplier(() -> Optional.empty());
        } else {
            return Mono.fromSupplier(() -> Optional.of(cart));
        }
    }
}
