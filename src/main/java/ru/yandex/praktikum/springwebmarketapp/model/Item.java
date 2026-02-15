package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;

    @OneToOne
    @JoinColumn(name = "itemId", nullable = false)
    ItemImage image;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id", referencedColumnName = "item_id")
    private Order order;
}
