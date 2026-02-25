package ru.yandex.praktikum.springwebmarketapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "items_images")
public class ItemImage {
    @Id
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_data")
    private byte[] fileData;
}
