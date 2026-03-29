package ru.yandex.praktikum.springwebmarketapp.exception;

public class OrderCreationException extends RuntimeException {
    public OrderCreationException(Throwable cause) {
        super(cause);
    }
}
