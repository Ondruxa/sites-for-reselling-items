package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;

/**
 * Маппер пользователя: преобразует сущность пользователя{@link UserEntity}
 * в DTO уровня API ({@link User}) и обратно (из входных DTO {@link Register}, {@link UpdateUser}).
 * <p>
 * Основные задачи:
 * <ul>
 *   <li>Отсечение чувствительных данных (хэш пароля не попадает в {@code User}).</li>
 *   <li>Формирование ссылки на аватар (/images/{id}) из связанной {@code ImageEntity}.</li>
 *   <li>Частичное обновление только разрешённых полей (имя, фамилия, телефон).</li>
 *   <li>Подготовка сущности для регистрации (пароль шифруется позже в сервисе).</li>
 * </ul>
 */
@Component
public class UserMapper {

    /**
     * Преобразование сущности пользователя в DTO для выдачи наружу.
     * @param entity доменная сущность
     * @return {@link User} или null если вход null
     */
    public User toDto(UserEntity entity) {
        if (entity == null) return null;
        User dto = new User();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setPhone(entity.getPhone());
        dto.setRole(entity.getRole() != null ? entity.getRole().name() : null);
        ImageEntity img = entity.getImage();
        dto.setImage(img != null ? "/images/" + img.getId() : null);
        return dto;
    }

    /**
     * Частичное обновление разрешённых полей пользователя данными из DTO.
     * Не изменяет email, пароль, роль и аватар.
     * @param dto входные данные
     * @param entity целевая сущность
     */
    public void updateEntity(UpdateUser dto, UserEntity entity) {
        if (dto == null || entity == null) return;
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPhone(dto.getPhone());
    }

    /**
     * Создание новой сущности пользователя из данных регистрации.
     * Пароль НЕ шифруется здесь — это делает сервис при сохранении.
     * @param register DTO регистрации
     * @return несохранённая сущность {@link UserEntity}
     */
    public UserEntity fromRegister(Register register) {
        if (register == null) return null;
        UserEntity entity = new UserEntity();
        entity.setEmail(register.getUsername());
        entity.setPassword(register.getPassword());
        entity.setFirstName(register.getFirstName());
        entity.setLastName(register.getLastName());
        entity.setPhone(register.getPhone());
        entity.setRole(register.getRole());
        return entity;
    }
}
