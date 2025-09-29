package ru.skypro.homework.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public void setPassword(NewPassword newPassword) {
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public UpdateUser updateUser(UpdateUser updateUser) {
        return null;
    }

    @Override
    public ResponseEntity<Void> updateUserImage(MultipartFile image) {
        return ResponseEntity.ok().build();
    }
}
