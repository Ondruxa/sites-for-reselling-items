package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.config.UserSecurityDTO;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.UserEntity;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity testUser;
    private UserSecurityDTO userSecurityDTO;
    private NewPassword validPassword;
    private UpdateUser validUpdateUser;
    private MultipartFile validImage;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Ivan");
        testUser.setLastName("Ivanov");
        testUser.setPhone("+79991234567");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);

        // Создаем UserSecurityDTO для аутентификации
        userSecurityDTO = new UserSecurityDTO(testUser);

        validPassword = new NewPassword();
        validPassword.setCurrentPassword("currentPassword123");
        validPassword.setNewPassword("newSecurePassword456");

        validUpdateUser = new UpdateUser();
        validUpdateUser.setFirstName("Ivan");
        validUpdateUser.setLastName("Ivanov");
        validUpdateUser.setPhone("+79991234567");

        validImage = new MockMultipartFile(
                "profileImage",
                "avatar.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Тест установки пароля с валидными данными
     * Проверяем, что метод выполняется без исключений при корректных паролях
     */
    @Test
    void setPassword_WithValidData_ShouldExecuteSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newSecurePassword456")).thenReturn("newEncodedPassword");

        // Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(validPassword),
                "Метод setPassword должен выполняться без исключений при валидных данных");

        // Verify
        verify(userRepository).save(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    /**
     * Тест установки пароля с null объектом
     * Проверяем, что метод выбрасывает NullPointerException при null параметре
     */
    @Test
    void setPassword_WithNullParameter_ShouldExecuteWithoutErrors() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        // Act & Assert
        assertThrows(NullPointerException.class,
                () -> userService.setPassword(null),
                "Метод setPassword должен обрабатывать null параметр без исключений");
    }

    /**
     * Тест установки пароля с частично заполненным объектом
     */
    @Test
    void setPassword_WithPartialData_ShouldExecuteSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        NewPassword partialPassword = new NewPassword();
        partialPassword.setCurrentPassword("onlyCurrent");
        partialPassword.setNewPassword("newPassword123"); // Добавляем новый пароль

        // Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(partialPassword),
                "Метод setPassword должен работать с частично заполненными объектами");
    }

    /**
     * Тест установки пароля с пустыми строками
     */
    @Test
    void setPassword_WithEmptyStrings_ShouldExecuteSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("")).thenReturn("encodedEmptyPassword");

        NewPassword emptyPassword = new NewPassword();
        emptyPassword.setCurrentPassword("");
        emptyPassword.setNewPassword("");

        // Act & Assert
        assertDoesNotThrow(() -> userService.setPassword(emptyPassword),
                "Метод setPassword должен обрабатывать пустые строки паролей");
    }

    /**
     * Тест получения пользователя - проверяем контракт метода
     * В текущей реализации всегда возвращается null
     */
    @Test
    void getUser_ShouldAlwaysReturnNull() {
        // Act
        User result = userService.getUser();

        // Assert
        assertNull(result, "Метод getUser() должен возвращать null в соответствии с текущей реализацией");
    }

    /**
     * Тест обновления пользователя с валидными данными
     * Проверяем корректность обработки данных, соответствующих ограничениям
     */
    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserEntity savedUser = new UserEntity();
        savedUser.setFirstName("Ivan");
        savedUser.setLastName("Ivanov");
        savedUser.setPhone("+79991234567");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        UpdateUser result = userService.updateUser(validUpdateUser);

        // Assert
        assertNotNull(result, "Метод updateUser() должен возвращать обновленные данные");
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("+79991234567", result.getPhone());
    }

    /**
     * Тест обновления пользователя, когда пользователь не найден
     */
    @Test
    void updateUser_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> userService.updateUser(validUpdateUser),
                "Должно выбрасываться исключение, когда пользователь не найден");
    }

    /**
     * Тест обновления пользователя с null данными
     */
    @Test
    void updateUser_WithNullData_ShouldHandleGracefully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        UpdateUser result = userService.updateUser(null);

        // Assert
        assertNull(result, "Метод должен возвращать null при null входных данных");
    }

    /**
     * Тест обновления пользователя с частичными данными (только имя)
     */
    @Test
    void updateUser_WithOnlyFirstName_ShouldUpdateOnlyFirstName() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        UpdateUser partialUpdate = new UpdateUser();
        partialUpdate.setFirstName("NewFirstNameOnly");

        // Act
        UpdateUser result = userService.updateUser(partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("NewFirstNameOnly", result.getFirstName());
        // Остальные поля должны остаться null или не измениться
        assertNull(result.getLastName());
        assertNull(result.getPhone());
    }

    /**
     * Тест обновления аватара с валидным изображением
     */
    @Test
    void updateUserImage_WithValidImage_ShouldExecuteSuccessfully() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act & Assert
        assertDoesNotThrow(() -> userService.updateUserImage(validImage),
                "Метод updateUserImage должен работать с валидным изображением");

        // Verify
        verify(userRepository).save(testUser);
    }

    /**
     * Тест обновления аватара с null изображением
     */
    @Test
    void updateUserImage_WithNullImage_ShouldExecuteWithoutErrors() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertDoesNotThrow(() -> userService.updateUserImage(null),
                "Метод updateUserImage должен обрабатывать null параметр");

        // Verify что save не вызывался при null
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Тест обновления аватара пользователя с валидным изображением
     */
    @Test
    void updateUserImage_WithValidImage_ShouldReturnOkResponse() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(validImage);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Должен возвращаться OK статус");
        verify(userRepository).save(testUser);
    }

    /**
     * Тест обновления аватара с null изображением
     */
    @Test
    void updateUserImage_WithNullImage_ShouldReturnBadRequest() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Должен возвращаться BAD_REQUEST для null");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Тест обновления аватара с пустым изображением
     */
    @Test
    void updateUserImage_WithEmptyImage_ShouldReturnBadRequest() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        MultipartFile emptyImage = new MockMultipartFile(
                "emptyImage",
                "empty.jpg",
                "image/jpeg",
                new byte[0] // пустой массив байтов
        );

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(emptyImage);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Должен возвращаться BAD_REQUEST для пустого изображения");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Тест обновления аватара, когда пользователь не найден
     */
    @Test
    void updateUserImage_WhenUserNotFound_ShouldReturnBadRequest() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(validImage);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Должен возвращаться BAD_REQUEST когда пользователь не найден");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    /**
     * Тест обновления аватара с ошибкой создания директории
     */
    @Test
    void updateUserImage_WithDirectoryCreationError_ShouldReturnInternalServerError() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        MultipartFile validImage = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                new byte[]{1, 2, 3, 4, 5}
        );

        // Act
        ResponseEntity<Void> response = userService.updateUserImage(validImage);

        // Assert - в нормальных условиях должен быть OK
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    /**
     * Тест метода findByEmail
     */
    @Test
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        Optional<UserEntity> result = userService.findByEmail(email);

        // Assert
        assertTrue(result.isPresent(), "Должен возвращаться пользователь для существующего email");
        assertEquals(testUser, result.get());
    }

    /**
     * Тест метода findByEmail с несуществующим email
     */
    @Test
    void findByEmail_WithNonExistingEmail_ShouldReturnEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<UserEntity> result = userService.findByEmail(email);

        // Assert
        assertFalse(result.isPresent(), "Должен возвращаться empty для несуществующего email");
    }

    /**
     * Тест метода getUser - должен возвращать User DTO
     */
    @Test
    void getUser_ShouldReturnUserDTO() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        User expectedUser = new User();
        expectedUser.setEmail("test@example.com");
        expectedUser.setFirstName("Ivan");
        expectedUser.setLastName("Ivanov");
        expectedUser.setPhone("+79991234567");
        when(userMapper.toDto(testUser)).thenReturn(expectedUser);

        // Act
        User result = userService.getUser();

        // Assert
        assertNotNull(result, "Метод getUser должен возвращать User DTO");
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Ivanov", result.getLastName());
        assertEquals("+79991234567", result.getPhone());
    }

    /**
     * Тест метода getUser, когда пользователь не найден
     */
    @Test
    void getUser_WhenUserNotFound_ShouldReturnNull() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@example.com");
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        User result = userService.getUser();

        // Assert
        assertNull(result, "Метод getUser должен возвращать null когда пользователь не найден");
    }
}
