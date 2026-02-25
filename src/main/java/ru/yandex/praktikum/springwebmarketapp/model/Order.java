package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date")
    LocalDateTime orderDate;

    @Transient
    @Getter(AccessLevel.NONE)
    private Double totalSum;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    @BatchSize(size = 100)
    private List<OrderItem> orderItems;

    @Transient
    private List<Item> items;

    public void addItem(Item item, Integer quantity) {
        OrderItem oItem = new OrderItem(this, item, quantity);
        orderItems.add(oItem);
    }

    public Double getTotalSum() {
        if (totalSum == null) {
            return 0.0;
        }
        return Math.round(totalSum * 100.0) / 100.0;
    }
}
