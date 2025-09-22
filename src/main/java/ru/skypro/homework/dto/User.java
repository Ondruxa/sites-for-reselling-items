package ru.skypro.homework.dto;

import lombok.Data;

@Data
public class User {
    /**
     * ID пользователя (integer)
     */
    private Integer id;

    /**
     * Email пользователя (login)
     */
    private String email;

    /**
     * Имя пользователя (string)
     */
    private String firstName;

    /**
     * Фамилия пользователя (string)
     */
    private String lastName;

    /**
     * Телефон пользователя (string)
     */
    private String phone;

    /**
     * Роль пользователя (USER | ADMIN)
     */
    private String role;

    /**
     * Ссылка на аватар пользователя (string)
     */
    private String image;
}
