package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Cart {
    private final Map<Long, Item> items;
    private Double totalPrice;
}
