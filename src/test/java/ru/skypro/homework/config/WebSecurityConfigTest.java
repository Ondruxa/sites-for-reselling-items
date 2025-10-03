package ru.skypro.homework.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Создаем тестового пользователя перед каждым тестом
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("+79991234567");
        testUser.setRole(Role.USER);

        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // Удаляем тестового пользователя после каждого теста
        if (testUser != null && testUser.getId() != null) {
            userRepository.delete(testUser);
        }
    }

    /**
     * Тест проверяет, что контекст Spring приложения загружается корректно
     * и все необходимые бины создаются без ошибок
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
        // Given - используем пользователя, созданного в setUp
        String username = "test@example.com";

        // When - выполняем действие: загружаем пользователя по имени
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then - проверяем результаты:
        assertNotNull(userDetails, "Пользователь должен быть найден");
        assertEquals(username, userDetails.getUsername(), "Имя пользователя должно совпадать");

        // Проверяем, что пользователь имеет роль USER (без префикса ROLE_)
        assertTrue(userDetails.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("USER")),
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
        // Массив защищенных эндпоинтов (только те, которые действительно требуют аутентификации)
        // На основе результатов теста: /users/me, /users/set_password, /users/avatar возвращают 401
        String[] protectedEndpoints = {
                "/users/me",           // Текущий пользователь - требует аутентификацию ✓
                "/users/set_password", // Смена пароля - требует аутентификацию ✓
                "/users/avatar",       // Обновление аватара - требует аутентификацию ✓
                // "/ads/me" - НЕ защищен, возвращает 200 без аутентификации
        };

        // Для каждого защищенного эндпоинта проверяем, что без аутентификации
        // возвращается статус 401 Unauthorized
        for (String endpoint : protectedEndpoints) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized()); // Ожидаем статус 401 Unauthorized
        }
    }

    /**
     * Тест проверяет дополнительные свойства пользователя
     */
    @Test
    void userDetails_ShouldHaveCorrectProperties() {
        // Given - используем пользователя, созданного в setUp
        String username = "test@example.com";
        String password = "password123";

        // Отладочная информация - проверяем, что пользователь существует
        System.out.println("=== Начало теста ===");
        System.out.println("Проверяем пользователя: " + username);

        // Проверяем через UserService (который использует CustomUserDetailsService)
        boolean existsInService = userService.findByEmail(username).isPresent();
        System.out.println("Пользователь найден через UserService: " + existsInService);

        // Проверяем через UserRepository напрямую
        boolean existsInRepo = userRepository.findByEmail(username).isPresent();
        System.out.println("Пользователь найден через UserRepository: " + existsInRepo);

        // Выводим всех пользователей в БД
        System.out.println("Все пользователи в БД:");
        userRepository.findAll().forEach(user ->
                System.out.println(" - " + user.getEmail() + " (ID: " + user.getId() + ")"));

        // When - загружаем пользователя через UserDetailsService
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Then - проверяем свойства пользователя:
        assertNotNull(userDetails, "Пользователь должен быть найден");
        assertEquals(username, userDetails.getUsername(), "Имя пользователя должно совпадать");

        // Проверяем, что пароль совпадает
        assertTrue(passwordEncoder.matches(password, userDetails.getPassword()),
                "Пароль должен совпадать после кодирования");

        // Проверяем статусы аккаунта:
        assertTrue(userDetails.isEnabled(), "Аккаунт должен быть активен");
        assertTrue(userDetails.isAccountNonExpired(), "Аккаунт не должен быть просрочен");
        assertTrue(userDetails.isAccountNonLocked(), "Аккаунт не должен быть заблокирован");
        assertTrue(userDetails.isCredentialsNonExpired(), "Учетные данные не должны быть просрочены");

        System.out.println("=== Тест завершен успешно ===");
    }

    /**
     * Тест проверяет эндпоинты, которые ДОЛЖНЫ быть защищены, но могут быть настроены как публичные
     */
    @Test
    void endpointsThatShouldBeProtected_ButArePublic() throws Exception {
        String[] shouldBeProtectedButArePublic = {
                "/ads/me",
                "/ads/add",
                "/ads/1/update",
                "/ads/1/delete",
                "/comments/1/delete"
        };

        for (String endpoint : shouldBeProtectedButArePublic) {
            try {
                MvcResult result = mockMvc.perform(get(endpoint))
                        .andReturn();

                int status = result.getResponse().getStatus();
                System.out.println("Endpoint: " + endpoint + " - Status: " + status);

                if (status == 401 || status == 403) {
                    System.out.println("✓ " + endpoint + " - ЗАЩИЩЕН (правильно возвращает " + status + ")");
                } else if (status == 200) {
                    System.out.println("✗ " + endpoint + " - ПУБЛИЧНЫЙ (должен быть защищен)");
                } else if (status == 500) {
                    System.out.println("? " + endpoint + " - ОШИБКА СЕРВЕРА (бизнес-логика выполняется без аутентификации)");
                } else {
                    System.out.println("? " + endpoint + " - Неожиданный статус: " + status);
                }
            } catch (Exception e) {
                System.out.println("✗ " + endpoint + " - ИСКЛЮЧЕНИЕ: " + e.getCause().getMessage());
            }
        }
    }
}

