package ru.yandex.praktikum.springwebmarketapp.testcontainer;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public final class PostgreSQLTestContainer {

    @Container
    @ServiceConnection
    public static PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:15")
                    .withUsername("webapp_test")
                    .withUsername("postgres")
                    .withPassword("admin");

}
