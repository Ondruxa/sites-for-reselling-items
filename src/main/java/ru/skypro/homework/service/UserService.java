package ru.skypro.homework.service;

public interface UserService {
    boolean setPassword(String currentPassword, String newPassword);

    boolean getUser();
}
