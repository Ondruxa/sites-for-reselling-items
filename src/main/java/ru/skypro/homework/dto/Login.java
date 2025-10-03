package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для передачи данных авторизации пользователя.
 * Используется при входе пользователя в систему.
 */
@Data
public class Login {
    /**
     * Имя пользователя (логин).
     */
    private String username;
    /**
     * Пароль пользователя.
     */
    private String password;
}
