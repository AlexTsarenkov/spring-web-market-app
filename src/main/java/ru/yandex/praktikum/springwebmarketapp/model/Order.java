package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Id
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_quantity")
    private Integer itemQuantity;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items;
}
