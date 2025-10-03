package ru.skypro.homework.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.ImageEntity;

/**
 * Сервис работы с изображениями
 * <p>
 * Отвечает за приём multipart-файлов, их сохранение на диск, хранение
 * метаданных и выдачу бинарных данных
 * по HTTP. Также обеспечивает удаление файла и соответствующей записи.
 * </p>
 */
public interface ImageService {

    /**
     * Сохраняет файл изображения и метаданные.
     * @param file multipart файл
     * @param prefix префикс для имени (например ad_{id}, user_{id})
     * @return сохранённая сущность ImageEntity
     */
    ImageEntity save(MultipartFile file, String prefix);

    /**
     * Загружает бинарные данные изображения в память.
     * @param id идентификатор (имя файла)
     * @return объект с байтами и content-type
     */
    ImageContent load(String id);

    /**
     * Формирует HTTP-ответ с байтами изображения и корректными заголовками.
     * @param id идентификатор изображения
     * @return 200 OK или 404 / 500 при ошибке
     */
    ResponseEntity<byte[]> getImage(String id);

    /**
     * Удаляет файл и метаданные.
     * @param id идентификатор изображения
     */
    void delete(String id);

    /**
     * Контейнер бинарных данных и медиатипа.
     */
    class ImageContent {
        private final byte[] data;
        private final String contentType;
        public ImageContent(byte[] data, String contentType) {
            this.data = data;
            this.contentType = contentType;
        }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
    }
}
