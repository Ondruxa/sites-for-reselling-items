package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.UserEntity;

import java.util.Objects;

@Component
public class AdMapper {
    public Ad toDto(AdEntity entity) {
        if (entity == null) return null;
        Ad dto = new Ad();
        dto.setPk(entity.getId());
        dto.setAuthor(entity.getAuthor() != null ? entity.getAuthor().getId() : null);
        dto.setImage(entity.getImage());
        dto.setPrice(entity.getPrice());
        dto.setTitle(entity.getTitle());
        return dto;
    }

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
        dto.setImage(entity.getImage());
        dto.setPrice(entity.getPrice());
        dto.setTitle(entity.getTitle());
        return dto;
    }

    public AdEntity fromCreate(CreateOrUpdateAd dto, UserEntity author) {
        if (dto == null) return null;
        AdEntity entity = new AdEntity();
        entity.setTitle(dto.getTitle());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
        entity.setAuthor(Objects.requireNonNull(author, "Author is required"));
        return entity;
    }

    public void updateEntity(CreateOrUpdateAd dto, AdEntity target) {
        if (dto == null || target == null) return;
        target.setTitle(dto.getTitle());
        target.setPrice(dto.getPrice());
        target.setDescription(dto.getDescription());
    }
}

