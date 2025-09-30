package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AdController
 * Тестируем REST API endpoints в полном Spring контексте
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AdControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdService adService;

    // ===== ТЕСТЫ ДЛЯ GET /ads =====

    /**
     * Тест получения всех объявлений когда нет данных
     * Цель: Проверить, что endpoint возвращает пустой список когда объявлений нет
     * Сценарий: Аутентифицированный пользователь запрашивает список объявлений
     * Ожидаемый результат: HTTP 200 OK с пустым массивом в ответе
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAllAds_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(adService.getAllAds()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/ads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(adService, times(1)).getAllAds();
    }

    /**
     * Тест получения всех объявлений с данными
     * Цель: Проверить корректное возвращение списка объявлений
     * Сценарий: Аутентифицированный пользователь запрашивает список объявлений
     * Ожидаемый результат: HTTP 200 OK с массивом объявлений в формате JSON
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAllAds_ShouldReturnAdsList() throws Exception {
        // Arrange
        Ad ad = new Ad();
        ad.setPk(1);
        ad.setTitle("Test Ad");
        when(adService.getAllAds()).thenReturn(List.of(ad));

        // Act & Assert
        mockMvc.perform(get("/ads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pk").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Ad"));

        verify(adService, times(1)).getAllAds();
    }

    // ===== ТЕСТЫ ДЛЯ POST /ads =====

    /**
     * Тест создания нового объявления с валидными данными
     * Цель: Проверить успешное создание объявления с изображением
     * Сценарий: Аутентифицированный пользователь отправляет multipart/form-data запрос
     * Ожидаемый результат: HTTP 201 Created без тела ответа
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addAd_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        CreateOrUpdateAd properties = new CreateOrUpdateAd();
        properties.setTitle("Test Title");
        properties.setDescription("Test Description");
        properties.setPrice(1000);

        MockMultipartFile propertiesFile = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(properties)
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        doNothing().when(adService).addAd(any(CreateOrUpdateAd.class), any());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(propertiesFile)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        verify(adService, times(1)).addAd(any(CreateOrUpdateAd.class), any());
    }

    // ===== ТЕСТЫ ДЛЯ GET /ads/{id} =====

    /**
     * Тест получения конкретного объявления по ID
     * Цель: Проверить корректное возвращение данных объявления
     * Сценарий: Аутентифицированный пользователь запрашивает объявление по существующему ID
     * Ожидаемый результат: HTTP 200 OK с данными объявления в формате JSON
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAd_WithExistingId_ShouldReturnAd() throws Exception {
        // Arrange
        ExtendedAd extendedAd = new ExtendedAd();
        extendedAd.setPk(1);
        extendedAd.setTitle("Test Ad");
        when(adService.getAdById(1)).thenReturn(extendedAd);

        // Act & Assert
        mockMvc.perform(get("/ads/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1))
                .andExpect(jsonPath("$.title").value("Test Ad"));

        verify(adService, times(1)).getAdById(1);
    }

    /**
     * Тест получения несуществующего объявления
     * Цель: Проверить обработку запроса к несуществующему ресурсу
     * Сценарий: Аутентифицированный пользователь запрашивает объявление по несуществующему ID
     * Ожидаемый результат: HTTP 200 OK с пустым телом ответа
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAd_WithNonExistingId_ShouldReturnNull() throws Exception {
        // Arrange
        when(adService.getAdById(999)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/ads/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(adService, times(1)).getAdById(999);
    }

    /**
     * Тест удаления объявления
     * Цель: Проверить успешное удаление объявления
     * Сценарий: Аутентифицированный пользователь удаляет существующее объявление
     * Ожидаемый результат: HTTP 204 No Content без тела ответа
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void removeAd_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(adService).removeAd(1);

        // Act & Assert
        mockMvc.perform(delete("/ads/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(adService, times(1)).removeAd(1);
    }

    // ===== ТЕСТЫ ДЛЯ PATCH /ads/{id} =====

    /**
     * Тест обновления объявления с валидными данными
     * Цель: Проверить успешное обновление данных объявления
     * Сценарий: Аутентифицированный пользователь обновляет данные существующего объявления
     * Ожидаемый результат: HTTP 200 OK с обновленными данными объявления
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateAd_WithValidData_ShouldReturnUpdatedAd() throws Exception {
        // Arrange
        CreateOrUpdateAd updateData = new CreateOrUpdateAd();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setPrice(2000);

        Ad updatedAd = new Ad();
        updatedAd.setPk(1);
        updatedAd.setTitle("Updated Title");

        when(adService.updateAd(any(CreateOrUpdateAd.class), eq(1))).thenReturn(updatedAd);

        // Act & Assert
        mockMvc.perform(patch("/ads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(adService, times(1)).updateAd(any(CreateOrUpdateAd.class), eq(1));
    }

    // ===== ТЕСТЫ ДЛЯ GET /ads/{id}/comments =====

    /**
     * Тест получения комментариев к объявлению
     * Цель: Проверить корректное возвращение списка комментариев
     * Сценарий: Аутентифицированный пользователь запрашивает комментарии к объявлению
     * Ожидаемый результат: HTTP 200 OK с данными комментариев
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getComments_ShouldReturnComments() throws Exception {
        // Arrange
        Comments comments = new Comments();
        when(adService.getAdComments(1)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/ads/1/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adService, times(1)).getAdComments(1);
    }

    // ===== ТЕСТЫ ДЛЯ GET /ads/me =====

    /**
     * Тест получения объявлений текущего пользователя
     * Цель: Проверить корректное возвращение объявлений авторизованного пользователя
     * Сценарий: Аутентифицированный пользователь запрашивает свои объявления
     * Ожидаемый результат: HTTP 200 OK с данными объявлений пользователя
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getUserAds_ShouldReturnUserAds() throws Exception {
        // Arrange
        Ads userAds = new Ads();
        when(adService.getUserAds()).thenReturn(userAds);

        // Act & Assert
        mockMvc.perform(get("/ads/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adService, times(1)).getUserAds();
    }

    // ===== ТЕСТЫ ДЛЯ PATCH /ads/{id}/image =====

    /**
     * Тест обновления изображения объявления
     * Цель: Проверить успешную загрузку нового изображения для объявления
     * Сценарий: Аутентифицированный пользователь загружает новое изображение для объявления
     * Ожидаемый результат: HTTP 200 OK с бинарными данными изображения
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateImage_ShouldReturnImageData() throws Exception {
        // Arrange
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "new-image.jpg",
                "image/jpeg",
                "new image content".getBytes()
        );

        when(adService.updateImage(eq(1), any())).thenReturn(new byte[]{1, 2, 3});

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/ads/1/image")
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[]{1, 2, 3}));

        verify(adService, times(1)).updateImage(eq(1), any());
    }

    // ===== ТЕСТЫ ДЛЯ POST /ads/{id}/comments =====

    /**
     * Тест добавления комментария к объявлению
     * Цель: Проверить успешное создание комментария
     * Сценарий: Аутентифицированный пользователь добавляет комментарий к объявлению
     * Ожидаемый результат: HTTP 200 OK с данными созданного комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addComment_WithValidData_ShouldReturnComment() throws Exception {
        // Arrange
        CreateOrUpdateComment commentData = new CreateOrUpdateComment();
        commentData.setText("Test comment");

        Comment comment = new Comment();
        comment.setPk(1);
        comment.setText("Test comment");

        when(adService.addComment(any(CreateOrUpdateComment.class), eq(1))).thenReturn(comment);

        // Act & Assert
        mockMvc.perform(post("/ads/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1))
                .andExpect(jsonPath("$.text").value("Test comment"));

        verify(adService, times(1)).addComment(any(CreateOrUpdateComment.class), eq(1));
    }

    // ===== ТЕСТЫ ДЛЯ DELETE /ads/{adId}/comments/{commentId} =====

    /**
     * Тест удаления комментария
     * Цель: Проверить успешное удаление комментария
     * Сценарий: Аутентифицированный пользователь удаляет комментарий
     * Ожидаемый результат: HTTP 200 OK без тела ответа
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deleteComment_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(adService).deleteComment(1, 1);

        // Act & Assert
        mockMvc.perform(delete("/ads/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adService, times(1)).deleteComment(1, 1);
    }

    // ===== ТЕСТЫ ДЛЯ PATCH /ads/{adId}/comments/{commentId} =====

    /**
     * Тест обновления комментария
     * Цель: Проверить успешное обновление текста комментария
     * Сценарий: Аутентифицированный пользователь обновляет существующий комментарий
     * Ожидаемый результат: HTTP 200 OK с обновленными данными комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateComment_WithValidData_ShouldReturnUpdatedComment() throws Exception {
        // Arrange
        CreateOrUpdateComment updateData = new CreateOrUpdateComment();
        updateData.setText("Updated comment");

        Comment updatedComment = new Comment();
        updatedComment.setPk(1);
        updatedComment.setText("Updated comment");

        when(adService.updateComment(any(CreateOrUpdateComment.class), eq(1), eq(1))).thenReturn(updatedComment);

        // Act & Assert
        mockMvc.perform(patch("/ads/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1))
                .andExpect(jsonPath("$.text").value("Updated comment"));

        verify(adService, times(1)).updateComment(any(CreateOrUpdateComment.class), eq(1), eq(1));
    }

    // ===== ТЕСТЫ ОБРАБОТКИ ОШИБОК =====

    /**
     * Тест обработки невалидного JSON при создании объявления
     * Цель: Проверить обработку ошибок формата данных
     * Сценарий: Аутентифицированный пользователь отправляет невалидный JSON
     * Ожидаемый результат: HTTP 400 Bad Request
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addAd_WithInvalidJson_ShouldHandleErrors() throws Exception {
        // Arrange
        MockMultipartFile invalidProperties = new MockMultipartFile(
                "properties",
                "",
                "application/json",
                "invalid json".getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(invalidProperties)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест обработки невалидного JSON при обновлении объявления
     * Цель: Проверить обработку ошибок формата данных при обновлении
     * Сценарий: Аутентифицированный пользователь отправляет невалидный JSON для обновления
     * Ожидаемый результат: HTTP 400 Bad Request
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateAd_WithInvalidJson_ShouldHandleErrors() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/ads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ===== ТЕСТЫ CORS =====

    /**
     * Тест CORS политики для localhost:3000
     * Цель: Проверить корректную настройку CORS
     * Сценарий: Запрос с Origin http://localhost:3000
     * Ожидаемый результат: HTTP 200 OK с CORS заголовками
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void cors_ShouldAllowLocalhost3000() throws Exception {
        // Arrange
        when(adService.getAllAds()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/ads")
                        .header("Origin", "http://localhost:3000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    // ===== ТЕСТЫ БЕЗОПАСНОСТИ =====

    /**
     * Тест доступа без аутентификации
     * Цель: Проверить, что неаутентифицированные пользователи не имеют доступа
     * Сценарий: Неаутентифицированный пользователь пытается получить список объявлений
     * Ожидаемый результат: HTTP 401 Unauthorized
     */
    @Test
    void getAllAds_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/ads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
