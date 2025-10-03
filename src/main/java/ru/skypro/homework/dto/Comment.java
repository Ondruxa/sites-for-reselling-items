package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для передачи информации о комментарии к объявлению.
 * Используется для отображения комментариев.
 */
@Data
public class Comment {
    /**
     * Идентификатор автора комментария.
     */
    private Integer author;

    /**
     * Ссылка на аватар автора комментария.
     */
    private String authorImage;

    /**
     * Имя автора комментария.
     */
    private String authorFirstName;

    /**
     * Дата и время создания комментария (в миллисекундах с 01.01.1970).
     */
    private Long createdAt;

    /**
     * Уникальный идентификатор комментария.
     */
    private Integer pk;

    /**
     * Текст комментария.
     */
    private String text;
}
