package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id
    private Long id;

    @Column(name = "order_date")
    LocalDateTime orderDate;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "order_items",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "item_id")})
    private List<Item> items;
}
