package ru.skypro.homework.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.skypro.homework.model.ImageEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("Тестирование ImageRepository")
public class ImageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ImageRepository imageRepository;

    private ImageEntity testImage1;
    private ImageEntity testImage2;

    @BeforeEach
    void setUp() {
        // Очистка базы данных перед каждым тестом
        imageRepository.deleteAll();

        // Создание тестовых данных
        testImage1 = new ImageEntity();
        testImage1.setId("img_123e4567-e89b-12d3-a456-426614174001.jpg");
        testImage1.setContentType("image/jpeg");
        testImage1.setSize(1024L);
        testImage1.setCreatedAt(System.currentTimeMillis());

        testImage2 = new ImageEntity();
        testImage2.setId("img_123e4567-e89b-12d3-a456-426614174002.png");
        testImage2.setContentType("image/png");
        testImage2.setSize(2048L);
        testImage2.setCreatedAt(System.currentTimeMillis() + 1000);

        imageRepository.saveAll(List.of(testImage1, testImage2));
    }

    @Test
    @DisplayName("findById - поиск существующего изображения по ID")
    void findById_WithExistingId_ShouldReturnImage() {
        // When
        Optional<ImageEntity> result = imageRepository.findById(testImage1.getId());

        // Then
        assertTrue(result.isPresent(), "Изображение должно быть найдено");
        assertEquals(testImage1.getId(), result.get().getId());
        assertEquals(testImage1.getContentType(), result.get().getContentType());
        assertEquals(testImage1.getSize(), result.get().getSize());
        assertEquals(testImage1.getCreatedAt(), result.get().getCreatedAt());
    }

    @Test
    @DisplayName("findById - поиск несуществующего изображения по ID")
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Given
        String nonExistingId = "non-existing-id.jpg";

        // When
        Optional<ImageEntity> result = imageRepository.findById(nonExistingId);

        // Then
        assertFalse(result.isPresent(), "Изображение не должно быть найдено");
    }

    @Test
    @DisplayName("save - сохранение нового изображения")
    void save_WithNewImage_ShouldPersistCorrectly() {
        // Given
        ImageEntity newImage = new ImageEntity();
        newImage.setId("new_image_123.jpg");
        newImage.setContentType("image/gif");
        newImage.setSize(512L);
        newImage.setCreatedAt(System.currentTimeMillis());

        // When
        ImageEntity savedImage = imageRepository.save(newImage);

        // Then
        assertNotNull(savedImage, "Сохраненное изображение не должно быть null");
        assertEquals(newImage.getId(), savedImage.getId());
        assertEquals(newImage.getContentType(), savedImage.getContentType());
        assertEquals(newImage.getSize(), savedImage.getSize());
        assertEquals(newImage.getCreatedAt(), savedImage.getCreatedAt());

        // Проверяем, что изображение действительно сохранено в БД
        Optional<ImageEntity> retrievedImage = imageRepository.findById(newImage.getId());
        assertTrue(retrievedImage.isPresent(), "Изображение должно быть найдено в БД после сохранения");
        assertEquals(newImage.getId(), retrievedImage.get().getId());
    }

    @Test
    @DisplayName("save - обновление существующего изображения")
    void save_WithExistingImage_ShouldUpdateCorrectly() {
        // Given
        String updatedContentType = "image/webp";
        Long updatedSize = 4096L;

        testImage1.setContentType(updatedContentType);
        testImage1.setSize(updatedSize);

        // When
        ImageEntity updatedImage = imageRepository.save(testImage1);

        // Then
        assertNotNull(updatedImage);
        assertEquals(testImage1.getId(), updatedImage.getId());
        assertEquals(updatedContentType, updatedImage.getContentType());
        assertEquals(updatedSize, updatedImage.getSize());

        // Проверяем, что изменения сохранены в БД
        Optional<ImageEntity> retrievedImage = imageRepository.findById(testImage1.getId());
        assertTrue(retrievedImage.isPresent());
        assertEquals(updatedContentType, retrievedImage.get().getContentType());
        assertEquals(updatedSize, retrievedImage.get().getSize());
    }

    @Test
    @DisplayName("existsById - проверка существования изображения")
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When
        boolean exists = imageRepository.existsById(testImage1.getId());

        // Then
        assertTrue(exists, "Должно вернуть true для существующего ID");
    }

    @Test
    @DisplayName("existsById - проверка несуществующего изображения")
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // Given
        String nonExistingId = "non-existing-id.png";

        // When
        boolean exists = imageRepository.existsById(nonExistingId);

        // Then
        assertFalse(exists, "Должно вернуть false для несуществующего ID");
    }

    @Test
    @DisplayName("existsById - должен вернуть false для несуществующего ID")
    void existsById_WithNonExistentId_ShouldReturnFalse() {
        // Given
        Long nonExistentId = 999L;

        // When
        boolean exists = imageRepository.existsById(String.valueOf(nonExistentId));

        // Then
        assertFalse(exists, "Должно вернуть false для несуществующего ID");
    }

    @Test
    @DisplayName("deleteById - удаление существующего изображения")
    void deleteById_WithExistingId_ShouldDeleteImage() {
        // Given
        String imageId = testImage1.getId();

        // When
        imageRepository.deleteById(imageId);

        // Then
        Optional<ImageEntity> deletedImage = imageRepository.findById(imageId);
        assertFalse(deletedImage.isPresent(), "Изображение должно быть удалено из БД");

        boolean exists = imageRepository.existsById(imageId);
        assertFalse(exists, "existsById должен вернуть true после удаления");
    }

    @Test
    @DisplayName("findAll - получение всех изображений")
    void findAll_ShouldReturnAllImages() {
        // When
        List<ImageEntity> images = imageRepository.findAll();

        // Then
        assertNotNull(images, "Список не должен быть null");
        assertEquals(2, images.size(), "Должно вернуть 2 изображения");

        // Проверяем, что оба тестовых изображения присутствуют
        List<String> imageIds = images.stream()
                .map(ImageEntity::getId)
                .collect(Collectors.toList());

        assertTrue(imageIds.contains(testImage1.getId()));
        assertTrue(imageIds.contains(testImage2.getId()));
    }

    @Test
    @DisplayName("findAll - получение всех изображений из пустой БД")
    void findAll_WithEmptyDatabase_ShouldReturnEmptyList() {
        // Given
        imageRepository.deleteAll();

        // When
        List<ImageEntity> images = imageRepository.findAll();

        // Then
        assertNotNull(images, "Список не должен быть null");
        assertTrue(images.isEmpty(), "Список должен быть пустым");
    }

    @Test
    @DisplayName("count - подсчет количества изображений")
    void count_ShouldReturnCorrectCount() {
        // When
        long count = imageRepository.count();

        // Then
        assertEquals(2, count, "Должно вернуть 2 изображения");
    }

    @Test
    @DisplayName("count - подсчет в пустой БД")
    void count_WithEmptyDatabase_ShouldReturnZero() {
        // Given
        imageRepository.deleteAll();

        // When
        long count = imageRepository.count();

        // Then
        assertEquals(0, count, "Должно вернуть 0 для пустой БД");
    }

    @Test
    @DisplayName("deleteAll - удаление всех изображений")
    void deleteAll_ShouldRemoveAllImages() {
        // When
        imageRepository.deleteAll();

        // Then
        List<ImageEntity> images = imageRepository.findAll();
        assertTrue(images.isEmpty(), "Все изображения должны быть удалены");
        assertEquals(0, imageRepository.count(), "Количество должно быть 0");
    }

    @Test
    @DisplayName("deleteAll - удаление конкретных изображений")
    void deleteAll_WithSpecificImages_ShouldRemoveOnlyThose() {
        // When
        imageRepository.deleteAll(List.of(testImage1));

        // Then
        Optional<ImageEntity> remainingImage = imageRepository.findById(testImage2.getId());
        assertTrue(remainingImage.isPresent(), "Второе изображение должно остаться в БД");

        Optional<ImageEntity> deletedImage = imageRepository.findById(testImage1.getId());
        assertFalse(deletedImage.isPresent(), "Первое изображение должно быть удалено");

        assertEquals(1, imageRepository.count(), "Должно остаться 1 изображение");
    }
}
