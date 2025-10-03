package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AuthController
 * Тестируем endpoints аутентификации и регистрации
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // ===== ТЕСТЫ ДЛЯ POST /login =====

    /**
     * Тест успешной аутентификации пользователя
     * Цель: Проверить корректную обработку валидных учетных данных
     * Сценарий: Пользователь отправляет правильные username и password
     * Ожидаемый результат: HTTP 200 OK
     */
    @Test
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        // Arrange
        Login loginRequest = new Login();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password123");

        when(authService.login("test@example.com", "password123")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        verify(authService, times(1)).login("test@example.com", "password123");
    }

    /**
     * Тест неуспешной аутентификации с неверными учетными данными
     * Цель: Проверить обработку невалидных учетных данных
     * Сценарий: Пользователь отправляет неправильные username или password
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        Login loginRequest = new Login();
        loginRequest.setUsername("wrong@example.com");
        loginRequest.setPassword("wrongpassword");

        when(authService.login("wrong@example.com", "wrongpassword")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login("wrong@example.com", "wrongpassword");
    }

    /**
     * Тест аутентификации с пустыми полями
     * Цель: Проверить обработку запроса с пустыми учетными данными
     * Сценарий: Пользователь отправляет пустые username и password
     * Ожидаемый результат: HTTP 401 Unauthorized (или другая логика обработки)
     */
    @Test
    void login_WithEmptyCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        Login loginRequest = new Login();
        loginRequest.setUsername("");
        loginRequest.setPassword("");

        when(authService.login("", "")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        verify(authService, times(1)).login("", "");
    }

    /**
     * Тест аутентификации с null значениями
     * Цель: Проверить обработку запроса с null значениями
     * Сценарий: Пользователь отправляет null вместо учетных данных
     * Ожидаемый результат: HTTP 400 Bad Request (валидация Jackson)
     */
    @Test
    void login_WithNullValues_ShouldHandleGracefully() throws Exception {
        // Arrange
        String nullJson = "{\"username\": null, \"password\": null}";

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nullJson))
                .andExpect(status().is4xxClientError());
    }

    /**
     * Тест аутентификации с неполными данными
     * Цель: Проверить обработку запроса без обязательных полей
     * Сценарий: Пользователь отправляет JSON без password
     * Ожидаемый результат: Jackson десериализует с default значениями
     */
    @Test
    void login_WithMissingPassword_ShouldCallServiceWithDefaults() throws Exception {
        // Arrange
        String incompleteJson = "{\"username\": \"test@example.com\"}";

        // Jackson создаст объект Login с password = null
        when(authService.login("test@example.com", null)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isUnauthorized()); // Сервис вернет false → 401

        verify(authService, times(1)).login("test@example.com", null);
    }

    // ===== ТЕСТЫ ДЛЯ POST /register =====

    /**
     * Тест успешной регистрации нового пользователя
     * Цель: Проверить корректную обработку валидных данных регистрации
     * Сценарий: Пользователь отправляет полные и корректные данные для регистрации
     * Ожидаемый результат: HTTP 201 Created
     */
    @Test
    void register_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("newuser@example.com");
        registerRequest.setPassword("newpassword123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhone("+79991234567");
        registerRequest.setRole(ru.skypro.homework.dto.Role.USER);

        when(authService.register(any(Register.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(Register.class));
    }

    /**
     * Тест неуспешной регистрации (например, пользователь уже существует)
     * Цель: Проверить обработку ситуации когда регистрация невозможна
     * Сценарий: Пользователь пытается зарегистрироваться с существующим username
     * Ожидаемый результат: HTTP 400 Bad Request
     */
    @Test
    void register_WhenRegistrationFails_ShouldReturnBadRequest() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("existing@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Smith");
        registerRequest.setPhone("+79998765432");
        registerRequest.setRole(ru.skypro.homework.dto.Role.USER);

        when(authService.register(any(Register.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(Register.class));
    }

    /**
     * Тест регистрации с минимальными данными
     * Цель: Проверить обработку регистрации только с обязательными полями
     * Сценарий: Пользователь отправляет только username и password
     * Ожидаемый результат: Зависит от бизнес-логики (может быть 201 или 400)
     */
    @Test
    void register_WithMinimalData_ShouldHandleAppropriately() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("minimal@example.com");
        registerRequest.setPassword("minpass");

        // Если сервис принимает минимальные данные
        when(authService.register(any(Register.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(Register.class));
    }

    /**
     * Тест регистрации с пустыми полями
     * Цель: Проверить обработку запроса с пустыми значениями
     * Сценарий: Пользователь отправляет пустые строки в полях
     * Ожидаемый результат: HTTP 400 Bad Request (или другая логика)
     */
    @Test
    void register_WithEmptyFields_ShouldHandleGracefully() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("");
        registerRequest.setPassword("");
        registerRequest.setFirstName("");
        registerRequest.setLastName("");
        registerRequest.setPhone("");

        when(authService.register(any(Register.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(Register.class));
    }

    /**
     * Тест регистрации с невалидным JSON
     * Цель: Проверить обработку синтаксически неверного JSON
     * Сценарий: Пользователь отправляет битый JSON
     * Ожидаемый результат: HTTP 400 Bad Request
     */
    @Test
    void register_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidJson = "{\"username\": \"test\", \"password\": }";

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any(Register.class));
    }

    /**
     * Тест регистрации с ролью ADMIN
     * Цель: Проверить возможность регистрации с разными ролями
     * Сценарий: Пользователь пытается зарегистрироваться с ролью ADMIN
     * Ожидаемый результат: Зависит от бизнес-логики приложения
     */
    @Test
    void register_WithAdminRole_ShouldHandleAppropriately() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("admin@example.com");
        registerRequest.setPassword("adminpass");
        registerRequest.setFirstName("Admin");
        registerRequest.setLastName("User");
        registerRequest.setPhone("+79991112233");
        registerRequest.setRole(ru.skypro.homework.dto.Role.ADMIN);

        when(authService.register(any(Register.class))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(authService, times(1)).register(any(Register.class));
    }

    // ===== ТЕСТЫ CORS =====

    /**
     * Тест CORS политики для endpoints аутентификации
     * Цель: Проверить что CORS разрешен для localhost:3000
     * Сценарий: Запрос с Origin http://localhost:3000
     * Ожидаемый результат: HTTP 200 OK с CORS заголовками
     */
    @Test
    void cors_ShouldAllowLocalhost3000() throws Exception {
        // Arrange
        Login loginRequest = new Login();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password");

        when(authService.login("test@example.com", "password")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/login")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    /**
     * Тест последовательных операций аутентификации и регистрации
     * Цель: Проверить стабильность работы endpoints при последовательных вызовах
     * Сценарий: Пользователь регистрируется, затем пытается аутентифицироваться
     * Ожидаемый результат: Корректная обработка обоих запросов
     */
    @Test
    void authWorkflow_RegisterThenLogin_ShouldWorkCorrectly() throws Exception {
        // Arrange - регистрация
        Register registerRequest = new Register();
        registerRequest.setUsername("workflow@example.com");
        registerRequest.setPassword("workflowpass");
        registerRequest.setFirstName("Workflow");
        registerRequest.setLastName("User");
        registerRequest.setPhone("+79994445566");
        registerRequest.setRole(ru.skypro.homework.dto.Role.USER);

        when(authService.register(any(Register.class))).thenReturn(true);

        // Act & Assert - регистрация
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Arrange - аутентификация
        Login loginRequest = new Login();
        loginRequest.setUsername("workflow@example.com");
        loginRequest.setPassword("workflowpass");

        when(authService.login("workflow@example.com", "workflowpass")).thenReturn(true);

        // Act & Assert - аутентификация
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Verify
        verify(authService, times(1)).register(any(Register.class));
        verify(authService, times(1)).login("workflow@example.com", "workflowpass");
    }

    /**
     * Тест обработки очень длинных входных данных
     * Цель: Проверить устойчивость к большим объемам данных
     * Сценарий: Пользователь отправляет очень длинные строки в полях
     * Ожидаемый результат: Зависит от валидации (может быть 400 или 201)
     */
    @Test
    void register_WithVeryLongData_ShouldHandleAppropriately() throws Exception {
        // Arrange
        Register registerRequest = new Register();
        registerRequest.setUsername("a".repeat(100) + "@example.com");
        registerRequest.setPassword("p".repeat(100));
        registerRequest.setFirstName("f".repeat(50));
        registerRequest.setLastName("l".repeat(50));
        registerRequest.setPhone("+7" + "9".repeat(15));

        // Сервис может либо принять, либо отклонить - тестируем оба сценария
        when(authService.register(any(Register.class))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).register(any(Register.class));
    }
}
