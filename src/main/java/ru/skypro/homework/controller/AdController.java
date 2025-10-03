package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdService;

import javax.validation.Valid;

/**
 * REST-контроллер для управления объявлениями (ads).
 * <p>
 * Предоставляет операции CRUD над объявлениями, загрузку/обновление изображения
 * и работу с комментариями (создание, чтение, обновление, удаление).
 * </p>
 * Особенности:
 * <ul>
 *   <li>Публичный доступ: GET /ads (список), GET /ads/{id} (карточка).</li>
 *   <li>Требует аутентификацию: создание/редактирование/удаление объявлений и все операции с комментариями.</li>
 *   <li>Изображения объявлений возвращаются как URL (поле image в DTO) вида /images/{id}.</li>
 * </ul>
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;

    /**
     * Получить список всех объявлений.
     * @return обёртка {@link Ads} (count + results)
     */
    @GetMapping
    public Ads getAllAds() {
        return adService.getAllAds();
    }

    /**
     * Создать новое объявление с изображением (multipart/form-data).
     * @param properties данные объявления
     * @param image файл изображения (обязателен согласно спецификации)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addAd(
            @RequestPart("properties") CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image) {
        adService.addAd(properties, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Получить объявление по id.
     * @param id идентификатор объявления
     * @return {@link ExtendedAd}
     */
    @Operation(summary = "Получение информации об объявлении",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление найдено", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExtendedAd.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @GetMapping("/{id}")
    public ExtendedAd getAds(@PathVariable Integer id) {
        return adService.getAdById(id);
    }

    /**
     * Удалить объявление (только его владелец или ADMIN).
     * @param id идентификатор объявления
     */
    @Operation(summary = "Удаление объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAd(@PathVariable Integer id) {
        adService.removeAd(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Частичное обновление (PATCH) основных полей объявления.
     * @param updatedData новые данные
     * @param id идентификатор объявления
     * @return обновлённое краткое представление {@link Ad}
     */
    @Operation(summary = "Обновление информации об объявлении",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление обновлено", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Ad.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Ad updateAds(@Valid @RequestBody CreateOrUpdateAd updatedData, @PathVariable Integer id) {
        return adService.updateAd(updatedData, id);
    }

    /**
     * Получить список комментариев к объявлению.
     * @param id идентификатор объявления
     * @return обёртка {@link Comments}
     */
    @Operation(summary = "Получение комментариев объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарии найдены", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comments.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @GetMapping(value = "/{id}/comments")
    public Comments getComments(@PathVariable Integer id) {
        return adService.getAdComments(id);
    }

    /**
     * Получить объявления текущего пользователя.
     * @return {@link Ads}
     */
    @Operation(summary = "Получение объявлений авторизованного пользователя",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список объявлений найден", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Ads.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @GetMapping("/me")
    public Ads getAdsMe() {
        return adService.getUserAds();
    }

    /**
     * Обновить изображение объявления (заменяет старое и старое удаляет).
     * @param id идентификатор объявления
     * @param file новое изображение
     * @return обновлённое объявление {@link Ad}
     */
    @Operation(summary = "Обновление картинки объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Картинка обновлена", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Ad.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> updateImage(
            @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id,
            @RequestParam("image") MultipartFile file) {
        return ResponseEntity.ok(adService.updateImage(id, file));
    }

    /**
     * Добавить новый комментарий к объявлению.
     * @param commentData тело комментария
     * @param id идентификатор объявления
     * @return созданный комментарий
     */
    @Operation(summary = "Добавление комментария к объявлению",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий добавлен", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Comment addComment(@Valid @RequestBody CreateOrUpdateComment commentData, @PathVariable Integer id) {
        return adService.addComment(commentData, id);
    }

    /**
     * Удалить комментарий (владелец комментария или ADMIN).
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     */
    @Operation(summary = "Удаление комментария",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий удалён"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Integer adId, @PathVariable Integer commentId) {
        adService.deleteComment(adId, commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * Обновление текста комментария.
     * @param updatedData новые данные
     * @param adId идентификатор объявления
     * @param commentId идентификатор комментария
     * @return обновлённый комментарий
     */
    @Operation(summary = "Обновление комментария",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий обновлён", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{adId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Comment updateComment(@Valid @RequestBody CreateOrUpdateComment updatedData, @PathVariable Integer adId, @PathVariable Integer commentId) {
        return adService.updateComment(updatedData, adId, commentId);
    }
}
