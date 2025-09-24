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
import java.util.List;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
public class AdController {

    private final AdService adService;

    @GetMapping
    public List<Ad> getAllAds() {
        return List.of(); // Возвращаем пустой список объявлений
    }

    /**
     * Добавляет новое объявление с изображением.
     *
     * @param properties Объект с информацией об объявлении.
     * @param image Изображение, прикрепленное к объявлению.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addAd(
            @RequestPart("properties") CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image) {
        adService.addAd(properties, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Получение информации об объявлении",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление найдено", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ExtendedAd.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @GetMapping("/{id}")
    public ExtendedAd getAds(@Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id) {
        return null;
    }

    @Operation(summary = "Удаление объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAd(@Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Обновление информации об объявлении",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Объявление обновлено", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Ad.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Ad updateAds(@Valid @RequestBody CreateOrUpdateAd updatedData,
                        @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id) {
        return null;
    }

    @Operation(summary = "Получение комментариев объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарии найдены", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comments.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @GetMapping(value = "/{id}/comments")
    public Comments getComments(@Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id) {
        return null;
    }

    @Operation(summary = "Получение объявлений авторизованного пользователя",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список объявлений найден", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Ads.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @GetMapping("/me")
    public Ads getAdsMe() {
        // Заглушка: возвращает null
        return null;
    }

    @Operation(summary = "Обновление картинки объявления",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Картинка обновлена", content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "application/octet-stream")),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> updateImage(
            @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id,
            @RequestParam("image") MultipartFile file) {
        // Заглушка: просто возвращаем пустой массив байтов
        return ResponseEntity.ok().body(new byte[]{});
    }

    @Operation(summary = "Добавление комментария к объявлению",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий добавлен", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PostMapping(value = "/{id}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Comment addComment(@Valid @RequestBody CreateOrUpdateComment commentData,
                              @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer id) {
        return null;
    }

    @Operation(summary = "Удаление комментария",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий удалён"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer adId,
            @Parameter(description = "Идентификатор комментария", example = "1", required = true) @PathVariable Integer commentId) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновление комментария",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Комментарий обновлён", content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = Comment.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "Not Found")
            })
    @PatchMapping(value = "/{adId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Comment updateComment(@Valid @RequestBody CreateOrUpdateComment updatedData,
                                 @Parameter(description = "Идентификатор объявления", example = "1", required = true) @PathVariable Integer adId,
                                 @Parameter(description = "Идентификатор комментария", example = "1", required = true) @PathVariable Integer commentId) {
        return null;
    }
}
