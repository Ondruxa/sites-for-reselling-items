package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceImplIntegrationTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Тест успешной аутентификации с корректными учетными данными
     * - Создается пользователь в базе данных с закодированным паролем
     * - Выполняется попытка входа с правильным email и паролем
     * - Ожидается возврат true (успешная аутентификация)
     */
    @Test
    void login_WithValidCredentials_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        String password = "password123";

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("+79999999999");
        user.setRole(Role.USER);
        userRepository.save(user);

        // When
        boolean result = authService.login(email, password);

        // Then
        assertTrue(result);
    }

    /**
     * Тест неуспешной аутентификации с неверным паролем
     * - Создается пользователь в базе данных
     * - Выполняется попытка входа с правильным email, но неверным паролем
     * - Ожидается возврат false (неуспешная аутентификация)
     */
    @Test
    void login_WithInvalidPassword_ShouldReturnFalse() {
        // Given
        String email = "test@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(correctPassword));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhone("+79999999999");
        user.setRole(Role.USER);
        userRepository.save(user);

        // When
        boolean result = authService.login(email, wrongPassword);

        // Then
        assertFalse(result);
    }

    /**
     * Тест неуспешной аутентификации с несуществующим пользователем
     * - В базе данных нет пользователя с указанным email
     * - Выполняется попытка входа с несуществующими учетными данными
     * - Ожидается возврат false (неуспешная аутентификация)
     */
    @Test
    void login_WithNonExistentUser_ShouldReturnFalse() {
        // When
        boolean result = authService.login("nonexistent@example.com", "password");

        // Then
        assertFalse(result);
    }

    /**
     * Тест успешной регистрации нового пользователя
     * - Создается DTO с данными нового пользователя
     * - Выполняется регистрация пользователя
     * - Проверяется, что пользователь сохранен в базе с корректными данными
     * - Проверяется, что пароль закодирован
     */
    @Test
    void register_WithNewUser_ShouldReturnTrueAndSaveUser() {
        // Given
        Register register = new Register();
        register.setUsername("newuser@example.com");
        register.setPassword("newpassword123");
        register.setFirstName("Jane");
        register.setLastName("Smith");
        register.setPhone("+78888888888");
        register.setRole(Role.USER); // В DTO передаем строку, маппер конвертирует в enum

        // When
        boolean result = authService.register(register);

        // Then
        assertTrue(result);

        Optional<UserEntity> savedUser = userRepository.findByEmail("newuser@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals("Jane", savedUser.get().getFirstName());
        assertEquals("Smith", savedUser.get().getLastName());
        assertEquals("+78888888888", savedUser.get().getPhone());
        assertEquals(Role.USER, savedUser.get().getRole());

        // Verify password is encoded
        assertTrue(passwordEncoder.matches("newpassword123", savedUser.get().getPassword()));
    }

    /**
     * Тест неуспешной регистрации с существующим email
     * - Создается пользователь с определенным email
     * - Выполняется попытка регистрации нового пользователя с тем же email
     * - Ожидается возврат false
     * - Проверяется, что исходный пользователь не был изменен
     */
    @Test
    void register_WithExistingEmail_ShouldReturnFalse() {
        // Given
        String existingEmail = "existing@example.com";

        UserEntity existingUser = new UserEntity();
        existingUser.setEmail(existingEmail);
        existingUser.setPassword(passwordEncoder.encode("password"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        existingUser.setPhone("+77777777777");
        existingUser.setRole(Role.USER);
        userRepository.save(existingUser);

        Register register = new Register();
        register.setUsername(existingEmail);
        register.setPassword("newpassword");
        register.setFirstName("New");
        register.setLastName("User");
        register.setPhone("+76666666666");
        register.setRole(Role.USER);

        // When
        boolean result = authService.register(register);

        // Then
        assertFalse(result);

        Optional<UserEntity> user = userRepository.findByEmail(existingEmail);
        assertTrue(user.isPresent());
        assertEquals("Existing", user.get().getFirstName());
        assertEquals(Role.USER, user.get().getRole()); // Проверяем, что роль не изменилась
    }

    /**
     * Тест регистрации с пустым паролем
     * - Создается DTO с пустым паролем
     * - Выполняется регистрация пользователя
     * - Проверяется, что пароль закодирован
     * - Проверяется, что пользователь успешно сохранен
     */
    @Test
    void register_WithEmptyPassword_ShouldEncodeAndSaveUser() {
        // Given
        Register register = new Register();
        register.setUsername("emptyPassword@example.com");
        register.setPassword("");
        register.setFirstName("Empty");
        register.setLastName("Password");
        register.setPhone("+75555555555");
        register.setRole(Role.USER);

        // When
        boolean result = authService.register(register);

        // Then
        assertTrue(result);

        Optional<UserEntity> savedUser = userRepository.findByEmail("emptyPassword@example.com");
        assertTrue(savedUser.isPresent());
        // Password should be encoded, not stored as plain text
        assertNotEquals("", savedUser.get().getPassword());
        assertTrue(passwordEncoder.matches("", savedUser.get().getPassword()));
        assertEquals(Role.USER, savedUser.get().getRole()); // Проверяем роль
    }

    /**
     * Тест регистрации пользователя с ролью ADMIN
     * - Создается DTO с ролью ADMIN
     * - Выполняется регистрация пользователя
     * - Проверяется, что пользователь сохранен с правильной ролью ADMIN
     * - Проверяется успешность операции регистрации
     */
    @Test
    void register_WithAdminRole_ShouldSaveWithAdminRole() {
        // Given
        Register register = new Register();
        register.setUsername("admin@example.com");
        register.setPassword("adminpass");
        register.setFirstName("Admin");
        register.setLastName("User");
        register.setPhone("+74444444444");
        register.setRole(Role.ADMIN); // Передаем строку "ADMIN"

        // When
        boolean result = authService.register(register);

        // Then
        assertTrue(result);

        Optional<UserEntity> savedUser = userRepository.findByEmail("admin@example.com");
        assertTrue(savedUser.isPresent());
        assertEquals(Role.ADMIN, savedUser.get().getRole()); // Проверяем, что роль сохранилась как ADMIN
    }
}
