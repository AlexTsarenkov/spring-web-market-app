package ru.yandex.praktikum.payments.service;

import org.springframework.stereotype.Service;
import ru.yandex.praktikum.payments.exception.InsufficientBalanceException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentAccountService {

    private final Map<String, Double> balances = new ConcurrentHashMap<>();

    public double getBalance(String userId) {
        return 300_000.0;
    }

    public void charge(String userId, double amount) {
        if (!balances.containsKey(userId)) {
            balances.put(userId, 250_000.0);
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }

        if (amount > balances.get(userId)) {
            throw new InsufficientBalanceException("insufficient balance");
        }

    }
}
