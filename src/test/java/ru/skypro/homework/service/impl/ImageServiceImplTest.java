package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.repository.ImageRepository;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тестирование ImageServiceImpl")
public class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    private ImageServiceImpl imageService;

    @TempDir
    Path tempDir;

    private MultipartFile testFile;
    private byte[] testFileContent;

    @BeforeEach
    void setUp() throws Exception {
        imageService = new ImageServiceImpl(imageRepository);

        // Устанавливаем imagesDir через reflection
        setImagesDir(tempDir.toString());

        testFileContent = new byte[]{1, 2, 3, 4, 5};
        testFile = new MockMultipartFile(
                "test-image.jpg",
                "test-image.jpg",
                "image/jpeg",
                testFileContent
        );
    }

    private void setImagesDir(String imagesDir) throws Exception {
        Field imagesDirField = ImageServiceImpl.class.getDeclaredField("imagesDir");
        imagesDirField.setAccessible(true);
        imagesDirField.set(imageService, imagesDir);
    }

    @Test
    @DisplayName("save - успешное сохранение изображения")
    void save_WithValidFile_ShouldSaveSuccessfully() throws Exception {
        // Given
        String prefix = "user";
        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ImageEntity result = imageService.save(testFile, prefix);

        // Then
        assertNotNull(result, "Результат не должен быть null");
        assertNotNull(result.getId(), "ID не должен быть null");
        assertTrue(result.getId().startsWith(prefix + "_"), "ID должен начинаться с префикса");
        assertEquals("image/jpeg", result.getContentType(), "Content-Type должен совпадать");
        assertEquals(testFile.getSize(), result.getSize(), "Размер должен совпадать");
        assertNotNull(result.getCreatedAt(), "Время создания не должно быть null");

        // Проверяем, что файл сохранен на диск
        Path savedFile = tempDir.resolve(result.getId());
        assertTrue(Files.exists(savedFile), "Файл должен быть сохранен на диск");
        assertArrayEquals(testFileContent, Files.readAllBytes(savedFile), "Содержимое файла должно совпадать");

        verify(imageRepository, times(1)).save(any(ImageEntity.class));
    }

    @Test
    @DisplayName("save - пустой файл должен выбрасывать исключение")
    void save_WithEmptyFile_ShouldThrowException() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "empty.jpg",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> imageService.save(emptyFile, "test"));
        assertEquals("Файл изображения пустой", exception.getMessage());
    }

    @Test
    @DisplayName("save - null файл должен выбрасывать исключение")
    void save_WithNullFile_ShouldThrowException() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> imageService.save(null, "test"));
        assertEquals("Файл изображения пустой", exception.getMessage());
    }
    @Test
    @DisplayName("save - файл без расширения")
    void save_FileWithoutExtension_ShouldWorkCorrectly() {
        // Given
        MultipartFile noExtensionFile = new MockMultipartFile(
                "noextension",
                "noextension",
                "image/jpeg",
                testFileContent
        );
        when(imageRepository.save(any(ImageEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ImageEntity result = imageService.save(noExtensionFile, "test");

        // Then
        assertNotNull(result.getId());
        assertFalse(result.getId().contains("."), "ID не должен содержать точку если нет расширения");
    }

    @Test
    @DisplayName("load - успешная загрузка существующего изображения")
    void load_WithExistingImage_ShouldReturnImageContent() throws IOException {
        // Given
        String imageId = "test-image.jpg";
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setContentType("image/jpeg");
        imageEntity.setSize(5L);

        // Создаем тестовый файл на диске
        Path imagePath = tempDir.resolve(imageId);
        Files.write(imagePath, testFileContent);

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        // When
        ImageServiceImpl.ImageContent result = imageService.load(imageId);

        // Then
        assertNotNull(result, "Результат не должен быть null");
        assertArrayEquals(testFileContent, result.getData(), "Данные должны совпадать");
        assertEquals("image/jpeg", result.getContentType(), "Content-Type должен совпадать");
        verify(imageRepository, times(1)).findById(imageId);
    }

    @Test
    @DisplayName("load - изображение не найдено в БД")
    void load_WithNonExistentImageInDb_ShouldThrowException() {
        // Given
        String imageId = "non-existent.jpg";
        when(imageRepository.findById(imageId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> imageService.load(imageId));
        assertEquals("Изображение не найдено", exception.getMessage());
        verify(imageRepository, times(1)).findById(imageId);
    }

    @Test
    @DisplayName("load - файл не найден")
    void load_WhenFileNotExistsOnDisk_ShouldThrowException() {
        // Given
        String imageId = "missing-file.jpg";
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setId(imageId);
        imageEntity.setContentType("image/jpeg");

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(imageEntity));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> imageService.load(imageId));
        assertEquals("Файл изображения не найден", exception.getMessage());
    }

    @Test
    @DisplayName("delete - успешное удаление существующего изображения")
    void delete_WithExistingImage_ShouldDeleteSuccessfully() throws IOException {
        // Given
        String imageId = "image-to-delete.jpg";

        // Создаем файл на диске
        Path imagePath = tempDir.resolve(imageId);
        Files.write(imagePath, testFileContent);

        when(imageRepository.existsById(imageId)).thenReturn(true);

        // When
        imageService.delete(imageId);

        // Then
        assertFalse(Files.exists(imagePath), "Файл должен быть удален с диска");
        verify(imageRepository, times(1)).existsById(imageId);
        verify(imageRepository, times(1)).deleteById(imageId);
    }

    @Test
    @DisplayName("delete - null или пустой ID")
    void delete_WithNullOrEmptyId_ShouldDoNothing() {
        // Given & When & Then - null ID
        imageService.delete(null);
        verify(imageRepository, never()).existsById(any());

        // Given & When & Then - пустой ID
        imageService.delete("");
        verify(imageRepository, never()).existsById(any());

        // Given & When & Then - ID с пробелами
        imageService.delete("   ");
        verify(imageRepository, never()).existsById(any());
    }


}
