package ru.yandex.praktikum.payments.exception;

import com.payments.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class PaymentApiExceptionHandler {

    @ExceptionHandler(UnauthorizedPaymentException.class)
    public Mono<ResponseEntity<ErrorResponse>> unauthorized(UnauthorizedPaymentException ex) {
        return Mono.just(error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Unauthorized access"));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<ErrorResponse>> insufficientBalance(InsufficientBalanceException ex) {
        return Mono.just(error(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE", "Insufficient balance for purchase"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> badRequest(IllegalArgumentException ex) {
        return Mono.just(error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request"));
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse().errorCode(code).message(message));
    }
}
