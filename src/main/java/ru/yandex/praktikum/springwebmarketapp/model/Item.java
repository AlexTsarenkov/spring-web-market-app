package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private Double price;

    @Transient
    private String imagePath;

    @Transient
    @Getter(AccessLevel.NONE)
    private Integer count;

    @ManyToMany(mappedBy = "items")
    List<Order> orders;

    public Integer getCount() {
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }
}
