package ru.skypro.homework.dto;

import lombok.Data;

/**
 * DTO для смены пароля пользователя.
 * Используется при изменении пароля через профиль.
 */
@Data
public class NewPassword {
    /**
     * Текущий пароль пользователя.
     */
    private String currentPassword;
    /**
     * Новый пароль пользователя.
     */
    private String newPassword;
}
