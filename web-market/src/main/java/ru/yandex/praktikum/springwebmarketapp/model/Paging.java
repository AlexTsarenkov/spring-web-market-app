package ru.yandex.praktikum.springwebmarketapp.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Paging {
    private final int pageSize;
    private final int pageNumber;
    private final boolean hasPrevious;
    private final boolean hasNext;
}
