package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для передачи информации о пользователе.
 * Используется для отображения профиля пользователя на фронте.
 */
@Data
public class User {
    /**
     * Идентификатор пользователя.
     */
    private Integer id;
    /**
     * Email пользователя (логин).
     */
    private String email;
    /**
     * Имя пользователя.
     */
    private String firstName;
    /**
     * Фамилия пользователя.
     */
    private String lastName;
    /**
     * Телефон пользователя.
     */
    private String phone;
    /**
     * Роль пользователя (USER | ADMIN).
     */
    private String role;
    /**
     * Ссылка на аватар пользователя.
     */
    private String image;
}
