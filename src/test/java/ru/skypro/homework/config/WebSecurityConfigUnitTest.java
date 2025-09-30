package ru.skypro.homework.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import ru.skypro.homework.dto.Role;

import static org.junit.jupiter.api.Assertions.*;

public class WebSecurityConfigUnitTest {

    private WebSecurityConfig webSecurityConfig;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Тест проверяет, что метод passwordEncoder() возвращает
     * корректную реализацию BCryptPasswordEncoder
     */
    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // When - вызываем метод создания PasswordEncoder
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();

        // Then - проверяем, что возвращен правильный тип encoder'а
        assertNotNull(encoder, "PasswordEncoder не должен быть null");
        assertTrue(encoder instanceof BCryptPasswordEncoder,
                "Метод passwordEncoder() должен возвращать BCryptPasswordEncoder");
    }

    /**
     * Тест проверяет создание UserDetailsService с правильными настройками пользователя
     */
    @Test
    void userDetailsService_ShouldCreateUserWithCorrectProperties() {
        // When - создаем UserDetailsService через тестируемый метод
        InMemoryUserDetailsManager userDetailsService = webSecurityConfig.userDetailsService(passwordEncoder);

        // Then - проверяем, что сервис создан и работает корректно
        assertNotNull(userDetailsService, "UserDetailsService должен быть создан");

        // Проверяем, что пользователь может быть загружен
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@gmail.com");
        assertNotNull(userDetails, "Пользователь должен быть загружен из UserDetailsService");
        assertEquals("user@gmail.com", userDetails.getUsername(),
                "Имя пользователя должно соответствовать конфигурации");
        // Проверяем, что пароль совпадает после кодирования
        assertTrue(passwordEncoder.matches("password", userDetails.getPassword()),
                "Пароль должен совпадать после кодирования");
        // Проверяем, что пользователь имеет правильную роль
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + Role.USER.name())),
                "Пользователь должен иметь роль USER");
    }

    /**
     * Тест проверяет корректность создания бинов безопасности
     */
    @Test
    void securityBeans_ShouldBeCreated() {
        // Given
        WebSecurityConfig config = new WebSecurityConfig();

        // When - создаем бины
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        UserDetailsService userDetailsService = config.userDetailsService(passwordEncoder);

        // Then - проверяем, что бины созданы корректно
        assertNotNull(passwordEncoder, "PasswordEncoder должен быть создан");
        assertNotNull(userDetailsService, "UserDetailsService должен быть создан");
        assertTrue(passwordEncoder instanceof BCryptPasswordEncoder,
                "PasswordEncoder должен быть BCryptPasswordEncoder");

        // Проверяем, что UserDetailsService может загружать пользователя
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@gmail.com");
        assertNotNull(userDetails, "Должен загружаться тестовый пользователь");
    }

}

