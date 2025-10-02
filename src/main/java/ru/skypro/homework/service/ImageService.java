package ru.skypro.homework.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.model.ImageEntity;

public interface ImageService {

    ImageEntity save(MultipartFile file, String prefix);

    ImageContent load(String id);

    ResponseEntity<byte[]> getImage(String id);

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
