package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;

import java.util.Objects;

/**
 * Маппер для преобразования между сущностью объявления {@link ru.skypro.homework.model.AdEntity}
 * и различными DTO: {@link ru.skypro.homework.dto.Ad}, {@link ru.skypro.homework.dto.ExtendedAd},
 * а также входным DTO {@link ru.skypro.homework.dto.CreateOrUpdateAd}.
 * <p>
 * Основные задачи:
 * <ul>
 *   <li>Отсечение лишних внутренних полей при выдаче краткого списка.</li>
 *   <li>Сбор расширенной информации об авторе для детальной карточки объявления.</li>
 *   <li>Формирование новой сущности при создании (fromCreate) и обновление существующей (updateEntity).</li>
 * </ul>
 */
@Component
public class AdMapper {
    /**
     * Преобразование сущности в краткое DTO объявления.
     * @param entity сущность объявления
     * @return DTO {@link ru.skypro.homework.dto.Ad} или null если вход null
     */
    public Ad toDto(AdEntity entity) {
        if (entity == null) return null;
        Ad dto = new Ad();
        dto.setPk(entity.getId());
        dto.setAuthor(entity.getAuthor() != null ? entity.getAuthor().getId() : null);
        ImageEntity img = entity.getImage();
        dto.setImage(img != null ? "/images/" + img.getId() : null);
        dto.setPrice(entity.getPrice());
        dto.setTitle(entity.getTitle());
        return dto;
    }

    /**
     * Преобразование сущности в расширенное DTO (карточка объявления).
     * @param entity сущность объявления
     * @return DTO {@link ru.skypro.homework.dto.ExtendedAd} или null если вход null
     */
    public ExtendedAd toExtendedDto(AdEntity entity) {
        if (entity == null) return null;
        ExtendedAd dto = new ExtendedAd();
        dto.setPk(entity.getId());
        UserEntity author = entity.getAuthor();
        if (author != null) {
            dto.setAuthorFirstName(author.getFirstName());
            dto.setAuthorLastName(author.getLastName());
            dto.setEmail(author.getEmail());
            dto.setPhone(author.getPhone());
        }
        dto.setDescription(entity.getDescription());
        ImageEntity img = entity.getImage();
        dto.setImage(img != null ? "/images/" + img.getId() : null);
        dto.setPrice(entity.getPrice());
        dto.setTitle(entity.getTitle());
        return dto;
    }

    /**
     * Создание новой сущности объявления из входного DTO.
     * @param dto входные данные (title, price, description)
     * @param author сущность автора (обязательна)
     * @return новая несохранённая сущность {@link AdEntity}
     */
    public AdEntity fromCreate(CreateOrUpdateAd dto, UserEntity author) {
        if (dto == null) return null;
        AdEntity entity = new AdEntity();
        entity.setTitle(dto.getTitle());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        entity.setAuthor(Objects.requireNonNull(author, "Author is required"));
        return entity;
    }

    /**
     * Обновление существующей сущности объявления данными из входного DTO.
     * @param dto входные данные
     * @param target изменяемая сущность
     */
    public void updateEntity(CreateOrUpdateAd dto, AdEntity target) {
        if (dto == null || target == null) return;
        target.setTitle(dto.getTitle());
        target.setPrice(dto.getPrice());
        target.setDescription(dto.getDescription());
    }
}
