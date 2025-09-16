package ru.skypro.homework.dto;

import lombok.Data;

import java.util.List;

@Data
public class Comments {
    /**
     * Общее количество комментариев (integer)
     */
    private Integer count;

    /**
     * Массив комментариев (List of CommentDto)
     */
    private List<Comment> results;
}
