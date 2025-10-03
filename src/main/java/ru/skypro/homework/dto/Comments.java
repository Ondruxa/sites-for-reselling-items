package ru.skypro.homework.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO для передачи списка комментариев и их количества.
 * Используется для отображения коллекции комментариев на фронте.
 */
@Data
public class Comments {
    /**
     * Общее количество комментариев.
     */
    private Integer count;

    /**
     * Список комментариев.
     */
    private List<Comment> results;
}
