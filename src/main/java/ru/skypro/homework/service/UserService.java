package ru.skypro.homework.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.UserEntity;

import java.util.Optional;

public interface UserService {
    /**
     * Устанавливает пароль пользователя.
     */
    void setPassword(NewPassword newPassword);

    /**
     * Возвращает профиль текущего пользователя.
     *
     * @return Пользователь (возвращается null, если пользователь не найден)
     */
    User getUser();

    /**
     * Обновляет профиль пользователя.
     *
     * @param updateUser Данные для обновления
     * @return Объект с информацией о новом профиле (или null, если ошибка)
     */
    UpdateUser updateUser(UpdateUser updateUser);

    /**
     * Обновляет изображение профиля пользователя.
     *
     * @param image Файл изображения
     * @return Ответ сервера (OK, если обновление прошло успешно)
     */
    ResponseEntity<Void> updateUserImage(MultipartFile image);

    /**
     * Поиск пользователя по email (для аутентификации)
     *
     * @param email email пользователя
     * @return Optional с UserEntity
     */
    Optional<UserEntity> findByEmail(String email);
}
