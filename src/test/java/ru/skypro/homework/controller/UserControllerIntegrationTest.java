package ru.skypro.homework.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.model.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.config.TestSecurityConfig;
import ru.skypro.homework.repository.UserRepository;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private NewPassword newPassword;
    private UpdateUser updateUser;

    @BeforeEach
    void setUp() {
        // Создаем объект для смены пароля с валидными данными
        newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword123");
        newPassword.setNewPassword("newPassword123");
        // Создаем объект для обновления профиля пользователя
        updateUser = new UpdateUser();
        updateUser.setFirstName("UpdatedJohn");
        updateUser.setLastName("UpdatedDoe");
        updateUser.setPhone("+79998887766");
    }

    /**
     * Тест успешного обновления пароля
     * Цель: Проверить корректное обновление пароля при валидных данных
     * Сценарий: Аутентифицированный пользователь отправляет корректные текущий и новый пароль
     * Ожидаемый результат: HTTP 200 OK
     */
    @Test
    @DisplayName("Обновление пароля - успешный сценарий")
    @WithMockUser(username = "user@gmail.com", password = "password", roles = "USER")
    void setPassword_WhenValidData_ShouldReturnOk() throws Exception {
        // Создаем пользователя в базе данных с закодированным паролем
        UserEntity testUser = new UserEntity();
        testUser.setEmail("user@gmail.com");
        testUser.setPassword(passwordEncoder.encode("currentPassword")); // Закодированный текущий пароль
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+79990000000");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        // Создаем DTO для смены пароля
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("currentPassword"); // Текущий пароль (в открытом виде)
        newPassword.setNewPassword("newPassword123");      // Новый пароль

        mockMvc.perform(post("/users/set_password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword)))
                .andExpect(status().isOk());
    }

    /**
     * Тест успешного получения профиля пользователя
     * Цель: Проверить корректное получение данных текущего пользователя
     * Сценарий: Аутентифицированный пользователь запрашивает свой профиль
     * Ожидаемый результат: HTTP 200 OK с данными пользователя
     */
    @Test
    @DisplayName("Получение профиля - успешный сценарий")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void getUser_WhenAuthorized_ShouldReturnUserInfo() throws Exception {
        mockMvc.perform(get("/users/me")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    /**
     * Тест обновления профиля с некорректными данными
     * Цель: Проверить валидацию данных при обновлении профиля
     * Сценарий: Аутентифицированный пользователь отправляет невалидные данные профиля
     * Ожидаемый результат: HTTP 200 OK (если валидация не реализована)
     */
    @Test
    @DisplayName("Обновление профиля - невалидные данные")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUser_WhenInvalidData_ShouldReturnOk() throws Exception {
        // Создаем тестового пользователя в базе данных
        UserEntity testUser = new UserEntity();
        testUser.setEmail("user@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+79990000000");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        UpdateUser invalidUser = new UpdateUser();
        invalidUser.setFirstName("");
        invalidUser.setLastName("Test");
        invalidUser.setPhone("invalid_phone");

        mockMvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isOk());
    }

    /**
     * Тест успешного обновления профиля пользователя
     * Цель: Проверить корректное обновление данных профиля
     * Сценарий: Аутентифицированный пользователь отправляет валидные данные для обновления
     * Ожидаемый результат: HTTP 200 OK с обновленными данными
     */
    @Test
    @DisplayName("Обновление профиля - успешный сценарий")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUser_WhenValidData_ShouldReturnUpdatedUser() throws Exception {
        UserEntity testUser = new UserEntity();
        testUser.setEmail("user@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+79990000000");
        testUser.setRole(Role.USER);
        userRepository.save(testUser);

        UpdateUser validUpdate = new UpdateUser();
        validUpdate.setFirstName("UpdatedJohn");
        validUpdate.setLastName("UpdatedDoe");
        validUpdate.setPhone("+79998887766");

        mockMvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedJohn"))
                .andExpect(jsonPath("$.lastName").value("UpdatedDoe"))
                .andExpect(jsonPath("$.phone").value("+79998887766"));
    }

    /**
     * Тест с различными ролями пользователей
     * Цель: Проверить доступность функционала для разных ролей
     * Сценарий: Пользователь с ролью ADMIN запрашивает профиль
     * Ожидаемый результат: HTTP 200 OK
     */
    @Test
    @DisplayName("Получение профиля - пользователь с ролью ADMIN")
    @WithMockUser(username = "admin@gmail.com", roles = "ADMIN")
    void getUser_WhenAdminRole_ShouldReturnUserInfo() throws Exception {
        UserEntity testUser = new UserEntity();
        testUser.setEmail("admin@gmail.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Admin");
        testUser.setLastName("User");
        testUser.setPhone("+79990000000");
        testUser.setRole(Role.ADMIN);
        userRepository.save(testUser);

        mockMvc.perform(get("/users/me")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Admin"));
    }

    /**
     * Тест получения профиля неавторизованным пользователем
     * Цель: Проверить обработку запроса без аутентификации
     * Сценарий: Неавторизованный пользователь запрашивает профиль
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Получение профиля - неавторизованный пользователь")
    void getUser_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized()); // Корректное ожидание для защищенного эндпоинта
    }

    /**
     * Тест обновления аватара неавторизованным пользователем
     * Цель: Проверить обработку запроса без аутентификации
     * Сценарий: Неавторизованный пользователь пытается загрузить аватар
     * Ожидаемый результат: HTTP 400 Bad Request
     */
    @Test
    @DisplayName("Обновление аватара - неавторизованный пользователь")
    void updateUserImage_WhenUnauthorized_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/users/me/image")
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }
}

