package ru.skypro.homework.dto;

import lombok.Data;

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
