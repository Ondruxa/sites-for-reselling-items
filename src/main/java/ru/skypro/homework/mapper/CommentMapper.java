package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования комментариев между сущностью {@link ru.skypro.homework.model.CommentEntity}
 * и DTO уровня API: {@link ru.skypro.homework.dto.Comment}, обёрткой {@link ru.skypro.homework.dto.Comments}
 * и входным DTO {@link ru.skypro.homework.dto.CreateOrUpdateComment}.
 * <p>
 * Основные задачи:
 * <ul>
 *   <li>Формирование DTO с полями автора (id, имя, аватар).</li>
 *   <li>Обёртка списка комментариев в {@code Comments} с полем count.</li>
 *   <li>Создание новой сущности с проставлением timestamp (createdAt).</li>
 *   <li>Обновление текста существующего комментария (updateEntity).</li>
 * </ul>
 */
@Component
public class CommentMapper {
    /**
     * Преобразует одну сущность комментария в DTO.
     * @param entity сущность
     * @return {@link ru.skypro.homework.dto.Comment} или null
     */
    public Comment toDto(CommentEntity entity) {
        if (entity == null) return null;
        Comment dto = new Comment();
        dto.setPk(entity.getId());
        dto.setText(entity.getText());
        dto.setCreatedAt(entity.getCreatedAt());
        UserEntity author = entity.getAuthor();
        if (author != null) {
            dto.setAuthor(author.getId());
            dto.setAuthorFirstName(author.getFirstName());
            ImageEntity img = author.getImage();
            dto.setAuthorImage(img != null ? "/images/" + img.getId() : null);
        }
        return dto;
    }

    /**
     * Преобразует список сущностей в обёртку {@link Comments} c заполнением count.
     * @param entities список сущностей
     * @return обёртка DTO
     */
    public Comments toDtos(List<CommentEntity> entities) {
        Comments wrapper = new Comments();
        List<Comment> list = entities.stream().map(this::toDto).collect(Collectors.toList());
        wrapper.setResults(list);
        wrapper.setCount(list.size());
        return wrapper;
    }

    /**
     * Создание новой сущности комментария из входного DTO.
     * @param dto входные данные
     * @param ad объявление (обязательная связь)
     * @param author автор комментария
     * @return новая несохранённая сущность
     */
    public CommentEntity fromCreate(CreateOrUpdateComment dto, AdEntity ad, UserEntity author) {
        if (dto == null) return null;
        CommentEntity entity = new CommentEntity();
        entity.setText(dto.getText());
        entity.setAd(ad);
        entity.setAuthor(author);
        entity.setCreatedAt(Instant.now().toEpochMilli());
        return entity;
    }

    /**
     * Обновление текста существующего комментария.
     * @param dto данные
     * @param target целевая сущность
     */
    public void updateEntity(CreateOrUpdateComment dto, CommentEntity target) {
        if (dto == null || target == null) return;
        target.setText(dto.getText());
    }
}
