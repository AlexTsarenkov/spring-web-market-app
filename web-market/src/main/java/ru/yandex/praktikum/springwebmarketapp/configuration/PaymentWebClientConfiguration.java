package ru.yandex.praktikum.springwebmarketapp.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PaymentWebClientConfiguration {

    @Bean
    public WebClient webClientConfiguration(
            @Value("${app.payment.base-url:http://localhost:8081}") String paymentBaseUrl) {
        return WebClient.create(paymentBaseUrl);
    }
}
