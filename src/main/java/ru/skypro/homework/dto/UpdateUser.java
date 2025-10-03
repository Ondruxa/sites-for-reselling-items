package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для обновления профиля пользователя.
 * Используется при изменении данных пользователя через профиль.
 */
@Data
public class UpdateUser {
    /**
     * Имя пользователя (min length: 3, max length: 10)
     */
    private String firstName;

    /**
     * Фамилия пользователя (min length: 3, max length: 10)
     */
    private String lastName;

    /**
     * Телефон пользователя (формат: '+7 XXX XXXX-XX-XX')
     */
    private String phone;
}
