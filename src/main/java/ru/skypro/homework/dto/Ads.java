package ru.skypro.homework.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO для передачи списка объявлений и их количества.
 * Используется для отображения коллекции объявлений.
 */
@Data
public class Ads {
    /**
     * Общее количество объявлений.
     */
    private Integer count;

    /**
     * Список объявлений.
     */
    private List<Ad> results;
}
