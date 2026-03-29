package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentBalanceResponse {
    private Double balance;
}
