package ru.yandex.praktikum.payments.controller;

import com.payments.api.DefaultApi;
import com.payments.model.GetBalance200Response;
import com.payments.model.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.praktikum.payments.exception.UnauthorizedPaymentException;
import ru.yandex.praktikum.payments.service.PaymentAccountService;

@RestController
@RequiredArgsConstructor
public class PaymentApiController implements DefaultApi {

    private final PaymentAccountService paymentAccountService;

    @Override
    public Mono<ResponseEntity<GetBalance200Response>> getBalance(String userId, String token,
                                                                    ServerWebExchange exchange) {
        if (!isTokenAccepted(token)) {
            return Mono.error(new UnauthorizedPaymentException("Invalid or missing credentials"));
        }
        return Mono.fromCallable(() -> paymentAccountService.getBalance(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .map(balance -> ResponseEntity.ok(new GetBalance200Response().balance(balance)));
    }

    @Override
    public Mono<ResponseEntity<Void>> processPayment(Mono<PaymentRequest> paymentRequest,
                                                     ServerWebExchange exchange) {
        return paymentRequest.flatMap(req -> {
            if (req.getOrderSum() == null || req.getOrderSum() <= 0) {
                return Mono.error(new IllegalArgumentException("orderSum must be positive"));
            }
            return Mono.fromRunnable(() -> paymentAccountService.charge(req.getUserId(), req.getOrderSum()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .thenReturn(ResponseEntity.noContent().build());
        });
    }

    private static boolean isTokenAccepted(String token) {
        return true;
    }
}
