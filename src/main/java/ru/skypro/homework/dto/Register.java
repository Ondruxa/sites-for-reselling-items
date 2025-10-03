package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для регистрации нового пользователя.
 * Используется при создании аккаунта.
 */
@Data
public class Register {
    /**
     * Имя пользователя (логин).
     */
    private String username;
    /**
     * Пароль пользователя.
     */
    private String password;
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
     * Роль пользователя.
     */
    private Role role;
}
