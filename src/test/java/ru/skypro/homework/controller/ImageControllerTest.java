package ru.skypro.homework.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.skypro.homework.service.ImageService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование контроллера изображений")
public class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    private String testImageId;
    private byte[] testImageData;

    @BeforeEach
    void setUp() {
        // Подготовка тестовых данных
        testImageId = "img_123e4567-e89b-12d3-a456-426614174000.jpg";
        testImageData = new byte[]{1, 2, 3, 4, 5};
    }

    @Test
    @DisplayName("Успешное получение изображения по корректному ID")
    void getImage_WithValidId_ShouldReturnImage() {
        // Given - настройка моков
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(testImageData);

        when(imageService.getImage(testImageId)).thenReturn(expectedResponse);

        // When - вызов тестируемого метода
        ResponseEntity<byte[]> actualResponse = imageController.getImage(testImageId);

        // Then - проверки
        assertNotNull(actualResponse, "Ответ не должен быть null");
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode(), "Статус код должен быть 200 OK");
        assertArrayEquals(testImageData, actualResponse.getBody(), "Данные изображения должны совпадать");
        verify(imageService, times(1)).getImage(testImageId);
    }

    @Test
    @DisplayName("Получение 404 при запросе несуществующего изображения")
    void getImage_WithNonExistentId_ShouldReturnNotFound() {
        // Given
        String nonExistentId = "non_existent_image.jpg";
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.notFound().build();

        when(imageService.getImage(nonExistentId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<byte[]> actualResponse = imageController.getImage(nonExistentId);

        // Then
        assertNotNull(actualResponse, "Ответ не должен быть null");
        assertEquals(HttpStatus.NOT_FOUND, actualResponse.getStatusCode(), "Статус код должен быть 404");
        assertNull(actualResponse.getBody(), "Тело ответа должно быть пустым");
        verify(imageService, times(1)).getImage(nonExistentId);
    }

    @Test
    @DisplayName("Получение 500 при внутренней ошибке сервера")
    void getImage_WhenServiceThrowsException_ShouldReturnInternalServerError() {
        // Given
        String problematicId = "problematic_image.jpg";
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.internalServerError().build();

        when(imageService.getImage(problematicId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<byte[]> actualResponse = imageController.getImage(problematicId);

        // Then
        assertNotNull(actualResponse, "Ответ не должен быть null");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode(),
                "Статус код должен быть 500");
        verify(imageService, times(1)).getImage(problematicId);
    }

    @Test
    @DisplayName("Обработка ID изображения с точкой в имени файла")
    void getImage_WithIdContainingDot_ShouldProcessCorrectly() {
        // Given
        String idWithDot = "image.with.dots.jpg";
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok()
                .header("Content-Type", "image/jpeg")
                .body(testImageData);

        when(imageService.getImage(idWithDot)).thenReturn(expectedResponse);

        // When
        ResponseEntity<byte[]> actualResponse = imageController.getImage(idWithDot);

        // Then
        assertNotNull(actualResponse, "Ответ не должен быть null");
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode(), "Статус код должен быть 200 OK");
        verify(imageService, times(1)).getImage(idWithDot);
    }

    @Test
    @DisplayName("Обработка ID изображения с разными расширениями")
    void getImage_WithDifferentExtensions_ShouldHandleCorrectly() {
        // Тестируем различные расширения файлов
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".webp", ".bmp"};

        for (String ext : extensions) {
            String imageId = "test_image" + ext;
            ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok()
                    .body(testImageData);

            when(imageService.getImage(imageId)).thenReturn(expectedResponse);

            ResponseEntity<byte[]> response = imageController.getImage(imageId);

            assertNotNull(response, "Ответ для расширения " + ext + " не должен быть null");
            assertEquals(HttpStatus.OK, response.getStatusCode(),
                    "Статус код для " + ext + " должен быть 200 OK");
        }
    }

    @Test
    @DisplayName("Проверка вызова сервиса верным параметром")
    void getImage_ShouldCallServiceWithCorrectParameter() {
        // Given
        String expectedId = "expected_id.png";
        when(imageService.getImage(expectedId)).thenReturn(ResponseEntity.ok().build());

        // When
        imageController.getImage(expectedId);

        // Then
        verify(imageService, times(1)).getImage(expectedId);
        verifyNoMoreInteractions(imageService);
    }

    @Test
    @DisplayName("Обработка ID со специальными символами")
    void getImage_WithSpecialCharactersInId_ShouldProcessCorrectly() {
        // Given
        String specialId = "image-123_456@test.jpg";
        ResponseEntity<byte[]> expectedResponse = ResponseEntity.ok().build();

        when(imageService.getImage(specialId)).thenReturn(expectedResponse);

        // When
        ResponseEntity<byte[]> response = imageController.getImage(specialId);

        // Then
        assertNotNull(response, "Ответ не должен быть null");
        verify(imageService, times(1)).getImage(specialId);
    }
}
