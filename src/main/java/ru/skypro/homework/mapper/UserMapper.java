package ru.skypro.homework.mapper;

import org.springframework.stereotype.Component;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;

@Component
public class UserMapper {
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

    public void updateEntity(UpdateUser dto, UserEntity entity) {
        if (dto == null || entity == null) return;
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setPhone(dto.getPhone());
    }

    public UserEntity fromRegister(Register register) {
        if (register == null) return null;
        UserEntity entity = new UserEntity();
        entity.setEmail(register.getUsername());
        entity.setPassword(register.getPassword()); // пароль зашифруется вне маппера
        entity.setFirstName(register.getFirstName());
        entity.setLastName(register.getLastName());
        entity.setPhone(register.getPhone());
        entity.setRole(register.getRole());
        return entity;
    }
}
