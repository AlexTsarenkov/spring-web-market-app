package ru.yandex.praktikum.payments.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.praktikum.payments.exception.InsufficientBalanceException;
import ru.yandex.praktikum.payments.exception.PaymentApiExceptionHandler;
import ru.yandex.praktikum.payments.service.PaymentAccountService;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentApiControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private PaymentAccountService paymentAccountService;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient
                .bindToController(new PaymentApiController(paymentAccountService))
                .controllerAdvice(new PaymentApiExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /getBalance")
    class GetBalance {

        @Test
        @DisplayName("returns balance from service")
        void returnsBalance() {
            when(paymentAccountService.getBalance("user-1")).thenReturn(1500.5);

            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/getBalance")
                            .queryParam("userId", "user-1")
                            .queryParam("token", "any-token")
                            .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.balance").value(closeTo(1500.5, 0.001));

            verify(paymentAccountService).getBalance("user-1");
        }
    }

    @Nested
    @DisplayName("POST /processPayment")
    class ProcessPayment {

        @Test
        @DisplayName("returns 204 when charge succeeds")
        void returnsNoContent() {
            doNothing().when(paymentAccountService).charge(eq("user-1"), anyDouble());

            webTestClient.post()
                    .uri("/processPayment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"userId\":\"user-1\",\"orderSum\":99.99}")
                    .exchange()
                    .expectStatus().isNoContent();

            verify(paymentAccountService).charge("user-1", 99.99);
        }

        @Test
        @DisplayName("returns 400 when orderSum is not positive")
        void badRequestWhenOrderSumInvalid() {
            webTestClient.post()
                    .uri("/processPayment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"userId\":\"user-1\",\"orderSum\":0}")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errorCode").isEqualTo("INVALID_REQUEST")
                    .jsonPath("$.message").isEqualTo("orderSum must be positive");
        }

        @Test
        @DisplayName("returns 402 when balance is insufficient")
        void paymentRequiredWhenInsufficientBalance() {
            doThrow(new InsufficientBalanceException("insufficient balance"))
                    .when(paymentAccountService).charge(eq("user-1"), eq(1_000.0));

            webTestClient.post()
                    .uri("/processPayment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{\"userId\":\"user-1\",\"orderSum\":1000}")
                    .exchange()
                    .expectStatus().isEqualTo(402)
                    .expectBody()
                    .jsonPath("$.errorCode").isEqualTo("INSUFFICIENT_BALANCE")
                    .jsonPath("$.message").isEqualTo("insufficient balance");
        }
    }
}
