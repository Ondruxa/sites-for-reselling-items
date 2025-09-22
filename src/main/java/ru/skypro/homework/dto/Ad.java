package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class Ad {
    private Integer author;

    /**
     * Ссылка на изображение объявления (string)
     */
    private String image;

    /**
     * Уникальный идентификатор объявления (primary key, integer)
     */
    private Integer pk;

    /**
     * Цена объявления (integer)
     */
    private Integer price;

    /**
     * Заголовок объявления (string)
     */
    private String title;

}
