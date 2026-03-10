package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    private Long id;

    @Column("order_date")
    LocalDateTime orderDate;

    @Transient
    @Getter(AccessLevel.NONE)
    private Double totalSum;

    @Transient
    private List<Item> items;


    public Double getTotalSum() {
        if (totalSum == null) {
            return 0.0;
        }
        return Math.round(totalSum * 100.0) / 100.0;
    }
}
