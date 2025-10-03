package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.service.ImageService;

/**
 * Контроллер отдачи бинарных данных изображений.
 * <p>
 * Возвращает байтовое содержимое файла изображения по его id (имя файла),
 * который хранится как метаданные в таблице <code>images</code>. Сами файлы расположены
 * в файловой системе (директория, заданная через свойство <code>images.upload.dir</code>).
 * </p>
 * Особенности:
 * <ul>
 *   <li>Не требует авторизации (путь /images/** whitelisted в {@link ru.skypro.homework.config.WebSecurityConfig}).</li>
 *   <li>Автоматически определяет Content-Type (если не указан в метаданных — пробует по расширению / probeContentType).</li>
 *   <li>При отсутствии изображения возвращает 404.</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    /**
     * Получить бинарное содержимое изображения по id.
     * @param id имя файла (идентификатор, например ad_10_...uuid....jpg)
     * @return 200 OK и байты файла либо 404 Not Found
     */
    @GetMapping("/images/{id:.+}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        return imageService.getImage(id);
    }
}
