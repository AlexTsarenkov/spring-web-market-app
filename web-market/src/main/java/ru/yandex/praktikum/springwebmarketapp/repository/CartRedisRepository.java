package ru.yandex.praktikum.springwebmarketapp.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.praktikum.springwebmarketapp.model.Cart;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartRedisRepository {
    public static final String REDIS_CART_ID = "currentCart";
    private final RedisTemplate<String, Cart> redisTemplate;

    @Value("${cart.ttl.minutes}")
    private long cartTtlMinutes;

    public void initCart(String userId) {
        String cartId = getCartId(userId);
        if (!redisTemplate.opsForValue().getOperations().hasKey(cartId)) {
            log.info("Creating new cart");
            redisTemplate.opsForValue().set(
                    cartId,
                    new Cart(new HashMap<>(), 0.0),
                    cartTtlMinutes,
                    TimeUnit.MINUTES
            );
        }
    }

    public Cart getCartFromRedis(String userId) {
        return redisTemplate.opsForValue().get(getCartId(userId));
    }

    public void updateCartInRedis(Cart cart, String userId) {
        redisTemplate.opsForValue().set(getCartId(userId), cart);
    }

    public void deleteCartFromRedis(String userId) {
        redisTemplate.delete(getCartId(userId));
    }

    private String getCartId(String userId) {
        return REDIS_CART_ID + ":" + userId;
    }
}
