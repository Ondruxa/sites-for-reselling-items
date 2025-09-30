package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.config.TestSecurityConfig;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты для AuthServiceImpl
 * <p>
 * Особенности:
 * - Полностью отключает Spring Security через properties
 * - Использует H2 in-memory базу данных
 * - Тестирует реальное взаимодействие с UserDetailsManager и PasswordEncoder
 * - @Transactional обеспечивает изоляцию тестов
 */
@SpringBootTest
@Transactional
@Import(TestSecurityConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")

public class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserDetailsManager userDetailsManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String TEST_USERNAME = "testuser@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_FIRST_NAME = "Ivan";
    private static final String TEST_LAST_NAME = "Ivanov";
    private static final String TEST_PHONE = "+79991234567";
    private static final String TEST_INVALID_USERNAME = "nonexistent@example.com";

    /**
     * Очистка перед каждым тестом
     * Удаляем тестового пользователя, если он существует
     */
    @BeforeEach
    void setUp() {
        if (userDetailsManager.userExists(TEST_USERNAME)) {
            userDetailsManager.deleteUser(TEST_USERNAME);
        }
        // Также очищаем других тестовых пользователей
        if (userDetailsManager.userExists("admin@example.com")) {
            userDetailsManager.deleteUser("admin@example.com");
        }
        if (userDetailsManager.userExists("minimal@example.com")) {
            userDetailsManager.deleteUser("minimal@example.com");
        }
        if (userDetailsManager.userExists("empty@example.com")) {
            userDetailsManager.deleteUser("empty@example.com");
        }
    }

    /**
     * Вспомогательный метод для создания тестового пользователя
     */
    private void createTestUser(String username, String password) {
        userDetailsManager.createUser(
                User.builder()
                        .passwordEncoder(passwordEncoder::encode)
                        .password(password)
                        .username(username)
                        .roles("USER")
                        .build()
        );
    }

    /**
     * Вспомогательный метод для создания DTO регистрации
     */
    private Register createRegisterDto(String username, String password, String firstName,
                                       String lastName, String phone, ru.skypro.homework.dto.Role role) {
        Register register = new Register();
        register.setUsername(username);
        register.setPassword(password);
        register.setFirstName(firstName);
        register.setLastName(lastName);
        register.setPhone(phone);
        register.setRole(role);
        return register;
    }

    /**
     * Тест успешной аутентификации существующего пользователя
     */
    @Test
    void login_WithValidCredentials_ShouldReturnTrue() {
        // Given: создаем пользователя напрямую через UserDetailsManager
        createTestUser(TEST_USERNAME, TEST_PASSWORD);

        // When: пытаемся аутентифицироваться
        boolean result = authService.login(TEST_USERNAME, TEST_PASSWORD);

        // Then: аутентификация должна быть успешной
        assertTrue(result, "Аутентификация должна быть успешной для валидных учетных данных");
    }

    /**
     * Тест неуспешной аутентификации с неверным паролем
     */
    @Test
    void login_WithInvalidPassword_ShouldReturnFalse() {
        // Given: создаем пользователя
        createTestUser(TEST_USERNAME, TEST_PASSWORD);

        // When: пытаемся аутентифицироваться с неверным паролем
        boolean result = authService.login(TEST_USERNAME, "wrongpassword");

        // Then: аутентификация должна провалиться
        assertFalse(result, "Аутентификация должна провалиться для неверного пароля");
    }

    /**
     * Тест аутентификации несуществующего пользователя
     */
    @Test
    void login_WithNonExistentUser_ShouldReturnFalse() {
        // When: пытаемся аутентифицироваться с несуществующим пользователем
        boolean result = authService.login(TEST_INVALID_USERNAME, TEST_PASSWORD);

        // Then: аутентификация должна провалиться
        assertFalse(result, "Аутентификация должна провалиться для несуществующего пользователя");
    }

    /**
     * Тест успешной регистрации нового пользователя с ролью USER
     */
    @Test
    void register_WithNewUserAndUserRole_ShouldCreateUserAndReturnTrue() {
        // Given: создаем DTO для регистрации со всеми полями
        Register register = createRegisterDto(TEST_USERNAME, TEST_PASSWORD, TEST_FIRST_NAME,
                TEST_LAST_NAME, TEST_PHONE, ru.skypro.homework.dto.Role.USER);

        // When: регистрируем нового пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна быть успешной
        assertTrue(result, "Регистрация должна быть успешной для нового пользователя");

        // And: пользователь должен существовать в системе
        assertTrue(userDetailsManager.userExists(TEST_USERNAME),
                "Пользователь должен быть создан в системе");

        // And: пользователь должен иметь возможность аутентифицироваться
        UserDetails userDetails = userDetailsManager.loadUserByUsername(TEST_USERNAME);
        assertNotNull(userDetails, "Детали пользователя не должны быть null");
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, userDetails.getPassword()),
                "Пароль должен быть корректно закодирован");
    }

    /**
     * Тест попытки регистрации уже существующего пользователя
     */
    @Test
    void register_WithExistingUser_ShouldReturnFalse() {
        // Given: создаем пользователя
        createTestUser(TEST_USERNAME, TEST_PASSWORD);

        // And: создаем DTO для регистрации с тем же username
        Register register = createRegisterDto(TEST_USERNAME, "newpassword", "Petr",
                "Petrov", "+79997654321", ru.skypro.homework.dto.Role.USER);

        // When: пытаемся зарегистрировать существующего пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна провалиться
        assertFalse(result, "Регистрация должна провалиться для существующего пользователя");
    }

    /**
     * Тест проверки кодирования пароля при регистрации
     */
    @Test
    void register_ShouldEncodePassword() {
        // Given: создаем DTO для регистрации
        Register register = createRegisterDto(TEST_USERNAME, TEST_PASSWORD, TEST_FIRST_NAME,
                TEST_LAST_NAME, TEST_PHONE, ru.skypro.homework.dto.Role.USER);

        // When: регистрируем пользователя
        authService.register(register);

        // Then: проверяем, что пароль закодирован
        UserDetails userDetails = userDetailsManager.loadUserByUsername(TEST_USERNAME);
        String storedPassword = userDetails.getPassword();

        assertNotEquals(TEST_PASSWORD, storedPassword,
                "Пароль не должен храниться в открытом виде");
        assertTrue(passwordEncoder.matches(TEST_PASSWORD, storedPassword),
                "Закодированный пароль должен соответствовать исходному");
    }

    /**
     * Тест регистрации пользователя с ролью ADMIN
     */
    @Test
    void register_WithAdminRole_ShouldCreateAdminUser() {
        // Given: создаем DTO для регистрации с ролью ADMIN
        Register register = createRegisterDto("admin@example.com", TEST_PASSWORD,
                "Admin", "Adminov", "+79998887766", ru.skypro.homework.dto.Role.ADMIN);

        // When: регистрируем пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна быть успешной
        assertTrue(result, "Регистрация должна быть успешной для ADMIN пользователя");
        assertTrue(userDetailsManager.userExists("admin@example.com"),
                "ADMIN пользователь должен быть создан в системе");
    }

    /**
     * Тест регистрации с минимальными данными (только обязательные поля)
     */
    @Test
    void register_WithMinimumData_ShouldCreateUser() {
        // Given: создаем DTO только с обязательными полями
        Register register = new Register();
        register.setUsername("minimal@example.com");
        register.setPassword(TEST_PASSWORD);
        register.setRole(ru.skypro.homework.dto.Role.USER);
        // firstName, lastName, phone могут быть null

        // When: регистрируем пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна быть успешной
        assertTrue(result, "Регистрация должна быть успешной с минимальными данными");
        assertTrue(userDetailsManager.userExists("minimal@example.com"),
                "Пользователь должен быть создан в системе");
    }

    /**
     * Тест граничного случая: пустой пароль
     */
    @Test
    void register_WithEmptyPassword_ShouldCreateUser() {
        // Given: создаем DTO с пустым паролем
        Register register = createRegisterDto("empty@example.com", "",
                "Empty", "Password", "+79990000000", ru.skypro.homework.dto.Role.USER);

        // When: регистрируем пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна быть успешной (пустой пароль допустим)
        assertTrue(result, "Регистрация должна быть успешной даже с пустым паролем");
        assertTrue(userDetailsManager.userExists("empty@example.com"),
                "Пользователь должен быть создан в системе");
    }

    /**
     * Тест регистрации пользователя без дополнительных полей (только username, password, role)
     */
    @Test
    void register_WithOnlyRequiredFields_ShouldCreateUser() {
        // Given: создаем DTO только с обязательными полями
        Register register = new Register();
        register.setUsername("requiredonly@example.com");
        register.setPassword("requiredpass");
        register.setRole(ru.skypro.homework.dto.Role.USER);

        // When: регистрируем пользователя
        boolean result = authService.register(register);

        // Then: регистрация должна быть успешной
        assertTrue(result, "Регистрация должна быть успешной только с обязательными полями");
        assertTrue(userDetailsManager.userExists("requiredonly@example.com"),
                "Пользователь должен быть создан в системе");

        // And: пользователь должен аутентифицироваться
        boolean loginResult = authService.login("requiredonly@example.com", "requiredpass");
        assertTrue(loginResult, "Пользователь должен успешно аутентифицироваться");
    }
}
