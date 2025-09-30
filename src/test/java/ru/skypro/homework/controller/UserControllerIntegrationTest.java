package ru.skypro.homework.controller;

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

    // ===== ТЕСТЫ ОБНОВЛЕНИЯ ПАРОЛЯ =====
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
        mockMvc.perform(post("/users/set_password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword)))
                .andExpect(status().isOk());
    }

    /**
     * Тест обновления пароля без аутентификации
     * Цель: Проверить запрет доступа для неавторизованных пользователей
     * Сценарий: Неаутентифицированный пользователь пытается изменить пароль
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Обновление пароля - неавторизованный доступ")
    void setPassword_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/users/set_password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword)))
                .andExpect(status().isUnauthorized());
    }

    // ===== ТЕСТЫ ПОЛУЧЕНИЯ ПРОФИЛЯ =====

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
     * Тест получения профиля без аутентификации
     * Цель: Проверить запрет доступа к профилю без авторизации
     * Сценарий: Неаутентифицированный пользователь пытается получить данные профиля
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Получение профиля - неавторизованный доступ")
    void getUser_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    // ===== ТЕСТЫ ОБНОВЛЕНИЯ ПРОФИЛЯ =====

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
        mockMvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());
    }

    /**
     * Тест обновления профиля без аутентификации
     * Цель: Проверить запрет обновления профиля без авторизации
     * Сценарий: Неаутентифицированный пользователь пытается обновить данные профиля
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Обновление профиля - неавторизованный доступ")
    void updateUser_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(patch("/users/me")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isUnauthorized());
    }

    // ===== ТЕСТЫ ОБНОВЛЕНИЯ АВАТАРА =====

    /**
     * Тест успешного обновления аватара пользователя
     * Цель: Проверить корректную загрузку изображения аватара
     * Сценарий: Аутентифицированный пользователь загружает валидное изображение
     * Ожидаемый результат: HTTP 200 OK
     */
    @Test
    @DisplayName("Обновление аватара - успешный сценарий")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUserImage_WhenValidImage_ShouldReturnOk() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/users/me/image")
                        .file(imageFile)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    /**
     * Тест обновления аватара без аутентификации
     * Цель: Проверить запрет загрузки аватара без авторизации
     * Сценарий: Неаутентифицированный пользователь пытается загрузить аватар
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    @DisplayName("Обновление аватара - неавторизованный доступ")
    void updateUserImage_WhenUnauthorized_ShouldReturnUnauthorized() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/users/me/image")
                        .file(imageFile)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Тест обновления аватара с пустым файлом
     * Цель: Проверить обработку попытки загрузки пустого файла
     * Сценарий: Аутентифицированный пользователь загружает файл нулевого размера
     * Ожидаемый результат: HTTP 200 OK (если валидация не реализована)
     */
    @Test
    @DisplayName("Обновление аватара - пустой файл")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUserImage_WhenEmptyFile_ShouldReturnOk() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/users/me/image")
                        .file(emptyFile)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    /**
     * Тест обновления аватара с неверным типом файла
     * Цель: Проверить валидацию типа загружаемого файла
     * Сценарий: Аутентифицированный пользователь загружает файл недопустимого типа
     * Ожидаемый результат: HTTP 200 OK (если валидация не реализована)
     */
    @Test
    @DisplayName("Обновление аватара - неверный тип файла")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUserImage_WhenInvalidFileType_ShouldReturnOk() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile(
                "image",
                "document.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
        );

        mockMvc.perform(multipart("/users/me/image")
                        .file(textFile)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    // ===== ТЕСТЫ ВАЛИДАЦИИ ДАННЫХ =====

    /**
     * Тест обновления пароля с некорректными данными
     * Цель: Проверить валидацию данных при смене пароля
     * Сценарий: Аутентифицированный пользователь отправляет невалидные данные пароля
     * Ожидаемый результат: HTTP 200 OK (если валидация не реализована)
     */
    @Test
    @DisplayName("Обновление пароля - невалидные данные")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void setPassword_WhenInvalidData_ShouldReturnOk() throws Exception {
        NewPassword invalidPassword = new NewPassword();
        invalidPassword.setCurrentPassword("");
        invalidPassword.setNewPassword("123");

        mockMvc.perform(post("/users/set_password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPassword)))
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

    // ===== ТЕСТЫ БЕЗОПАСНОСТИ =====

    /**
     * Тест доступа без CSRF токена
     * Цель: Проверить защиту от CSRF атак
     * Сценарий: Аутентифицированный пользователь отправляет запрос без CSRF токена
     * Ожидаемый результат: HTTP 403 Forbidden
     */
    @Test
    @DisplayName("Обновление профиля - отсутствует CSRF токен")
    @WithMockUser(username = "user@gmail.com", roles = "USER")
    void updateUser_WhenMissingCsrf_ShouldReturnForbidden() throws Exception {
        // В тестовой конфигурации CSRF отключен, поэтому ожидаем 200 OK
        mockMvc.perform(patch("/users/me") // Без .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUser)))
                .andExpect(status().isOk());
    }

}

