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

    public void initCart() {
        if (!redisTemplate.opsForValue().getOperations().hasKey(REDIS_CART_ID)) {
            log.info("Creating new cart");
            redisTemplate.opsForValue().set(
                    REDIS_CART_ID,
                    new Cart(new HashMap<>(), 0.0),
                    cartTtlMinutes,
                    TimeUnit.MINUTES
            );
        }
    }

    public Cart getCartFromRedis() {
        return redisTemplate.opsForValue().get(REDIS_CART_ID);
    }

    public void updateCartInRedis(Cart cart) {
        redisTemplate.opsForValue().set(REDIS_CART_ID, cart);
    }

    public void deleteCartFromRedis() {
        redisTemplate.delete(REDIS_CART_ID);
    }
}
