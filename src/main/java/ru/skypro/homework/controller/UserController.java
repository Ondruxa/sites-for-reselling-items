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

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    @PostMapping("/set_password")
    public void setPassword(@RequestBody NewPassword newPassword) {
    }

    @GetMapping("/me")
    public User getUser() {
        return null;
    }

    @PatchMapping("/me")
    public UpdateUser updateUser(@RequestBody UpdateUser updateUser) {
        return null;
    }

    @Operation(summary = "Обновление аватара авторизованного пользователя",
            security = {@SecurityRequirement(name = "BearerAuth")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аватар обновлён"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateUserImage(
            @Parameter(description = "Изображение профиля", required = true) @RequestParam("image") MultipartFile image) {
        // Заглушка: просто возвращаем успешный ответ
        return ResponseEntity.ok().build();
    }
}