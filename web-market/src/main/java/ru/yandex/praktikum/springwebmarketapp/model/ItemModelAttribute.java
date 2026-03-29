package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class ItemModelAttribute {
    private Long id;

    @Getter(AccessLevel.NONE)
    private String search;

    private String sort;
    private int pageNumber;
    private int pageSize;
    private String action;

    public String getSearch() {
        if (search == null) {
            return "";
        } else
            return search;
    }

}
