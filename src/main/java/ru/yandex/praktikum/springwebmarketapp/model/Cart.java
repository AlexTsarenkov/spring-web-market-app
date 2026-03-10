package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Cart {
    private final List<Item> items;
    private final Double totalPrice;
}
