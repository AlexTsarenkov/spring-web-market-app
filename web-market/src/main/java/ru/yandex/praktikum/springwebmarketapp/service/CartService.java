package ru.yandex.praktikum.springwebmarketapp.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;
import ru.yandex.praktikum.springwebmarketapp.model.Item;
import ru.yandex.praktikum.springwebmarketapp.model.PaymentBalanceResponse;
import ru.yandex.praktikum.springwebmarketapp.repository.ItemRepository;
import ru.yandex.praktikum.springwebmarketapp.utill.ItemQuantityAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@AllArgsConstructor
public class CartService {
    public static final String REDIS_CART_ID = "currentCart";
    public final RedisTemplate<String, Cart> redisTemplate;
    public final ItemRepository itemRepository;
    private final WebClient webClient;

    public Mono<Boolean> isPurchaseAvailable() {
        Cart cart = getCartFromRedis();

        return webClient.get()
                .uri("/payment/v1/getBalance?userId=123&token=123")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(PaymentBalanceResponse.class)
                .flatMap(balance -> Mono.fromSupplier(() -> balance.getBalance() > cart.getTotalPrice()));
    }

    public Mono<Void> changeItemQuantity(Long itemId,
                                         ItemQuantityAction action) {
        initCart();

        Cart cart = getCartFromRedis();

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
            updateCartInRedis(cart);
            return Mono.empty();
        }

        return itemRepository.findById(itemId)
                .flatMap(item -> {
                    if (action.equals(ItemQuantityAction.PLUS)) {
                        item.setCount(1);
                        cartItems.put(itemId, item);
                    }
                    recalculateTotalPrice(cart);
                    updateCartInRedis(cart);
                    return Mono.<Void>empty();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Item with id {} not found in database", itemId);
                    return Mono.empty();
                }));
    }

    private static void recalculateTotalPrice(Cart cart) {
        double total = cart.getItems().values().stream()
                .mapToDouble(i -> i.getPrice() * i.getCount())
                .sum();
        cart.setTotalPrice(total);
    }

    public Mono<Cart> getCart() {
        initCart();
        return Mono.fromSupplier(() -> getCartFromRedis());
    }


    private void initCart() {
        if (!redisTemplate.opsForValue().getOperations().hasKey(REDIS_CART_ID)) {
            log.info("Creating new cart");
            redisTemplate.opsForValue().set(
                    REDIS_CART_ID,
                    new Cart(new HashMap<>(), 0.0),
                    15,
                    TimeUnit.MINUTES
            );
        }
    }

    private Cart getCartFromRedis() {
        return redisTemplate.opsForValue().get(REDIS_CART_ID);
    }

    private void updateCartInRedis(Cart cart) {
        redisTemplate.opsForValue().set(REDIS_CART_ID, cart);
    }

    public void deleteCartFromRedis() {
        redisTemplate.delete(REDIS_CART_ID);
    }
}
