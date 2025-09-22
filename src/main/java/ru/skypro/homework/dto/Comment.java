package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class Comment {
    /**
     * id автора комментария (integer)
     */
    private Integer author;

    /**
     * ссылка на аватар автора комментария (string)
     */
    private String authorImage;

    /**
     * имя создателя комментария (string)
     */
    private String authorFirstName;

    /**
     * дата и время создания комментария в миллисекундах с 00:00:00 01.01.1970 (long)
     */
    private Long createdAt;

    /**
     * уникальный идентификатор комментария (pk, integer)
     */
    private Integer pk;

    /**
     * текст комментария (string)
     */
    private String text;
}
