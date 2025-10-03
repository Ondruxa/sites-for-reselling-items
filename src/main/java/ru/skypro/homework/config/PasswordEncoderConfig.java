package ru.skypro.homework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Конфигурация шифрования паролей.
 * Регистрирует единственный бин {@link PasswordEncoder}, который будет
 * инжектиться в сервисы (регистрация / смена пароля / аутентификация).
 * Используется {@link BCryptPasswordEncoder} с настройками по умолчанию (strength = 10),
 * что даёт устойчивый к перебору хэш.
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Бин кодировщика паролей приложения.
     * @return реализация {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
