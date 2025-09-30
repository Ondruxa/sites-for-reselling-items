package ru.skypro.homework.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.dto.Role;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тест проверяет, что контекст Spring приложения загружается корректно
     * и все необходимые бины создаются без ошибок
     * Это базовый тест, который гарантирует, что конфигурация применилась правильно
     */
    @Test
    void contextLoads() {
        // Проверяем, что UserDetailsService был создан и внедрен
        assertNotNull(userDetailsService, "UserDetailsService должен быть создан в контексте Spring");
        // Проверяем, что PasswordEncoder был создан и внедрен
        assertNotNull(passwordEncoder, "PasswordEncoder должен быть создан в контексте Spring");
    }

    /**
     * Тест проверяет корректность работы UserDetailsService:
     * - пользователь загружается по имени
     * - у пользователя правильное имя
     * - пользователь имеет правильную роль (USER)
     */
    @Test
    void userDetailsService_ShouldLoadUserByUsername() {
        // Given - задаем условия теста: имя существующего пользователя
        String username = "user@gmail.com";

        // When - выполняем действие: загружаем пользователя по имени
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then - проверяем результаты:
        // Пользователь должен быть найден
        assertNotNull(userDetails, "Пользователь должен быть найден");
        // Имя пользователя должно соответствовать запрошенному
        assertEquals(username, userDetails.getUsername(), "Имя пользователя должно совпадать");
        // Пользователь должен иметь роль USER
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + Role.USER.name())),
                "Пользователь должен иметь роль USER");
    }

    /**
     * Тест проверяет обработку ситуации, когда запрашивается несуществующий пользователь
     * Ожидается, что сервис выбросит UsernameNotFoundException
     */
    @Test
    void userDetailsService_ShouldThrowExceptionForNonExistentUser() {
        // Given - задаем условия: имя несуществующего пользователя
        String nonExistentUsername = "nonexistent@gmail.com";

        // When & Then - проверяем, что при загрузке несуществующего пользователя выбрасывается ожидаемое исключение
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(nonExistentUsername),
                "Для несуществующего пользователя должно выбрасываться UsernameNotFoundException");
    }

    /**
     * Тест проверяет, что PasswordEncoder реализован через BCrypt
     */
    @Test
    void passwordEncoder_ShouldBeBCrypt() {
        // Проверяем, что используемый PasswordEncoder является BCryptPasswordEncoder
        assertTrue(passwordEncoder instanceof org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder,
                "PasswordEncoder должен быть реализацией BCryptPasswordEncoder");
    }

    /**
     * Тест проверяет функциональность кодирования и проверки паролей:
     * - пароль кодируется без ошибок
     * - закодированный пароль имеет правильный формат BCrypt
     * - оригинальный пароль совпадает с закодированным
     */
    @Test
    void passwordEncoder_ShouldEncodeAndMatchPassword() {
        // Given - задаем условия: оригинальный пароль
        String rawPassword = "password";

        // When - выполняем действия: кодируем пароль
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then - проверяем результаты:
        // Закодированный пароль не должен быть null
        assertNotNull(encodedPassword, "Закодированный пароль не должен быть null");
        // Закодированный пароль должен начинаться с префикса BCrypt ($2a$)
        assertTrue(encodedPassword.startsWith("$2a$"),
                "Закодированный пароль должен иметь префикс BCrypt ($2a$)");
        // Оригинальный пароль должен совпадать с закодированным
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword),
                "Оригинальный пароль должен совпадать с закодированной версией");
    }

    /**
     * Тест проверяет корректность конфигурации Spring Security для публичных эндпоинтов
     */
    @Test
    void publicEndpoints_ShouldNotRequireAuthentication() throws Exception {
        // Массив публичных эндпоинтов из AUTH_WHITELIST
        String[] publicEndpoints = {
                "/swagger-ui.html",
                "/v3/api-docs",
                "/login",
                "/register"
        };

        // Для каждого публичного эндпоинта проверяем, что он не возвращает 401 Unauthorized
        for (String endpoint : publicEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(result -> {
                        int status = result.getResponse().getStatus();
                        // Проверяем, что статус не 401 (Unauthorized) и не 403 (Forbidden)
                        assertTrue(status != 401 && status != 403,
                                "Endpoint " + endpoint + " не должен требовать аутентификации. Получен статус: " + status);
                    });
        }
    }

    /**
     * Тест проверяет, что защищенные эндпоинты требуют аутентификации
     * При обращении без аутентификации должен возвращаться статус 401 (Unauthorized)
     */
    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        // Массив защищенных эндпоинтов
        String[] protectedEndpoints = {
                "/ads",        // Все объявления
                "/ads/1",      // Конкретное объявление
                "/users/me",   // Текущий пользователь
                "/users/1"     // Конкретный пользователь
        };

        // Для каждого защищенного эндпоинта проверяем, что без аутентификации
        // возвращается статус 401 Unauthorized
        for (String endpoint : protectedEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized()); // Ожидаем статус 401 Unauthorized
        }
    }

    /**
     * Тест проверяет дополнительные свойства пользователя:
     * - аккаунт активен (enabled)
     * - аккаунт не просрочен (accountNonExpired)
     * - аккаунт не заблокирован (accountNonLocked)
     * - учетные данные не просрочены (credentialsNonExpired)
     */
    @Test
    void userDetails_ShouldHaveCorrectProperties() {
        // Given - задаем условия: данные тестового пользователя
        String username = "user@gmail.com";
        String password = "password";

        // When - загружаем пользователя
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then - проверяем свойства пользователя:
        assertNotNull(userDetails, "Пользователь должен быть найден");
        assertEquals(username, userDetails.getUsername(), "Имя пользователя должно совпадать");
        // Проверяем, что пароль совпадает (используя кодировщик)
        assertTrue(passwordEncoder.matches(password, userDetails.getPassword()),
                "Пароль должен совпадать после кодирования");

        // Проверяем статусы аккаунта:
        assertTrue(userDetails.isEnabled(), "Аккаунт должен быть активен");
        assertTrue(userDetails.isAccountNonExpired(), "Аккаунт не должен быть просрочен");
        assertTrue(userDetails.isAccountNonLocked(), "Аккаунт не должен быть заблокирован");
        assertTrue(userDetails.isCredentialsNonExpired(), "Учетные данные не должны быть просрочены");
    }
}

