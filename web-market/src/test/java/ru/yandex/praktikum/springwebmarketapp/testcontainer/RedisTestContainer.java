package ru.yandex.praktikum.springwebmarketapp.testcontainer;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public final class RedisTestContainer {

    @Container
    public static final GenericContainer<?> REDIS =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);

    private RedisTestContainer() {
    }
}
