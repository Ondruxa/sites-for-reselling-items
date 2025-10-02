package ru.skypro.homework.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.AdEntity;
import ru.skypro.homework.model.ImageEntity;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final ImageService imageService;
    private final AdRepository adRepository;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper,
                           ImageService imageService,
                           AdRepository adRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.imageService = imageService;
        this.adRepository = adRepository;
    }

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public User getUser() {
        UserEntity current = getCurrentUserEntity();
        return userMapper.toDto(current);
    }

    @Override
    @Transactional
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
    @Transactional
    public ResponseEntity<Void> updateUserImage(MultipartFile image) {
        UserEntity current = getCurrentUserEntity();
        if (current == null) {
            return ResponseEntity.badRequest().build();
        }
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            ImageEntity oldImage = current.getImage();
            ImageEntity saved = imageService.save(image, "user_" + current.getId());
            current.setImage(saved);
            userRepository.save(current);
            if (oldImage != null) {
                imageService.delete(oldImage.getId());
            }
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    @Transactional
    public void deleteCurrentUser() {
        UserEntity current = getCurrentUserEntity();
        if (current == null) {
            throw new IllegalStateException("Текущий пользователь не найден");
        }
        // Собираем изображения объявлений пользователя и удаляем их (distinct по id)
        adRepository.findAllByAuthor_Id(current.getId()).stream()
                .map(AdEntity::getImage)
                .filter(java.util.Objects::nonNull)
                .map(ImageEntity::getId)
                .distinct()
                .forEach(imageService::delete);
        // Удаляем аватар пользователя
        if (current.getImage() != null) {
            imageService.delete(current.getImage().getId());
        }
        userRepository.delete(current);
    }

    @Override
    @Transactional(readOnly = true)
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
