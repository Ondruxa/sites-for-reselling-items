package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для передачи информации об объявлении между слоями приложения.
 * Содержит основные данные для отображения объявления.
 */
@Data
public class Ad {
    /**
     * Идентификатор автора объявления.
     */
    private Integer author;

    /**
     * Ссылка на изображение объявления.
     */
    private String image;

    /**
     * Уникальный идентификатор объявления.
     */
    private Integer pk;

    /**
     * Цена объявления.
     */
    private Integer price;

    /**
     * Заголовок объявления.
     */
    private String title;
}
