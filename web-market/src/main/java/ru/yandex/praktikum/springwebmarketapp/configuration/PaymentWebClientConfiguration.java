package ru.yandex.praktikum.springwebmarketapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfiguration {
    @Bean
    public WebClient WebClientConfiguration() {
        return WebClient.create("http://localhost:8081");
    }
}
