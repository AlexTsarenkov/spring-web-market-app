package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
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

    @Getter(AccessLevel.NONE)
    private Double price;

    @Column(name = "img_path")
    @Getter(AccessLevel.NONE)
    private String imgPath;

    @Transient
    @Getter(AccessLevel.NONE)
    private Integer count;

    @OneToMany(mappedBy = "item")
    List<OrderItem> orderItems = new ArrayList<>();

    public Integer getCount() {
        if (count == null) {
            return 0;
        } else {
            return count;
        }
    }

    public Double getPrice() {
        if (price == null) {
            return 0.0;
        }
        return Math.round(price * 100.0) / 100.0;
    }

    public String getImgPath() {
        if (imgPath == null) {
            return "";
        }
        return imgPath;
    }
}
