package ru.yandex.praktikum.springwebmarketapp;

import okhttp3.mockwebserver.MockWebServer;
import org.springframework.test.context.DynamicPropertyRegistry;
import ru.yandex.praktikum.springwebmarketapp.testcontainer.RedisTestContainer;

public final class TestDynamicProperties {

    private TestDynamicProperties() {
    }

    public static void registerRedis(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", RedisTestContainer.REDIS::getHost);
        registry.add("spring.data.redis.port", () -> String.valueOf(RedisTestContainer.REDIS.getMappedPort(6379)));
    }

    public static void registerPaymentBaseUrl(DynamicPropertyRegistry registry, MockWebServer paymentServer) {
        registry.add("app.payment.base-url", () -> "http://localhost:" + paymentServer.getPort());
    }
}
