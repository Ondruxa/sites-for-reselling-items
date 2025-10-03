package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для создания или обновления комментария.
 * Используется при отправке текста комментария с фронта на сервер.
 */
@Data
public class CreateOrUpdateComment {
    /**
     * Текст комментария.
     */
    private String text;
}
