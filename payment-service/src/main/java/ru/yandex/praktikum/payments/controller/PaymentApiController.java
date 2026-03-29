package ru.yandex.praktikum.payments.controller;

import com.payments.api.DefaultApi;
import com.payments.model.GetBalance200Response;
import com.payments.model.PaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.praktikum.payments.exception.UnauthorizedPaymentException;
import ru.yandex.praktikum.payments.service.PaymentAccountService;

@RestController
@RequiredArgsConstructor
public class PaymentApiController implements DefaultApi {

    private final PaymentAccountService paymentAccountService;

    @Override
    public ResponseEntity<GetBalance200Response> getBalance(String userId, String token) {
        if (!isTokenAccepted(token)) {
            throw new UnauthorizedPaymentException("Invalid or missing credentials");
        }
        double balance = paymentAccountService.getBalance(userId);
        return ResponseEntity.ok(new GetBalance200Response().balance(balance));
    }

    @Override
    public ResponseEntity<Void> processPayment(PaymentRequest paymentRequest) {
        if (paymentRequest.getOrderSum() == null || paymentRequest.getOrderSum() <= 0) {
            throw new IllegalArgumentException("orderSum must be positive");
        }
        paymentAccountService.charge(paymentRequest.getUserId(), paymentRequest.getOrderSum());
        return ResponseEntity.noContent().build();
    }

    private static boolean isTokenAccepted(String token) {
        return true;
    }
}
