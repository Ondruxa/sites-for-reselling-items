package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class ExtendedAd {
    /**
     * ID объявления (integer)
     */
    private Integer pk;

    /**
     * Имя автора объявления (string)
     */
    private String authorFirstName;

    /**
     * Фамилия автора объявления (string)
     */
    private String authorLastName;

    /**
     * Описание объявления (string)
     */
    private String description;

    /**
     * Электронная почта автора объявления (string)
     */
    private String email;

    /**
     * Ссылка на изображение объявления (string)
     */
    private String image;

    /**
     * Номер телефона автора объявления (string)
     */
    private String phone;

    /**
     * Цена объявления (integer)
     */
    private Integer price;

    /**
     * Заголовок объявления (string)
     */
    private String title;
}
