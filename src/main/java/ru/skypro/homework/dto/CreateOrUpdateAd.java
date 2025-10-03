package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для создания или обновления объявления.
 * Используется при отправке данных с фронта на сервер.
 */
@Data
public class CreateOrUpdateAd {
    /**
     * Заголовок объявления.
     */
    private String title;
    /**
     * Цена объявления.
     */
    private int price;
    /**
     * Описание объявления.
     */
    private String description;
}
