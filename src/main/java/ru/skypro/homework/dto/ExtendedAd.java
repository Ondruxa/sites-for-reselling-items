package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для расширенного представления объявления.
 * Содержит подробную информацию для детального отображения объявления.
 */
@Data
public class ExtendedAd {
    /**
     * Идентификатор объявления.
     */
    private Integer pk;
    /**
     * Имя автора объявления.
     */
    private String authorFirstName;
    /**
     * Фамилия автора объявления.
     */
    private String authorLastName;
    /**
     * Описание объявления.
     */
    private String description;
    /**
     * Email автора объявления.
     */
    private String email;
    /**
     * Ссылка на изображение объявления.
     */
    private String image;
    /**
     * Телефон автора объявления.
     */
    private String phone;
    /**
     * Цена объявления.
     */
    private Integer price;
    /**
     * Заголовок объявления.
     */
    private String title;
}
