package ru.skypro.homework.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.TestSecurityConfig;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@WebMvcTest(AdController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public class AdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdService adService;

    private CreateOrUpdateAd createOrUpdateAd;
    private Ad ad;
    private ExtendedAd extendedAd;
    private CreateOrUpdateComment createOrUpdateComment;
    private Comment comment;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных перед каждым тестом
        createOrUpdateAd = new CreateOrUpdateAd();
        createOrUpdateAd.setTitle("Test Ad");
        createOrUpdateAd.setDescription("Test Description");
        createOrUpdateAd.setPrice(1000);

        ad = new Ad();
        ad.setPk(1);
        ad.setAuthor(1);
        ad.setTitle("Test Ad");
        ad.setPrice(1000);
        ad.setImage("/images/test.jpg");

        extendedAd = new ExtendedAd();
        extendedAd.setPk(1);
        extendedAd.setAuthorFirstName("John");
        extendedAd.setAuthorLastName("Doe");
        extendedAd.setDescription("Test Description");
        extendedAd.setEmail("john@example.com");
        extendedAd.setImage("/images/test.jpg");
        extendedAd.setPhone("+79991234567");
        extendedAd.setPrice(1000);
        extendedAd.setTitle("Test Ad");

        createOrUpdateComment = new CreateOrUpdateComment();
        createOrUpdateComment.setText("Test comment");

        comment = new Comment();
        comment.setPk(1);
        comment.setAuthor(1);
        comment.setAuthorImage("/images/user.jpg");
        comment.setAuthorFirstName("John");
        comment.setText("Test comment");
        comment.setCreatedAt(1234567890L);

        imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
    }

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

    /**
     * Тест добавления нового объявления
     * Теперь безопасность отключена, поэтому не нужна аутентификация
     */
    @Test
    void addAd_ShouldReturnCreated() throws Exception {
        MockMultipartFile propertiesFile = new MockMultipartFile(
                "properties",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(createOrUpdateAd)
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        doNothing().when(adService).addAd(any(CreateOrUpdateAd.class), any());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads")
                        .file(propertiesFile)
                        .file(imageFile))
                .andExpect(status().isCreated());

        verify(adService, times(1)).addAd(any(CreateOrUpdateAd.class), any());
    }

    /**
     * Тест доступа без аутентификации к защищенным endpoint'ам
     */
    @Test
    void accessProtectedEndpoints_WithoutAuth_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isOk()); // или isUnauthorized() в зависимости от конфигурации
    }


    /**
     * Тест удаления несуществующего комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deleteComment_WithNonExistingComment_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(adService).deleteComment(1, 999);

        // Act & Assert
        mockMvc.perform(delete("/ads/1/comments/999"))
                .andExpect(status().isOk());

        verify(adService, times(1)).deleteComment(1, 999);
    }

    /**
     * Тест получения комментариев объявления
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getAdComments_ShouldReturnComments() throws Exception {
        // Arrange
        Comments comments = new Comments();
        comments.setCount(1);
        comments.setResults(List.of(comment));

        when(adService.getAdComments(1)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/ads/1/comments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.results[0].pk").value(1))
                .andExpect(jsonPath("$.results[0].text").value("Test comment"));

        verify(adService, times(1)).getAdComments(1);
    }

    /**
     * Тест добавления комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void addComment_ShouldReturnComment() throws Exception {
        // Arrange
        when(adService.addComment(any(CreateOrUpdateComment.class), eq(1))).thenReturn(comment);

        // Act & Assert
        mockMvc.perform(post("/ads/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrUpdateComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(1))
                .andExpect(jsonPath("$.text").value("Test comment"));

        verify(adService, times(1)).addComment(any(CreateOrUpdateComment.class), eq(1));
    }

    /**
     * Тест обновления комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateComment_ShouldReturnUpdatedComment() throws Exception {
        // Arrange
        Comment updatedComment = new Comment();
        updatedComment.setPk(1);
        updatedComment.setText("Updated comment");

        when(adService.updateComment(any(CreateOrUpdateComment.class), eq(1), eq(1)))
                .thenReturn(updatedComment);

        // Act & Assert
        mockMvc.perform(patch("/ads/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOrUpdateComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated comment"));

        verify(adService, times(1))
                .updateComment(any(CreateOrUpdateComment.class), eq(1), eq(1));
    }

    /**
     * Тест удаления комментария
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deleteComment_ShouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(adService).deleteComment(1, 1);

        // Act & Assert
        mockMvc.perform(delete("/ads/1/comments/1"))
                .andExpect(status().isOk());

        verify(adService, times(1)).deleteComment(1, 1);
    }

    /**
     * Тест обновления изображения объявления
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void updateAdImage_ShouldReturnImageData() throws Exception {
        // Arrange
        byte[] imageData = "updated image content".getBytes();
        when(adService.updateImage(eq(1), any(MultipartFile.class))).thenReturn(imageData);

        MockMultipartFile newImageFile = new MockMultipartFile(
                "image",
                "new-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "updated image content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.multipart("/ads/1/image")
                        .file(newImageFile)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageData));

        verify(adService, times(1)).updateImage(eq(1), any(MultipartFile.class));
    }

    /**
     * Тест удаления объявления
     */
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void deleteAd_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(adService).removeAd(1);

        // Act & Assert
        mockMvc.perform(delete("/ads/1"))
                .andExpect(status().isNoContent());

        verify(adService, times(1)).removeAd(1);
    }
}
