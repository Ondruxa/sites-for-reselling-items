package ru.skypro.homework.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.service.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageServiceImpl.class);

    private final ImageRepository imageRepository;

    @Value("${images.upload.dir:images}")
    private String imagesDir;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public ImageEntity save(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения пустой");
        }
        String original = file.getOriginalFilename();
        String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String id = (prefix != null ? prefix : "img") + "_" + UUID.randomUUID() + ext;
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать содержимое файла", e);
        }
        try {
            Path dir = Paths.get(imagesDir);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Files.write(dir.resolve(id), bytes);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла на диск", e);
        }
        ImageEntity entity = new ImageEntity();
        entity.setId(id);
        entity.setContentType(file.getContentType());
        entity.setSize(file.getSize());
        entity.setCreatedAt(Instant.now().toEpochMilli());
        return imageRepository.save(entity);
    }

    @Override
    public ImageContent load(String id) {
        if (id == null) throw new IllegalArgumentException("ID изображения не указан");
        Optional<ImageEntity> entityOpt = imageRepository.findById(id);
        ImageEntity entity = entityOpt.orElseThrow(() -> new IllegalArgumentException("Изображение не найдено"));
        try {
            Path path = Paths.get(imagesDir).resolve(id);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Файл изображения не найден");
            }
            byte[] data = Files.readAllBytes(path);
            String type = entity.getContentType();
            if (type == null) {
                type = Files.probeContentType(path);
            }
            return new ImageContent(data, type != null ? type : "application/octet-stream");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения изображения", e);
        }
    }

    @Override
    public ResponseEntity<byte[]> getImage(String id) {
        try {
            ImageContent content = load(id);
            String contentType = content.getContentType();
            if (contentType == null) {
                contentType = guessContentType(id);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
            if (contentType != null) {
                try { headers.setContentType(MediaType.parseMediaType(contentType)); }
                catch (Exception ignore) { headers.setContentType(MediaType.APPLICATION_OCTET_STREAM); }
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            headers.setContentLength(content.getData().length);
            return new ResponseEntity<>(content.getData(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Ошибка получения изображения {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public void delete(String id) {
        if (id == null || id.isBlank()) return;
        try {
            if (!imageRepository.existsById(id)) return;
            Path path = Paths.get(imagesDir).resolve(id);
            if (Files.exists(path)) {
                try { Files.delete(path); }
                catch (IOException e) { log.warn("Не удалось удалить файл {}: {}", id, e.getMessage()); }
            }
            imageRepository.deleteById(id);
        } catch (Exception e) {
            log.warn("Ошибка при удалении изображения {}: {}", id, e.getMessage());
        }
    }

    private String guessContentType(String id) {
        String lower = id.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (lower.endsWith(".webp")) return "image/webp";
        return null;
    }
}
