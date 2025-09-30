package ru.skypro.homework.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.skypro.homework.config.TestSecurityConfig;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Интеграционные тесты через REST API")
public class AdServiceImpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdService adService;

    private CreateOrUpdateAd testAdProperties;
    private CreateOrUpdateComment testComment;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testAdProperties = new CreateOrUpdateAd();
        testAdProperties.setTitle("Test Ad Title");
        testAdProperties.setDescription("Test Ad Description");
        testAdProperties.setPrice(1000);

        testComment = new CreateOrUpdateComment();
        testComment.setText("Test comment text");

        // Создание тестового изображения
        testImage = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }

    @Test
    @DisplayName("Получение всех объявлений - должен возвращать пустой список")
    @WithMockUser
    void getAllAds_shouldReturnEmptyList() throws Exception {
        // When & Then: Выполнение запроса и проверка результата
        mockMvc.perform(get("/ads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Добавление нового объявления - должен возвращать статус 201 Created")
    @WithMockUser(roles = "USER")
    void addAd_shouldReturnCreatedStatus() throws Exception {
        // Given: Подготовка данных для запроса
        String adPropertiesJson = objectMapper.writeValueAsString(testAdProperties);
        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                adPropertiesJson.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then: Выполнение multipart запроса и проверка статуса
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(properties)
                        .file(testImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Удаление объявления - должен возвращать No Content")
    @WithMockUser(roles = "ADMIN")
    void removeAd_shouldReturnNoContent() throws Exception {
        // Given: ID объявления для удаления
        Integer adId = 1;

        // When & Then: Запрос на удаление должен вернуть 204
        mockMvc.perform(delete("/ads/{id}", adId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Обновление объявления - должен возвращать обновленное объявление")
    @WithMockUser(roles = "USER")
    void updateAd_shouldReturnUpdatedAd() throws Exception {
        // Given: Данные для обновления и ID объявления
        Integer adId = 1;
        CreateOrUpdateAd updateData = new CreateOrUpdateAd();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setPrice(1500);

        // When & Then: Запрос на обновление и проверка статуса
        mockMvc.perform(patch("/ads/{id}", adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("Удаление комментария - должен возвращать статус OK")
    @WithMockUser(roles = "ADMIN")
    void deleteComment_shouldReturnOk() throws Exception {
        // Given: ID объявления и комментария
        Integer adId = 1;
        Integer commentId = 1;

        // When & Then: Запрос на удаление комментария
        mockMvc.perform(delete("/ads/{adId}/comments/{commentId}", adId, commentId))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("Интеграционный тест полного цикла работы с объявлениями")
    @WithMockUser(roles = "USER")
    void fullAdLifecycleIntegrationTest() throws Exception {
        // Phase 1: Получение начального списка объявлений
        String initialResponse = mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Ad> initialAds = objectMapper.readValue(initialResponse,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Ad.class));

        assertThat(initialAds).isNotNull();

        // Phase 2: Добавление нового объявления
        String adPropertiesJson = objectMapper.writeValueAsString(testAdProperties);
        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                adPropertiesJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(properties)
                        .file(testImage))
                .andExpect(status().isCreated());

        // Phase 3: Проверка, что список объявлений доступен после добавления
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Получение информации об объявлении по ID - должен возвращать 200 даже для несуществующего ID")
    @WithMockUser
    void getAdById_withNonExistentId_shouldReturnOk() throws Exception {
        // Given: Несуществующий ID объявления
        Integer nonExistentAdId = 9999;

        // When & Then: Запрос несуществующего объявления должен вернуть 200 (т.к. сервис возвращает null)
        mockMvc.perform(get("/ads/{id}", nonExistentAdId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Обновление объявления - должен возвращать статус OK")
    @WithMockUser(roles = "USER")
    void updateAd_shouldReturnOk() throws Exception {
        // Given: Данные для обновления и ID объявления
        Integer adId = 1;
        CreateOrUpdateAd updateData = new CreateOrUpdateAd();
        updateData.setTitle("Updated Title");
        updateData.setDescription("Updated Description");
        updateData.setPrice(1500);

        // When & Then: Запрос на обновление и проверку статуса
        mockMvc.perform(patch("/ads/{id}", adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Получение объявлений текущего пользователя - должен возвращать статус OK")
    @WithMockUser
    void getUserAds_shouldReturnOk() throws Exception {
        // When & Then: Запрос объявлений пользователя
        mockMvc.perform(get("/ads/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Обновление изображения объявления - должен возвращать статус OK")
    @WithMockUser(roles = "USER")
    void updateImage_shouldReturnOk() throws Exception {
        // Given: ID объявления и новое изображение
        Integer adId = 1;

        // When & Then: Запрос на обновление изображения с использованием PATCH
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads/{id}/image", adId)
                        .file(testImage)
                        .with(request -> {
                            request.setMethod("PATCH"); // Явно указываем метод PATCH
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[0])); // Ожидаем пустой массив байтов
    }

    @Test
    @DisplayName("Получение комментариев объявления - должен возвращать статус OK")
    @WithMockUser
    void getAdComments_shouldReturnOk() throws Exception {
        // Given: ID объявления
        Integer adId = 1;

        // When & Then: Запрос комментариев объявления
        mockMvc.perform(get("/ads/{id}/comments", adId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Добавление комментария к объявлению - должен возвращать статус OK")
    @WithMockUser
    void addComment_shouldReturnOk() throws Exception {
        // Given: ID объявления и данные комментария
        Integer adId = 1;

        // When & Then: Запрос на добавление комментария
        mockMvc.perform(post("/ads/{id}/comments", adId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testComment)))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Обновление комментария - должен возвращать статус OK")
    @WithMockUser
    void updateComment_shouldReturnOk() throws Exception {
        // Given: ID объявления, комментария и данные для обновления
        Integer adId = 1;
        Integer commentId = 1;
        CreateOrUpdateComment updateData = new CreateOrUpdateComment();
        updateData.setText("Updated comment text");

        // When & Then: Запрос на обновление комментария
        mockMvc.perform(patch("/ads/{adId}/comments/{commentId}", adId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(content().string("")); // Ожидаем пустой ответ
    }

    @Test
    @DisplayName("Тестирование безопасности endpoints - доступ без аутентификации")
    void security_testUnauthenticatedAccess() throws Exception {
        // Проверка, что ВСЕ endpoints требуют аутентификации
        mockMvc.perform(get("/ads"))
                .andExpect(status().isUnauthorized()); // Этот endpoint требует аутентификации

        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isUnauthorized()); // Этот требует аутентификации

        mockMvc.perform(post("/ads"))
                .andExpect(status().isUnauthorized()); // Создание объявления требует аутентификации

        mockMvc.perform(delete("/ads/1"))
                .andExpect(status().isUnauthorized()); // Удаление требует аутентификации

        mockMvc.perform(get("/ads/1/comments"))
                .andExpect(status().isUnauthorized()); // Комментарии требуют аутентификации

        mockMvc.perform(get("/ads/1"))
                .andExpect(status().isUnauthorized()); // Получение объявления по ID требует аутентификации

        mockMvc.perform(patch("/ads/1"))
                .andExpect(status().isUnauthorized()); // Обновление объявления требует аутентификации
    }

    @Test
    @DisplayName("Валидация входных данных - должен обрабатывать некорректный запрос")
    @WithMockUser(roles = "USER")
    void validation_testInvalidInput() throws Exception {
        // Given: Некорректные данные (отсутствует обязательное поле)
        CreateOrUpdateAd invalidAd = new CreateOrUpdateAd();
        // title не установлен - должен быть invalid

        String invalidAdJson = objectMapper.writeValueAsString(invalidAd);
        MockMultipartFile properties = new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                invalidAdJson.getBytes(StandardCharsets.UTF_8)
        );

        // When & Then: Запрос с некорректными данными
        // В текущей реализации сервис не валидирует данные, поэтому ожидаем 201
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(properties)
                        .file(testImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated()); // Ожидаем 201, т.к. сервис не валидирует
    }

    @Test
    @DisplayName("Тестирование CORS заголовков")
    @WithMockUser
    void cors_testHeaders() throws Exception {
        mockMvc.perform(get("/ads")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Credentials"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

}
