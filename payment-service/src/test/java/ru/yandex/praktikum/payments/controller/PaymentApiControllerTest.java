package ru.yandex.praktikum.payments.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentApiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentAccountService paymentAccountService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaymentApiController(paymentAccountService))
                .setControllerAdvice(new PaymentApiExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /getBalance")
    class GetBalance {

        @Test
        @DisplayName("returns balance from service")
        void returnsBalance() throws Exception {
            when(paymentAccountService.getBalance("user-1")).thenReturn(1500.5);

            mockMvc.perform(get("/getBalance")
                            .queryParam("userId", "user-1")
                            .queryParam("token", "any-token")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance", closeTo(1500.5, 0.001)));

            verify(paymentAccountService).getBalance("user-1");
        }
    }

    @Nested
    @DisplayName("POST /processPayment")
    class ProcessPayment {

        @Test
        @DisplayName("returns 204 when charge succeeds")
        void returnsNoContent() throws Exception {
            doNothing().when(paymentAccountService).charge(eq("user-1"), anyDouble());

            mockMvc.perform(post("/processPayment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"user-1\",\"orderSum\":99.99}"))
                    .andExpect(status().isNoContent());

            verify(paymentAccountService).charge("user-1", 99.99);
        }

        @Test
        @DisplayName("returns 400 when orderSum is not positive")
        void badRequestWhenOrderSumInvalid() throws Exception {
            mockMvc.perform(post("/processPayment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"user-1\",\"orderSum\":0}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("orderSum must be positive"));
        }

        @Test
        @DisplayName("returns 402 when balance is insufficient")
        void paymentRequiredWhenInsufficientBalance() throws Exception {
            doThrow(new InsufficientBalanceException("insufficient balance"))
                    .when(paymentAccountService).charge(eq("user-1"), eq(1_000.0));

            mockMvc.perform(post("/processPayment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"userId\":\"user-1\",\"orderSum\":1000}"))
                    .andExpect(status().isPaymentRequired())
                    .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_BALANCE"))
                    .andExpect(jsonPath("$.message").value("insufficient balance"));
        }
    }
}
