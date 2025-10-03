package ru.skypro.homework.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.skypro.homework.config.TestSecurityConfig;
import ru.skypro.homework.service.ImageService;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ImageController.class)
@Import(TestSecurityConfig.class)
@DisplayName("Интеграционное тестирование ImageController")
public class ImageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ImageService imageService;

    @Test
    @DisplayName("GET /images/{id} - успешное получение изображения")
    @WithMockUser
        // Добавляем мок-пользователя
    void getImage_IntegrationTest() throws Exception {
        // Given
        String imageId = "test.jpg";
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};

        when(imageService.getImage(anyString()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageData));

        // When & Then
        mockMvc.perform(get("/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));
    }

    @Test
    @DisplayName("GET /images/{id} - изображение не найдено")
    @WithMockUser
    void getImage_NotFound_IntegrationTest() throws Exception {
        // Given
        String nonExistentId = "nonexistent.jpg";

        when(imageService.getImage(anyString()))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(get("/images/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /images/{id} - внутренняя ошибка сервера")
    @WithMockUser
    void getImage_InternalServerError_IntegrationTest() throws Exception {
        // Given
        String problematicId = "problematic.jpg";

        when(imageService.getImage(anyString()))
                .thenReturn(ResponseEntity.internalServerError().build());

        // When & Then
        mockMvc.perform(get("/images/{id}", problematicId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("GET /images/{id} - доступ без аутентификации запрещен")
    void getImage_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then - без @WithMockUser
        mockMvc.perform(get("/images/{id}", "test.jpg"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /images/{id} - доступ с различными типами аутентификации")
    @WithMockUser(username = "testuser", authorities = {"ROLE_USER"})
    void getImage_WithSpecificUser_ShouldWork() throws Exception {
        // Given
        String imageId = "user-specific.jpg";
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};

        when(imageService.getImage(anyString()))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageData));

        // When & Then
        mockMvc.perform(get("/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "image_with_underscore.jpg",
            "image-with-dash.png",
            "image.with.dots.gif",
            "IMAGE_UPPERCASE.JPEG",
            "12345.jpg",
            "image with spaces.jpg",
            "special@chars#image.png"
    })
    @WithMockUser
    @DisplayName("GET /images/{id} - обработка различных форматов ID изображения")
    void getImage_WithVariousIdFormats_ShouldProcessCorrectly(String imageId) throws Exception {
        // Given
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};

        when(imageService.getImage(imageId))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageData));

        // When & Then
        mockMvc.perform(get("/images/{id}", imageId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));

        verify(imageService, times(1)).getImage(imageId);
    }

    @Test
    @DisplayName("GET /images/{id} - пустой ID изображения")
    @WithMockUser
    void getImage_WithEmptyId_ShouldReturnNotFound() throws Exception {
        // Given
        String emptyId = "";

        when(imageService.getImage(emptyId))
                .thenReturn(ResponseEntity.notFound().build());

        // When & Then
        mockMvc.perform(get("/images/{id}", emptyId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /images/{id} - очень длинный ID изображения")
    @WithMockUser
    void getImage_WithVeryLongId_ShouldProcessCorrectly() throws Exception {
        // Given
        String longId = "a".repeat(255) + ".jpg";
        byte[] imageData = new byte[]{1, 2, 3, 4, 5};

        when(imageService.getImage(longId))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageData));

        // When & Then
        mockMvc.perform(get("/images/{id}", longId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(imageData));
    }

    @Test
    @DisplayName("GET /images/{id} - проверка вызова сервиса с правильным ID")
    @WithMockUser
    void getImage_ShouldCallServiceWithCorrectId() throws Exception {
        // Given
        String expectedId = "expected-image-id.jpg";

        when(imageService.getImage(expectedId))
                .thenReturn(ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new byte[]{1, 2, 3}));

        // When
        mockMvc.perform(get("/images/{id}", expectedId));

        // Then
        verify(imageService, times(1)).getImage(expectedId);
        verifyNoMoreInteractions(imageService);
    }
}
