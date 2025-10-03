package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.skypro.homework.dto.Login;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.service.AuthService;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
/**
 * Контроллер аутентификации и регистрации.
 * <p>
 * Содержит два публичных эндпоинта (не требуют аутентификации):
 * <ul>
 *   <li><b>POST /login</b> — проверка учетных данных пользователя.</li>
 *   <li><b>POST /register</b> — регистрация новой учетной записи.</li>
 * </ul>
 * Возвращает только статусы без тела: 200 / 201 когда ОК, коды ошибок при неудаче.
 * </p>
 */
public class AuthController {

    private final AuthService authService;

    /**
     * Аутентификация пользователя по email + пароль.
     * @param login DTO с полями username / password
     * @return 200 OK при успехе или 401 Unauthorized при неверных данных
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Login login) {
        if (authService.login(login.getUsername(), login.getPassword())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Регистрация новой учетной записи.
     * @param register DTO с регистрационными данными
     * @return 201 Created при успехе, 400 Bad Request если пользователь уже существует или данные некорректны
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Register register) {
        if (authService.register(register)) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
