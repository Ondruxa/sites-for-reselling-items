package ru.skypro.homework.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.ImageEntity;

public interface ImageService {
    /**
     * Сохранить изображение и вернуть сохранённую сущность ImageEntity.
     * В fs режиме data=null, метаданные заполняются; в db режиме data хранит байты.
     */
    ImageEntity save(MultipartFile file, String prefix);

    /**
     * Получить байты изображения (используется внутренне, если нужно напрямую работать с содержимым).
     * @param id идентификатор изображения
     * @return объект с данными
     */
    ImageContent load(String id);

    /**
     * Вернуть готовый HTTP-ответ с изображением (инкапсулирует всю логику формирования заголовков).
     */
    ResponseEntity<byte[]> getImage(String id);

    /**
     * Флаг: режим хранения в БД.
     */
    boolean isDbMode();

    /**
     * Удалить изображение по id (файл или запись в БД).
     */
    void delete(String id);

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
