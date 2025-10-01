package ru.skypro.homework.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public void setPassword(NewPassword newPassword) {
        UserEntity current = getCurrentUserEntity();
        if (current == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        if (!passwordEncoder.matches(newPassword.getCurrentPassword(), current.getPassword())) {
            throw new IllegalArgumentException("Текущий пароль не совпадает");
        }
        current.setPassword(passwordEncoder.encode(newPassword.getNewPassword()));
        userRepository.save(current);
    }

    @Override
    public User getUser() {
        UserEntity current = getCurrentUserEntity();
        return userMapper.toDto(current);
    }

    @Override
    public UpdateUser updateUser(UpdateUser updateUser) {
        UserEntity current = getCurrentUserEntity();
        if (current == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        userMapper.updateEntity(updateUser, current);
        userRepository.save(current);
        return updateUser;
    }

    @Override
    public ResponseEntity<Void> updateUserImage(MultipartFile image) {
        UserEntity current = getCurrentUserEntity();
        if (current == null) {
            return ResponseEntity.badRequest().build();
        }
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            String original = image.getOriginalFilename();
            String ext = original != null && original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
            String fileName = "user_" + current.getId() + "_" + UUID.randomUUID() + ext;
            Path dir = Paths.get("images");
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path target = dir.resolve(fileName);
            Files.write(target, image.getBytes());
            current.setImage("/images/" + fileName);
            userRepository.save(current);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private UserEntity getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return null;
        }
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}
