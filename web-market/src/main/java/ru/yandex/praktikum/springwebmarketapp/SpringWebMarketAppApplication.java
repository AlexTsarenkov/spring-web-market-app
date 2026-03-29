package ru.yandex.praktikum.springwebmarketapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringWebMarketAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringWebMarketAppApplication.class, args);
    }

}
