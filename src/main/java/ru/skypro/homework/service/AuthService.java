package ru.skypro.homework.service;

import ru.skypro.homework.dto.Register;

/**
 * Сервис аутентификации и регистрации.
 * <p>
 * Отвечает за проверку учетных данных пользователя (логин) и регистрацию новой учётной записи.
 * </p>
 */
public interface AuthService {

    /**
     * Проверяет корректность email, пароль.
     * @param userName email (логин)
     * @param password сырой пароль (до шифрования)
     * @return true — если пользователь найден и пароль совпал, иначе false
     */
    boolean login(String userName, String password);

    /**
     * Регистрирует нового пользователя.
     * @param register DTO с данными регистрации
     * @return true — если регистрация успешна, false — если пользователь с таким email уже существует
     */
    boolean register(Register register);
}
