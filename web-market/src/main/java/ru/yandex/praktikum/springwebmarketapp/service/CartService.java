package ru.yandex.praktikum.springwebmarketapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.PaymentBalanceResponse;
import ru.yandex.praktikum.springwebmarketapp.repository.CartRedisRepository;
import ru.yandex.praktikum.springwebmarketapp.repository.ItemRepository;
import ru.yandex.praktikum.springwebmarketapp.util.ItemQuantityAction;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartService {
    public final ItemRepository itemRepository;
    private final WebClient webClient;
    private final CartRedisRepository cartRepository;
    private final SecurityService securityService;

    @Value("${mock.appl.userid}")
    private String mockUserId;

    @Value("${mock.appl.token}")
    private String mockToken;

    public Mono<Boolean> isPurchaseAvailable() {
        return securityService.getCurrentUserKey().flatMap(userId -> {
            Cart cart = cartRepository.getCartFromRedis(userId);

            return webClient.get()
                    .uri(String.format("/payment/v1/getBalance?userId=%s&token=%s", mockUserId, mockToken))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(PaymentBalanceResponse.class)
                    .flatMap(balance -> Mono.fromSupplier(() -> balance.getBalance() > cart.getTotalPrice()));
        });
    }

    public Mono<Void> changeItemQuantity(Long itemId,
                                         ItemQuantityAction action) {
        return securityService.getCurrentUserKey().flatMap(userId -> {
            cartRepository.initCart(userId);
            Cart cart = cartRepository.getCartFromRedis(userId);
            Map<Long, Item> cartItems = cart.getItems();

            if (cartItems.containsKey(itemId)) {
                Item item = cartItems.get(itemId);
                int itemQuantity = item.getCount();

                itemQuantity = switch (action) {
                    case PLUS -> itemQuantity + 1;
                    case MINUS -> itemQuantity >= 1 ? itemQuantity - 1 : 0;
                };

                item.setCount(itemQuantity);
                recalculateTotalPrice(cart);
                cartRepository.updateCartInRedis(cart, userId);
                return Mono.empty();
            }

            return itemRepository.findById(itemId)
                    .flatMap(item -> {
                        if (action.equals(ItemQuantityAction.PLUS)) {
                            item.setCount(1);
                            cartItems.put(itemId, item);
                        }
                        recalculateTotalPrice(cart);
                        cartRepository.updateCartInRedis(cart, userId);
                        return Mono.<Void>empty();
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("Item with id {} not found in database", itemId);
                        return Mono.empty();
                    }));
        });
    }

    private static void recalculateTotalPrice(Cart cart) {
        double total = cart.getItems().values().stream()
                .mapToDouble(i -> i.getPrice() * i.getCount())
                .sum();
        cart.setTotalPrice(total);
    }

    public Mono<Cart> getCart() {
        return securityService.getCurrentUserKey().flatMap(userId -> {
            cartRepository.initCart(userId);
            return Mono.fromSupplier(() -> cartRepository.getCartFromRedis(userId));
        });
    }

    public Mono<Void> deleteCartFromRedis() {
        return securityService.getCurrentUserKey().flatMap(userId -> {
            cartRepository.deleteCartFromRedis(userId);
            return Mono.empty();
        });
    }

    public Mono<Void> deleteCartFromRedis(String userId) {
        cartRepository.deleteCartFromRedis(userId);
        return Mono.empty();
    }
}
