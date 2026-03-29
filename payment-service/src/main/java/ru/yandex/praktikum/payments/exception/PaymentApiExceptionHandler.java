package ru.yandex.praktikum.payments.exception;

import com.payments.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentApiExceptionHandler {

    @ExceptionHandler(UnauthorizedPaymentException.class)
    public ResponseEntity<ErrorResponse> unauthorized(UnauthorizedPaymentException ex) {
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> insufficientBalance(InsufficientBalanceException ex) {
        return error(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse().errorCode(code).message(message));
    }
}
