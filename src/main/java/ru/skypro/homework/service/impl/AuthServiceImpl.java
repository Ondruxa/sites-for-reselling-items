package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

/**
 * Реализация сервиса аутентификации пользователей.
 * <p>
 * Основные функции:
 * <ul>
 *   <li>Аутентификация пользователя по логину и паролю</li>
 *   <li>Регистрация нового пользователя</li>
 * </ul>
 * <p>
 * Исключения:
 * <ul>
 *   <li>IllegalArgumentException — если данные некорректны</li>
 * </ul>
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Аутентифицирует пользователя по логину и паролю.
     * @param userName логин (email)
     * @param password пароль в открытом виде
     * @return true, если пара логин/пароль валидна; иначе false
     */
    @Override
    public boolean login(String userName, String password) {
        return userRepository.findByEmail(userName)
                .map(user -> encoder.matches(password, user.getPassword()))
                .orElse(false);
    }

    /**
     * Регистрирует нового пользователя.
     * @param register DTO с данными регистрации
     * @return true, если регистрация прошла успешно; false, если пользователь уже существует
     */
    @Override
    public boolean register(Register register) {
        if (userRepository.findByEmail(register.getUsername()).isPresent()) {
            return false;
        }
        UserEntity entity = userMapper.fromRegister(register);
        entity.setPassword(encoder.encode(entity.getPassword()));
        userRepository.save(entity);
        return true;
    }
}
