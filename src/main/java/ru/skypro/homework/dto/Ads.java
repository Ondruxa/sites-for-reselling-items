package ru.skypro.homework.dto;

import lombok.Data;

import java.util.List;

@Data
public class Ads {
    /**
     * Общее количество объявлений (integer)
     */
    private Integer count;

    /**
     * Список объявлений (array of Ad objects)
     */
    private List<Ad> results;
}
