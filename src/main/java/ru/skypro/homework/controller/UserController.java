package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.service.UserService;

/**
 * Контроллер управления профилем текущего авторизованного пользователя.
 * <p>
 * Предоставляет операции:
 * <ul>
 *   <li>Смена пароля (/users/set_password)</li>
 *   <li>Получение профиля (/users/me)</li>
 *   <li>Частичное обновление профиля (/users/me)</li>
 *   <li>Обновление аватара (/users/me/image)</li>
 * </ul>
 * </p>
 */
@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Изменение пароля текущего пользователя.
     * @param newPassword DTO с текущим и новым паролем
     */
    @Operation(summary = "Установка нового пароля пользователя")
    @PostMapping("/set_password")
    public void setPassword(@RequestBody NewPassword newPassword) {
        userService.setPassword(newPassword);
    }

    /**
     * Получить профиль текущего пользователя.
     * @return DTO {@link User}
     */
    @Operation(summary = "Получение профиля текущего пользователя")
    @GetMapping("/me")
    public User getUser() {
        return userService.getUser();
    }

    /**
     * Обновление полей профиля (firstName, lastName, phone).
     * @param updateUser DTO с новыми значениями
     * @return тот же DTO (для фронта)
     */
    @Operation(summary = "Обновление профиля пользователя")
    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody UpdateUser updateUser) {
        return userService.updateUser(updateUser);
    }

    /**
     * Обновление аватара пользователя. Старый файл (если был) удаляется.
     * @param image новый файл изображения (multipart)
     * @return 200 OK или код ошибки
     */
    @Operation(summary = "Обновление аватара авторизованного пользователя",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аватар обновлён"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserImage(
            @RequestParam("image") MultipartFile image) {
        return userService.updateUserImage(image);
    }
}