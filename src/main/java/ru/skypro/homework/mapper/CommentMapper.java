package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.CommentEntity;
import ru.skypro.homework.model.UserEntity;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {
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
            dto.setAuthorImage(author.getImage());
        }
        return dto;
    }

    public Comments toDtos(List<CommentEntity> entities) {
        Comments wrapper = new Comments();
        List<Comment> list = entities.stream().map(this::toDto).collect(Collectors.toList());
        wrapper.setResults(list);
        wrapper.setCount(list.size());
        return wrapper;
    }

    public CommentEntity fromCreate(CreateOrUpdateComment dto, AdEntity ad, UserEntity author) {
        if (dto == null) return null;
        CommentEntity entity = new CommentEntity();
        entity.setText(dto.getText());
        entity.setAd(ad);
        entity.setAuthor(author);
        entity.setCreatedAt(Instant.now().toEpochMilli());
        return entity;
    }

    public void updateEntity(CreateOrUpdateComment dto, CommentEntity target) {
        if (dto == null || target == null) return;
        target.setText(dto.getText());
    }
}

