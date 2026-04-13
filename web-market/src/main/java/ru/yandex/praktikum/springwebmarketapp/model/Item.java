package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
public class Item {
    @Id
    private Long id;

    private String title;
    private String description;

    @Getter(AccessLevel.NONE)
    private Double price;

    @Column("img_path")
    @Getter(AccessLevel.NONE)
    private String imgPath;

    @Transient
    @Getter(AccessLevel.NONE)
    private Integer count;

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
