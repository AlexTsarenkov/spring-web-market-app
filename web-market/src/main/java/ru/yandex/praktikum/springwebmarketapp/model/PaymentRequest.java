package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentRequest {
    private String userId;
    private Double orderSum;
}
